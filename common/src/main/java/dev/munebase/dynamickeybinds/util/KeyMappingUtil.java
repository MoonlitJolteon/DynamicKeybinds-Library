package dev.munebase.dynamickeybinds.util;

import net.minecraft.client.KeyMapping;

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
