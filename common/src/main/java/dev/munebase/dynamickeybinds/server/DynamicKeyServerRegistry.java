package dev.munebase.dynamickeybinds.server;

import dev.munebase.dynamickeybinds.action.DynamicKeybindAction;
import dev.munebase.dynamickeybinds.model.DisplaySpec;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

/**
 * Server-side API contract for mutating dynamic keybinds for specific players.
 */
public interface DynamicKeyServerRegistry {
    /**
     * Registers a dynamic keybind for a specific player and syncs it to that player.
     *
     * @return true when the keybind was added, false otherwise
     */
    boolean registerForPlayer(ServerPlayer player, String id, int keyCode, String category, Optional<DynamicKeybindAction> action, DisplaySpec displaySpec);

    /**
     * Removes a dynamic keybind for a specific player and syncs the updated list to that player.
     *
     * @return true when a keybind was removed, false otherwise
     */
    boolean unregisterForPlayer(ServerPlayer player, String id);
}
