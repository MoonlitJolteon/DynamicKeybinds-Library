package dev.munebase.dynamickeybinds.persistence;

import dev.munebase.dynamickeybinds.action.DynamicKeybindAction;
import dev.munebase.dynamickeybinds.model.DisplaySpec;
import java.util.Optional;

/**
 * Immutable record representing a stored keybind that can be persisted to NBT or net sync.
 *
 * Represents the complete state of a dynamic keybind including its ID, keycode, category,
 * optional custom action, and optional display metadata. This record is used as the data transfer object between
 * registration, persistence, networking, and client rendering.
 *
 * <p><strong>Serialization:</strong> This record is serialized to NBT tags for server-side
 * persistence and network synchronization. The action field (if present) is nested under
 * an "action" tag containing "actionID" and "data" subtags. The displaySpec is nested under
 * an optional "display" tag that contains translation key, fallback, and args.</p>
 *
 * <p><strong>Example:</strong></p>
 * <pre>
 * {@code
 * // Create without action or display (simple keybind)
 * StoredKeybind simpleKey = new StoredKeybind("mymod:move_key", 19, "mymod");
 * 
 * // Create with action and display (contextual keybind with custom label)
 * CompoundTag actionData = new CompoundTag();
 * actionData.putInt("x", 100);
 * DynamicKeybindAction action = new DynamicKeybindAction("mymod:mark_location", actionData);
 * DisplaySpec displaySpec = DisplaySpec.ofTranslationKeyWithFallback(
 *   "key.mymod.mark_instance",
 *   "Mark Location"
 * );
 * StoredKeybind contextualKey = new StoredKeybind(
 *   "mymod:mark_key",
 *   33,
 *   "mymod",
 *   Optional.of(action),
 *   displaySpec
 * );
 * }
 * </pre>
 *
 * @param id unique identifier for the keybind (e.g., "mymod:ability_cast")
 * @param keyCode the GLFW key code (numeric value 32-348)
 * @param category the keybind category for menu organization
 * @param action optional custom action that will be triggered when the key is pressed.
 *               If empty, the keybind can still be used but won't trigger any special behavior
 * @param displaySpec optional display metadata for the keybind label. If empty, uses default display.
 */
public record StoredKeybind(String id, int keyCode, String category, Optional<DynamicKeybindAction> action, DisplaySpec displaySpec) {
    /**
     * Backward-compatible constructor for keybinds without custom actions or display.
     *
     * @param id the keybind identifier
     * @param keyCode the GLFW key code
     * @param category the keybind category
     */
    public StoredKeybind(String id, int keyCode, String category) {
        this(id, keyCode, category, Optional.empty(), DisplaySpec.empty());
    }
    
    /**
     * Constructor for keybinds with action but no custom display.
     *
     * @param id the keybind identifier
     * @param keyCode the GLFW key code
     * @param category the keybind category
     * @param action the optional action
     */
    public StoredKeybind(String id, int keyCode, String category, Optional<DynamicKeybindAction> action) {
        this(id, keyCode, category, action, DisplaySpec.empty());
    }
}
