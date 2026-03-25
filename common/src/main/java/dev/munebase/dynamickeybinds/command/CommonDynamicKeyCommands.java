package dev.munebase.dynamickeybinds.command;

import dev.munebase.dynamickeybinds.DynamicKeyRegistry;
import dev.munebase.dynamickeybinds.DynamicKeyRegistryProvider;
import dev.munebase.dynamickeybinds.util.KeyMappingUtil;
import net.minecraft.client.KeyMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common command logic for dynamic keybinds that works across all loaders.
 * This handles add/list/remove operations independent of the mod loader.
 */
public final class CommonDynamicKeyCommands {
    private static final Logger LOGGER = LoggerFactory.getLogger("DynamicKeybinds");

    private CommonDynamicKeyCommands() {
    }

    /**
     * Adds a dynamic keybind to the registry with an action.
     * @param id Unique identifier for the keybind
     * @param keyCode GLFW key code
     * @param category Category for organizing in the keybinds menu
     * @param action the action to execute when key is pressed (optional)
     * @param onError Called with error message if registration fails
     * @return The registered KeyMapping, or null if registration failed
     */
    public static KeyMapping addKeybind(String id, int keyCode, String category, java.util.Optional<dev.munebase.dynamickeybinds.action.DynamicKeybindAction> action,
                                        java.util.function.Consumer<String> onError) {
        DynamicKeyRegistry registry = DynamicKeyRegistryProvider.getRegistry();
        try {
            KeyMapping keyMapping = registry.registerDynamicKey(id, keyCode, category, action);
            LOGGER.info("Registered keybind: {}", id);
            return keyMapping;
        } catch (IllegalArgumentException ex) {
            LOGGER.error("Failed to register keybind: {}", id, ex);
            onError.accept(ex.getMessage());
            return null;
        }
    }

    /**
     * Lists all currently registered dynamic keybinds.
     * @return List of keybind names
     */
    public static java.util.List<String> listKeybinds() {
        DynamicKeyRegistry registry = DynamicKeyRegistryProvider.getRegistry();
        return registry.getAllDynamicKeys().stream()
            .map(KeyMapping::getName)
            .toList();
    }

    /**
     * Formats user-facing lines for the `list` command output.
     *
     * @return display lines in render order
     */
    public static java.util.List<String> formatListOutput() {
        var keybinds = listKeybinds();
        if (keybinds.isEmpty()) {
            return java.util.List.of("No dynamic keybinds registered.");
        }

        java.util.List<String> lines = new java.util.ArrayList<>();
        lines.add("Dynamic keybinds:");
        for (String keybind : keybinds) {
            lines.add("- " + keybind);
        }
        return lines;
    }

    /**
     * User-facing add request message.
     *
     * @param id keybind id
     * @return formatted message
     */
    public static String formatAddRequestMessage(String id) {
        return "Requesting server to add dynamic keybind: " + id;
    }

    /**
     * User-facing networking not initialized message.
     *
     * @return formatted message
     */
    public static String formatNetworkingNotInitializedMessage() {
        return "Error: Networking not initialized";
    }

    /**
     * User-facing remove request message.
     *
     * @param id keybind id
     * @return formatted message
     */
    public static String formatRemoveRequestMessage(String id) {
        return "Requesting server to remove dynamic keybind: " + id;
    }

    /**
     * Computes Brigadier result for list output lines.
     *
     * @param lines list output lines
     * @return 0 when empty-state message is displayed; otherwise key count
     */
    public static int listResultCode(java.util.List<String> lines) {
        return lines.size() == 1 && "No dynamic keybinds registered.".equals(lines.get(0)) ? 0 : lines.size() - 1;
    }

    /**
     * Removes a dynamic keybind from the registry.
     * @param id Identifier of the keybind to remove
     * @param onError Called with error message if removal fails
     * @return The removed KeyMapping, or null if not found
     */
    public static KeyMapping removeKeybind(String id, java.util.function.Consumer<String> onError) {
        DynamicKeyRegistry registry = DynamicKeyRegistryProvider.getRegistry();
        KeyMapping keyBinding = registry.getKeyBindById(id);
        if (keyBinding == null) {
            onError.accept("Keybind not found: " + id);
            return null;
        }

        registry.unregisterDynamicKey(keyBinding);
        LOGGER.info("Unregistered keybind: {}", id);
        return keyBinding;
    }

    /**
     * Extracts keycode from a KeyMapping.
     *
     * @param keyMapping The keymapping to extract from
     * @return The GLFW key code, or 0 if extraction fails
     */
    public static int extractKeycodeFromKeyMapping(KeyMapping keyMapping) {
        try {
            return KeyMappingUtil.extractKeyCode(keyMapping);
        } catch (Throwable e) {
            LOGGER.warn("Could not extract keycode from KeyMapping", e);
        }
        return 0;
    }
}
