package dev.munebase.dynamickeybinds.server;

import dev.munebase.dynamickeybinds.action.DynamicKeybindAction;
import dev.munebase.dynamickeybinds.model.DisplaySpec;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

/**
 * Entry point for server-side per-player dynamic keybind mutations.
 *
 * Loader implementations must set a registry instance during initialization.
 */
public final class DynamicKeyServerApi {
    private DynamicKeyServerApi() {
    }

    /**
     * Sets the active server registry implementation.
     */
    public static void setRegistry(DynamicKeyServerRegistry registry) {
        Holder.REGISTRY = registry;
    }

    /**
     * Gets the active server registry implementation.
     */
    public static DynamicKeyServerRegistry getRegistry() {
        DynamicKeyServerRegistry registry = Holder.REGISTRY;
        if (registry == null) {
            throw new IllegalStateException("DynamicKeyServerApi registry not initialized");
        }
        return registry;
    }

    /**
     * Registers a keybind for a specific player.
     */
    public static boolean registerForPlayer(ServerPlayer serverPlayer, String id, int keyCode, String category, DynamicKeybindAction action, DisplaySpec displaySpec) {
        return getRegistry().registerForPlayer(serverPlayer, id, keyCode, category, Optional.ofNullable(action), displaySpec == null ? DisplaySpec.empty() : displaySpec);
    }

    /**
     * Registers a keybind for a specific player.
     */
    public static boolean registerForPlayer(ServerPlayer serverPlayer, String id, int keyCode, String category, Optional<DynamicKeybindAction> action, DisplaySpec displaySpec) {
        return getRegistry().registerForPlayer(serverPlayer, id, keyCode, category, action == null ? Optional.empty() : action, displaySpec == null ? DisplaySpec.empty() : displaySpec);
    }

    /**
     * Removes a keybind for a specific player.
     */
    public static boolean unregisterForPlayer(ServerPlayer serverPlayer, String id) {
        return getRegistry().unregisterForPlayer(serverPlayer, id);
    }

    private static final class Holder {
        private static DynamicKeyServerRegistry REGISTRY = null;

        private Holder() {
        }
    }
}
