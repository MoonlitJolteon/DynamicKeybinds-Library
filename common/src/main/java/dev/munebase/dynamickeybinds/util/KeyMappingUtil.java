package dev.munebase.dynamickeybinds.util;

import com.mojang.blaze3d.platform.InputConstants;
import dev.munebase.dynamickeybinds.persistence.StoredKey;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/**
 * Shared key mapping helpers used across loaders.
 */
public final class KeyMappingUtil {
    private KeyMappingUtil() {
    }

    /**
     * Converts a key mapping name/id to the canonical keybind id used by persistence.
     *
     * @param rawId key mapping name or id
     * @return normalized id without `key.` prefix
     */
    public static String normalizeId(String rawId) {
        if (rawId == null) {
            return "";
        }
        if (rawId.startsWith("key.")) {
            return rawId.substring(4);
        }
        return rawId;
    }

    /**
     * Normalizes key category for storage.
     *
     * @param category raw category
     * @return normalized category
     */
    public static String normalizeCategory(String category) {
        if (category != null && category.startsWith("category.")) {
            category = category.substring("category.".length());
        }
        if (category == null || category.isBlank()) {
            return "misc";
        }
        return category;
    }

    /**
     * Converts a stored input to a game key, or {@link InputConstants#UNKNOWN} (unbound) if its type is
     * unknown or its code is out of range for that type.
     */
    public static InputConstants.Key toKey(StoredKey stored) {
        int code = stored.code();
        return switch (stored.type()) {
            case StoredKey.KEYSYM -> code == -1 || (code >= GLFW.GLFW_KEY_SPACE && code <= GLFW.GLFW_KEY_LAST)
                ? InputConstants.Type.KEYSYM.getOrCreate(code) : InputConstants.UNKNOWN;
            case StoredKey.MOUSE -> code >= 0 && code <= GLFW.GLFW_MOUSE_BUTTON_LAST
                ? InputConstants.Type.MOUSE.getOrCreate(code) : InputConstants.UNKNOWN;
            case StoredKey.SCANCODE -> code >= 0 ? InputConstants.Type.SCANCODE.getOrCreate(code) : InputConstants.UNKNOWN;
            default -> InputConstants.UNKNOWN;
        };
    }

    /** The input a key mapping is currently bound to, with its type. */
    public static StoredKey toStored(KeyMapping keyMapping) {
        InputConstants.Key key = InputConstants.getKey(keyMapping.saveString());
        String type = switch (key.getType()) {
            case MOUSE -> StoredKey.MOUSE;
            case SCANCODE -> StoredKey.SCANCODE;
            default -> StoredKey.KEYSYM;
        };
        return new StoredKey(type, key.getValue());
    }

    /**
     * Extracts key code from key mapping in a mapping-stable way.
     *
     * @param keyMapping key mapping
     * @return GLFW key code, or 0 on failure
     */
    public static int extractKeyCode(KeyMapping keyMapping) {
        String serializedKey = keyMapping.saveString();
        if (serializedKey != null && !serializedKey.isBlank()) {
            return com.mojang.blaze3d.platform.InputConstants.getKey(serializedKey).getValue();
        }
        return 0;
    }
}
