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
    private static volatile Field resolvedKeyMappingsField;

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
            Field field = resolveKeyMappingsField(logger);
            if (field == null) {
                return;
            }
            field.set(minecraft.options, updated);
        } catch (Throwable e) {
            logger.error("Could not update runtime key mappings", e);
        }
    }

    private static Field resolveKeyMappingsField(Logger logger) {
        Field cached = resolvedKeyMappingsField;
        if (cached != null) {
            return cached;
        }

        Class<?> optionsClass = net.minecraft.client.Options.class;
        String[] candidates = new String[] {"keyMappings", "allKeys", "field_1843", "f_92059_"};

        for (String candidate : candidates) {
            try {
                Field field = optionsClass.getDeclaredField(candidate);
                if (field.getType().isArray() && field.getType().getComponentType() == KeyMapping.class) {
                    field.setAccessible(true);
                    resolvedKeyMappingsField = field;
                    return field;
                }
            } catch (NoSuchFieldException ignored) {
            }
        }

        Field discovered = null;
        for (Field field : optionsClass.getDeclaredFields()) {
            if (field.getType().isArray() && field.getType().getComponentType() == KeyMapping.class) {
                if (discovered != null) {
                    logger.error("Could not resolve runtime key mappings field: multiple KeyMapping[] fields found in Options");
                    return null;
                }
                discovered = field;
            }
        }

        if (discovered == null) {
            logger.error("Could not resolve runtime key mappings field: no KeyMapping[] field found in Options");
            return null;
        }

        discovered.setAccessible(true);
        resolvedKeyMappingsField = discovered;
        return discovered;
    }
}
