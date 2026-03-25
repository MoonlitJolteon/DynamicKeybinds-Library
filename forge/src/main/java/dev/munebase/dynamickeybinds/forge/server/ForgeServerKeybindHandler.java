package dev.munebase.dynamickeybinds.forge.server;

import dev.munebase.dynamickeybinds.network.AddKeybindPacket;
import dev.munebase.dynamickeybinds.network.RemoveKeybindPacket;
import dev.munebase.dynamickeybinds.network.SyncKeybindsPacket;
import dev.munebase.dynamickeybinds.network.UpdateKeybindPacket;
import dev.munebase.dynamickeybinds.persistence.StoredKeybind;
import dev.munebase.dynamickeybinds.server.CommonServerKeybindHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

/**
 * Handles server-side keybind persistence mutations for Forge networking.
 */
public final class ForgeServerKeybindHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("DynamicKeybinds");
    private static final CommonServerKeybindHandler COMMON = new CommonServerKeybindHandler(
        LOGGER,
        "Forge",
        ForgeServerKeybindHandler::syncLoadedKeybindsToPlayer
    );

    private ForgeServerKeybindHandler() {
    }

    public static void syncKeybindsToPlayer(ServerPlayer player) {
        COMMON.syncKeybindsToPlayer(player);
    }

    public static void handleAddKeybind(ServerPlayer player, AddKeybindPacket pkt) {
        COMMON.handleAddKeybind(player, pkt);
    }

    public static void handleRemoveKeybind(ServerPlayer player, RemoveKeybindPacket pkt) {
        COMMON.handleRemoveKeybind(player, pkt);
    }

    public static void handleUpdateKeybind(ServerPlayer player, UpdateKeybindPacket pkt) {
        COMMON.handleUpdateKeybind(player, pkt);
    }

    private static void syncLoadedKeybindsToPlayer(net.minecraft.server.level.ServerPlayer player, List<StoredKeybind> keybinds) {
        try {
            SyncKeybindsPacket packet = new SyncKeybindsPacket(keybinds);
            dev.munebase.dynamickeybinds.forge.network.ForgeNetworking.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                packet
            );
        } catch (Exception e) {
            LOGGER.error("Error syncing keybinds to player", e);
        }
    }
}

