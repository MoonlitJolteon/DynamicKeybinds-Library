package dev.munebase.dynamickeybinds.fabric.server;

import dev.munebase.dynamickeybinds.network.AddKeybindPacket;
import dev.munebase.dynamickeybinds.network.RemoveKeybindPacket;
import dev.munebase.dynamickeybinds.network.UpdateKeybindPacket;
import dev.munebase.dynamickeybinds.persistence.StoredKeybind;
import dev.munebase.dynamickeybinds.server.CommonServerKeybindHandler;
import dev.munebase.dynamickeybinds.server.DynamicKeyServerRegistry;
import dev.munebase.dynamickeybinds.action.DynamicKeybindAction;
import dev.munebase.dynamickeybinds.model.DisplaySpec;
import dev.munebase.dynamickeybinds.fabric.network.FabricNetworking;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Handles server-side keybind synchronization and persistence for Fabric.
 */
public final class FabricServerKeybindHandler implements DynamicKeyServerRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger("DynamicKeybinds");
    private static final CommonServerKeybindHandler COMMON = new CommonServerKeybindHandler(
        LOGGER,
        "Fabric",
        FabricServerKeybindHandler::syncLoadedKeybindsToPlayer
    );

    private FabricServerKeybindHandler() {
    }

    public static DynamicKeyServerRegistry registry() {
        return new FabricServerKeybindHandler();
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

    @Override
    public boolean registerForPlayer(ServerPlayer player, String id, int keyCode, String category, Optional<DynamicKeybindAction> action, DisplaySpec displaySpec) {
        return COMMON.registerForPlayer(player, id, keyCode, category, action, displaySpec);
    }

    @Override
    public boolean unregisterForPlayer(ServerPlayer player, String id) {
        return COMMON.unregisterForPlayer(player, id);
    }

    private static void syncLoadedKeybindsToPlayer(ServerPlayer player, List<StoredKeybind> keybinds) {
        try {
            FabricNetworking.sendSyncKeybindsToPlayer(player, keybinds);
        } catch (Exception e) {
            LOGGER.error("Error syncing keybinds to player", e);
        }
    }
}

