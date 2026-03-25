package dev.munebase.dynamickeybinds.persistence;

import dev.munebase.dynamickeybinds.action.DynamicKeybindAction;
import java.util.Optional;

/**
 * Immutable record representing a stored keybind that can be persisted to NBT or net sync.
 *
 * Represents the complete state of a dynamic keybind including its ID, keycode, category,
 * and optional custom action. This record is used as the data transfer object between
 * registration, persistence, networking, and client rendering.
 *
 * <p><strong>Serialization:</strong> This record is serialized to NBT tags for server-side
 * persistence and network synchronization. The action field (if present) is nested under
 * an "action" tag containing "actionID" and "data" subtags.</p>
 *
 * <p><strong>Example:</strong></p>
 * <pre>
 * {@code
 * // Create without action (simple keybind)
 * StoredKeybind simpleKey = new StoredKeybind("mymod:move_key", 19, "mymod");
 * 
 * // Create with action (contextual keybind)
 * CompoundTag actionData = new CompoundTag();
 * actionData.putInt("x", 100);
 * DynamicKeybindAction action = new DynamicKeybindAction("mymod:mark_location", actionData);
 * StoredKeybind contextualKey = new StoredKeybind(
 *   "mymod:mark_key",
 *   33,
 *   "mymod",
 *   Optional.of(action)
 * );
 * }
 * </pre>
 *
 * @param id unique identifier for the keybind (e.g., "mymod:ability_cast")
 * @param keyCode the GLFW key code (numeric value 32-348)
 * @param category the keybind category for menu organization
 * @param action optional custom action that will be triggered when the key is pressed.
 *               If empty, the keybind can still be used but won't trigger any special behavior
 */
public record StoredKeybind(String id, int keyCode, String category, Optional<DynamicKeybindAction> action) {
    /**
     * Backward-compatible constructor for keybinds without custom actions.
     *
     * @param id the keybind identifier
     * @param keyCode the GLFW key code
     * @param category the keybind category
     */
    public StoredKeybind(String id, int keyCode, String category) {
        this(id, keyCode, category, Optional.empty());
    }
}
