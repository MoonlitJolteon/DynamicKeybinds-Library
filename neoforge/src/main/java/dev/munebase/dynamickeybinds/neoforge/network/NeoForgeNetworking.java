package dev.munebase.dynamickeybinds.neoforge.network;

import dev.munebase.dynamickeybinds.action.DynamicKeybindAction;
import dev.munebase.dynamickeybinds.model.DisplaySpec;
import dev.munebase.dynamickeybinds.neoforge.NeoForgeKeybindPersistence;
import dev.munebase.dynamickeybinds.neoforge.server.NeoForgeServerKeybindHandler;
import dev.munebase.dynamickeybinds.network.AddKeybindPacket;
import dev.munebase.dynamickeybinds.network.RemoveKeybindPacket;
import dev.munebase.dynamickeybinds.network.SyncKeybindsPacket;
import dev.munebase.dynamickeybinds.network.UpdateKeybindPacket;
import dev.munebase.dynamickeybinds.persistence.StoredKey;
import dev.munebase.dynamickeybinds.persistence.StoredKeybind;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * NeoForge networking: payload registration, receivers, and send helpers.
 *
 * <p>NeoForge runs receivers on the main thread, so handlers can touch game state directly.
 * Handlers are lambdas rather than method references so a dedicated server never loads client classes.
 */
public final class NeoForgeNetworking {
    private static final Logger LOGGER = LoggerFactory.getLogger("DynamicKeybinds");
    private static final String PROTOCOL_VERSION = "2";

    private NeoForgeNetworking() {
    }

    /**
     * Registers every payload type with its receiver. Subscribed on the mod event bus.
     */
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        registrar.playToClient(SyncKeybindsPacket.TYPE, SyncKeybindsPacket.STREAM_CODEC,
            (packet, context) -> runLogged("handling sync packet",
                () -> NeoForgeKeybindPersistence.handleServerSync(packet.getKeybinds())));

        registrar.playToServer(AddKeybindPacket.TYPE, AddKeybindPacket.STREAM_CODEC,
            (packet, context) -> runLogged("handling add keybind packet",
                () -> NeoForgeServerKeybindHandler.handleAddKeybind((ServerPlayer) context.player(), packet)));
        registrar.playToServer(RemoveKeybindPacket.TYPE, RemoveKeybindPacket.STREAM_CODEC,
            (packet, context) -> runLogged("handling remove keybind packet",
                () -> NeoForgeServerKeybindHandler.handleRemoveKeybind((ServerPlayer) context.player(), packet)));
        registrar.playToServer(UpdateKeybindPacket.TYPE, UpdateKeybindPacket.STREAM_CODEC,
            (packet, context) -> runLogged("handling update keybind packet",
                () -> NeoForgeServerKeybindHandler.handleUpdateKeybind((ServerPlayer) context.player(), packet)));
    }

    /**
     * Sends a client add-keybind request packet.
     */
    public static void sendAddKeybindToServer(String id, int keyCode, String category, Optional<DynamicKeybindAction> action, DisplaySpec displaySpec) {
        runLogged("sending add keybind packet",
            () -> PacketDistributor.sendToServer(new AddKeybindPacket(id, keyCode, category, action, displaySpec)));
    }

    /**
     * Sends a client remove-keybind request packet.
     */
    public static void sendRemoveKeybindToServer(String id) {
        runLogged("sending remove keybind packet",
            () -> PacketDistributor.sendToServer(new RemoveKeybindPacket(id)));
    }

    /**
     * Sends a client update-keybind request packet.
     */
    public static void sendUpdateKeybindToServer(String id, StoredKey key) {
        runLogged("sending update keybind packet",
            () -> PacketDistributor.sendToServer(new UpdateKeybindPacket(id, key)));
    }

    /**
     * Sends a server sync packet containing all keybinds for a player.
     */
    public static void sendSyncKeybindsToPlayer(ServerPlayer player, List<StoredKeybind> keybinds) {
        runLogged("sending sync packet",
            () -> PacketDistributor.sendToPlayer(player, new SyncKeybindsPacket(keybinds)));
    }

    /**
     * Runs a network action, logging failures instead of letting them disconnect the player.
     */
    private static void runLogged(String description, Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            LOGGER.error("Error {}", description, e);
        }
    }
}
