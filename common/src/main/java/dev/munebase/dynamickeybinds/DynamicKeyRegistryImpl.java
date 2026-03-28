package dev.munebase.dynamickeybinds;

import com.mojang.blaze3d.platform.InputConstants;
import dev.munebase.dynamickeybinds.action.DynamicKeybindAction;
import dev.munebase.dynamickeybinds.client.DynamicKeyMapping;
import dev.munebase.dynamickeybinds.model.DisplaySpec;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe implementation of DynamicKeyRegistry using concurrent data structures.
 * 
 * Manages the registration and retrieval of dynamic keybinds at runtime. This implementation
 * uses ConcurrentHashMap and ConcurrentHashMap.newKeySet() to safely handle keybind management
 * from multiple threads without external synchronization.
 * 
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Thread-safe keybind registration and unregistration</li>
 *   <li>GLFW keycode validation</li>
 *   <li>Duplicate ID prevention</li>
 *   <li>Association of custom actions with keybinds</li>
 * </ul>
 * 
 * <p><strong>Example Usage:</strong></p>
 * <pre>
 * {@code
 * DynamicKeyRegistry registry = new DynamicKeyRegistryImpl();
 * Optional<DynamicKeybindAction> action = Optional.of(
 *   new DynamicKeybindAction("mymod:cast_spell", new CompoundTag())
 * );
 * KeyMapping key = registry.registerDynamicKey(
 *   "mymod:ability_cast",
 *   GLFW.GLFW_KEY_Q,
 *   "mymod",
 *   action
 * );
 * }
 * </pre>
 *
 * @see DynamicKeyRegistry
 */
public class DynamicKeyRegistryImpl implements DynamicKeyRegistry {
    /** Map of keybind IDs to their KeyMappings (thread-safe). */
    private final Map<String, KeyMapping> keyBindingsById = new ConcurrentHashMap<>();

    /** Map of KeyMappings to their associated actions (thread-safe). */
    private final Map<KeyMapping, Optional<DynamicKeybindAction>> actionsByKeyBinding = new ConcurrentHashMap<>();

    /** Map of KeyMappings to their stable keybind IDs (thread-safe). */
    private final Map<KeyMapping, String> idByKeyBinding = new ConcurrentHashMap<>();

    /** Map of keybind IDs to their display specs (thread-safe). */
    private final Map<String, DisplaySpec> displaySpecById = new ConcurrentHashMap<>();

    /** Set of all registered keybinds for iteration (thread-safe). */
    private final Set<KeyMapping> allDynamicKeys = ConcurrentHashMap.newKeySet();

    /**
     * Register a new dynamic keybind at runtime.
     *
     * @param id unique identifier for the keybind (e.g., "mymod:ability_cast")
     * @param keyCode the GLFW key code (must be in valid GLFW range)
     * @param category the keybind category for menu organization (e.g., "mymod")
     * @param action optional action to trigger when the key is pressed
     * @return the registered KeyMapping
     * @throws IllegalArgumentException if ID already exists or keyCode is invalid
     */
    @Override
    public KeyMapping registerDynamicKey(String id, int keyCode, String category, Optional<DynamicKeybindAction> action) {
        return registerDynamicKey(id, keyCode, category, action, DisplaySpec.empty());
    }

    /**
     * Register a new dynamic keybind at runtime with custom display metadata.
     *
     * @param id unique identifier for the keybind (e.g., "mymod:ability_cast")
     * @param keyCode the GLFW key code (must be in valid GLFW range)
     * @param category the keybind category for menu organization (e.g., "mymod")
     * @param action optional action to trigger when the key is pressed
     * @param displaySpec custom display metadata for the keybind label
     * @return the registered KeyMapping
     * @throws IllegalArgumentException if ID already exists or keyCode is invalid
     */
    @Override
    public KeyMapping registerDynamicKey(String id, int keyCode, String category, Optional<DynamicKeybindAction> action, DisplaySpec displaySpec) {
        if (keyBindingsById.containsKey(id)) {
            throw new IllegalArgumentException("Keybind with id '" + id + "' already exists");
        }

        if (!isValidKeyCode(keyCode)) {
            throw new IllegalArgumentException("Invalid keycode: " + keyCode + ". Use GLFW key constants (e.g. 32-348).");
        }

        DisplaySpec safeDisplaySpec = displaySpec == null ? DisplaySpec.empty() : displaySpec;
        KeyMapping keyMapping = new DynamicKeyMapping(id, keyCode, category, safeDisplaySpec);

        keyBindingsById.put(id, keyMapping);
        actionsByKeyBinding.put(keyMapping, action);
        idByKeyBinding.put(keyMapping, id);
        displaySpecById.put(id, safeDisplaySpec);
        allDynamicKeys.add(keyMapping);

        return keyMapping;
    }

    /**
     * Validates that a keyCode is within the acceptable GLFW range.
     *
     * @param keyCode the key code to validate
     * @return true if keyCode is valid, false otherwise
     */
    private static boolean isValidKeyCode(int keyCode) {
        return keyCode == InputConstants.UNKNOWN.getValue() || (keyCode >= GLFW.GLFW_KEY_SPACE && keyCode <= GLFW.GLFW_KEY_LAST);
    }

    /**
     * Unregister an existing dynamic keybind.
     * 
     * Removes the keybind from all internal maps, preventing further key presses
     * from triggering its action.
     *
     * @param keyBinding the KeyMapping to unregister
     */
    @Override
    public void unregisterDynamicKey(KeyMapping keyBinding) {
        actionsByKeyBinding.remove(keyBinding);
        allDynamicKeys.remove(keyBinding);
        String id = idByKeyBinding.remove(keyBinding);
        if (id != null) {
            keyBindingsById.remove(id);
            displaySpecById.remove(id);
            return;
        }

        keyBindingsById.entrySet().removeIf(entry -> {
            if (entry.getValue() == keyBinding) {
                displaySpecById.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }

    /**
     * Retrieve all currently registered dynamic keybinds.
     * 
     * @return immutable collection of all dynamic KeyMappings
     */
    @Override
    public Collection<KeyMapping> getAllDynamicKeys() {
        return Collections.unmodifiableCollection(allDynamicKeys);
    }

    /**
     * Retrieve a specific keybind by its ID.
     * 
     * @param id the unique identifier of the keybind
     * @return the KeyMapping if found, or null if not registered
     */
    @Override
    public KeyMapping getKeyBindById(String id) {
        return keyBindingsById.get(id);
    }

    /**
     * Retrieve the action associated with a keybind.
     * 
     * @param keyBinding the KeyMapping to query
     * @return an Optional containing the action, or empty if no action is registered
     */
    @Override
    public Optional<DynamicKeybindAction> getKeyBindAction(KeyMapping keyBinding) {
        return actionsByKeyBinding.getOrDefault(keyBinding, Optional.empty());
    }

    @Override
    public Optional<String> getKeyBindId(KeyMapping keyBinding) {
        return Optional.ofNullable(idByKeyBinding.get(keyBinding));
    }

    /**
     * Get the display metadata for a registered keybind.
     *
     * @param id the keybind ID
     * @return the DisplaySpec if registered, or an empty one if not found
     */
    public Optional<DisplaySpec> getDisplaySpec(String id) {
        DisplaySpec spec = displaySpecById.getOrDefault(id, DisplaySpec.empty());
        return Optional.of(spec);
    }
}
