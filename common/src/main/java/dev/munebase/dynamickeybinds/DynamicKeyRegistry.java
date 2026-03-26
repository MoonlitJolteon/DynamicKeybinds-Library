package dev.munebase.dynamickeybinds;

import net.minecraft.client.KeyMapping;
import java.util.Collection;
import java.util.Optional;

import dev.munebase.dynamickeybinds.action.DynamicKeybindAction;

/**
 * Registry for dynamically created keybinds at runtime.
 * 
 * This interface provides the core API for managing dynamic keybinds in the mod.
 * Implementations maintain a record of all keybinds and their associated actions,
 * allowing third-party mods to create, retrieve, and remove keybinds on-the-fly.
 * 
 * <p><strong>Thread Safety:</strong> Implementations must be thread-safe as keybinds
 * may be registered/unregistered from different threads.</p>
 * 
 * <p><strong>Example Usage:</strong></p>
 * <pre>
 * {@code
 * DynamicKeyRegistry registry = DynamicKeyRegistryProvider.getRegistry();
 * CompoundTag actionData = new CompoundTag();
 * actionData.putString("target", "someValue");
 * 
 * KeyMapping keyMapping = registry.registerDynamicKey(
 *   "mymod:key_id",
 *   GLFW.GLFW_KEY_K,
 *   "mymod",
 *   Optional.of(new DynamicKeybindAction("mymod:action_id", actionData))
 * );
 * }
 * </pre>
 * 
 * @see DynamicKeyRegistryImpl
 * @see DynamicKeybindAction
 */
public interface DynamicKeyRegistry {
    /**
     * Register a new dynamic key at runtime.
     *
     * @param id unique identifier for the keybind (e.g., "mymod:key_name")
     * @param keyCode the GLFW key code (e.g., GLFW.GLFW_KEY_K)
     * @param category the keybind category for menu organization
     * @param action the action to trigger when pressed (optional)
     * @return the registered KeyMapping
     * @throws IllegalArgumentException if the ID already exists or keyCode is invalid
     */
    KeyMapping registerDynamicKey(String id, int keyCode, String category, Optional<DynamicKeybindAction> action);

    /**
     * Unregister an existing dynamic key.
     *
     * @param keyBinding the key binding to unregister
     */
    void unregisterDynamicKey(KeyMapping keyBinding);

    /**
     * Unregister an existing dynamic key by ID.
     *
     * @param id keybind identifier
     * @return true when a keybind with the given ID existed and was removed
     */
    default boolean unregisterDynamicKey(String id) {
        KeyMapping keyBinding = getKeyBindById(id);
        if (keyBinding == null) {
            return false;
        }
        unregisterDynamicKey(keyBinding);
        return true;
    }

    /**
     * Get all currently registered dynamic keys.
     *
     * @return immutable collection of all dynamic key bindings
     */
    Collection<KeyMapping> getAllDynamicKeys();

    /**
     * Get a specific keybind by its ID.
     *
     * @param id the keybind identifier
     * @return the KeyMapping, or null if not found
     */
    KeyMapping getKeyBindById(String id);

    /**
     * Get the action associated with a keybind.
     *
     * @param keyBinding the key mapping to query
     * @return the action, or empty if not set
     */
    Optional<DynamicKeybindAction> getKeyBindAction(KeyMapping keyBinding);
}
