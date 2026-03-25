package dev.munebase.dynamickeybinds.action;

import net.minecraft.nbt.CompoundTag;

/**
 * Functional interface for handling keybind actions.
 * Invoked when a keybind with an associated action is pressed.
 */
public interface DynamicKeybindActionHandler {
    /**
     * Called when a keybind with this action is pressed.
     *
     * @param actionID the ID of the action being triggered
     * @param data the NBT data associated with this keybind instance
     */
    void onAction(String actionID, CompoundTag data);
}
