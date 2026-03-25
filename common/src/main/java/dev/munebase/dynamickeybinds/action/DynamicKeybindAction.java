package dev.munebase.dynamickeybinds.action;

import net.minecraft.nbt.CompoundTag;

/**
 * Represents a custom action triggered by a dynamic keybind.
 * 
 * This immutable record stores an action identifier and optional NBT data payload,
 * allowing third-party mods to define custom context-aware keybind behaviors.
 * The action can be serialized to NBT for persistence and network synchronization.
 *
 * <p><strong>Purpose:</strong> Enables extensibility by allowing other mods to create
 * contextual keybind actions. For example, a library mod might define an action that
 * stores block coordinates and dimension, enabling the keybind to perform different
 * operations depending on where in the world it was created.</p>
 *
 * <p><strong>Example:</strong></p>
 * <pre>
 * {@code
 * // Create an action with custom data
 * CompoundTag data = new CompoundTag();
 * data.putInt("dimension", 0);
 * data.putInt("x", 250);
 * data.putInt("y", 64);
 * data.putInt("z", 300);
 *
 * DynamicKeybindAction action = new DynamicKeybindAction("example:read_block_above", data);
 * }
 * </pre>
 *
 * @param actionID unique identifier for the action type (e.g., "example:read_block_above")
 * @param data NBT compound containing action-specific data (e.g., {dimension, pos})
 */
public record DynamicKeybindAction(String actionID, CompoundTag data) {
	/**
	 * Convenience constructor for actions without data payload.
	 *
	 * @param actionID the action identifier
	 */
	public DynamicKeybindAction(String actionID) {
		this(actionID, new CompoundTag());
	}

	/**
	 * Creates a new DynamicKeybindAction with null-safe data.
	 *
	 * @param actionID the action identifier
	 * @param data the action data (null values become empty CompoundTag)
	 */
	public DynamicKeybindAction(String actionID, CompoundTag data) {
		this.actionID = actionID;
		this.data = data != null ? data : new CompoundTag();
	}
}
