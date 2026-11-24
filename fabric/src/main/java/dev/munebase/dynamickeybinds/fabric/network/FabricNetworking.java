package dev.munebase.dynamickeybinds.fabric.network;

import dev.munebase.dynamickeybinds.action.DynamicKeybindAction;
import dev.munebase.dynamickeybinds.fabric.FabricKeybindPersistence;
import dev.munebase.dynamickeybinds.fabric.server.FabricServerKeybindHandler;
import dev.munebase.dynamickeybinds.model.DisplaySpec;
import dev.munebase.dynamickeybinds.network.AddKeybindPacket;
import dev.munebase.dynamickeybinds.network.RemoveKeybindPacket;
import dev.munebase.dynamickeybinds.network.SyncKeybindsPacket;
import dev.munebase.dynamickeybinds.network.UpdateKeybindPacket;
import dev.munebase.dynamickeybinds.persistence.StoredKey;
import dev.munebase.dynamickeybinds.persistence.StoredKeybind;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Fabric networking: payload registration, receivers, and send helpers.
 *
 * <p>Fabric runs receivers on the main thread, so handlers can touch game state directly.
 */
public final class FabricNetworking {
    private static final Logger LOGGER = LoggerFactory.getLogger("DynamicKeybinds");

    private FabricNetworking() {
    }

    /**
     * Registers every payload type. Must run on both sides, before any receivers are registered.
     */
    public static void registerPayloadTypes() {
        PayloadTypeRegistry.playS2C().register(SyncKeybindsPacket.TYPE, SyncKeybindsPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(AddKeybindPacket.TYPE, AddKeybindPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(RemoveKeybindPacket.TYPE, RemoveKeybindPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(UpdateKeybindPacket.TYPE, UpdateKeybindPacket.STREAM_CODEC);
    }

    /**
     * Registers client-side packet receivers.
     */
    public static void registerClientHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(SyncKeybindsPacket.TYPE,
            (packet, context) -> runLogged("handling sync packet",
                () -> FabricKeybindPersistence.handleServerSync(packet.getKeybinds())));
    }

    /**
     * Registers server-side packet receivers.
     */
    public static void registerServerHandlers() {
        ServerPlayNetworking.registerGlobalReceiver(AddKeybindPacket.TYPE,
            (packet, context) -> runLogged("handling add keybind packet",
                () -> FabricServerKeybindHandler.handleAddKeybind(context.player(), packet)));
        ServerPlayNetworking.registerGlobalReceiver(RemoveKeybindPacket.TYPE,
            (packet, context) -> runLogged("handling remove keybind packet",
                () -> FabricServerKeybindHandler.handleRemoveKeybind(context.player(), packet)));
        ServerPlayNetworking.registerGlobalReceiver(UpdateKeybindPacket.TYPE,
            (packet, context) -> runLogged("handling update keybind packet",
                () -> FabricServerKeybindHandler.handleUpdateKeybind(context.player(), packet)));
    }

    /**
     * Sends a client add-keybind request packet.
     */
    public static void sendAddKeybindToServer(String id, int keyCode, String category, Optional<DynamicKeybindAction> action, DisplaySpec displaySpec) {
        runLogged("sending add keybind packet",
            () -> ClientPlayNetworking.send(new AddKeybindPacket(id, keyCode, category, action, displaySpec)));
    }

    /**
     * Sends a client remove-keybind request packet.
     */
    public static void sendRemoveKeybindToServer(String id) {
        runLogged("sending remove keybind packet",
            () -> ClientPlayNetworking.send(new RemoveKeybindPacket(id)));
    }

    /**
     * Sends a client update-keybind request packet.
     */
    public static void sendUpdateKeybindToServer(String id, StoredKey key) {
        runLogged("sending update keybind packet",
            () -> ClientPlayNetworking.send(new UpdateKeybindPacket(id, key)));
    }

    /**
     * Sends a server sync packet containing all keybinds for a player.
     */
    public static void sendSyncKeybindsToPlayer(ServerPlayer player, List<StoredKeybind> keybinds) {
        runLogged("sending sync packet",
            () -> ServerPlayNetworking.send(player, new SyncKeybindsPacket(keybinds)));
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
