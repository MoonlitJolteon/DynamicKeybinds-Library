package dev.munebase.dynamickeybinds.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * Loader-agnostic runtime key mapping registration helpers.
 */
public final class RuntimeKeyMappingManager {
    private RuntimeKeyMappingManager() {
    }

    /**
     * Adds a runtime key mapping to options if not already present.
     *
     * @param keyMapping key mapping
     * @param logger logger for diagnostics
     */
    public static void registerRuntimeKey(KeyMapping keyMapping, Logger logger) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options == null) {
            return;
        }

        KeyMapping[] current = minecraft.options.keyMappings;
        boolean alreadyPresent = Arrays.stream(current)
            .anyMatch(existing -> existing == keyMapping || existing.getName().equals(keyMapping.getName()));

        if (alreadyPresent) {
            return;
        }

        KeyMapping[] updated = Arrays.copyOf(current, current.length + 1);
        updated[current.length] = keyMapping;
        applyMappings(minecraft, updated, logger);
        KeyMapping.resetMapping();
    }

    /**
     * Removes a runtime key mapping from options.
     *
     * @param keyMapping key mapping
     * @param logger logger for diagnostics
     */
    public static void unregisterRuntimeKey(KeyMapping keyMapping, Logger logger) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options == null) {
            return;
        }

        KeyMapping[] current = minecraft.options.keyMappings;
        KeyMapping[] updated = Arrays.stream(current)
            .filter(existing -> existing != keyMapping && !existing.getName().equals(keyMapping.getName()))
            .toArray(KeyMapping[]::new);

        if (updated.length != current.length) {
            applyMappings(minecraft, updated, logger);
            KeyMapping.resetMapping();
        }
    }

    private static void applyMappings(Minecraft minecraft, KeyMapping[] updated, Logger logger) {
        try {
            Field field = net.minecraft.client.Options.class.getDeclaredField("keyMappings");
            field.setAccessible(true);
            field.set(minecraft.options, updated);
        } catch (Throwable e) {
            logger.error("Could not update runtime key mappings", e);
        }
    }
}
