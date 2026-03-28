package dev.munebase.dynamickeybinds.mixin;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.controls.KeyBindsList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

/**
 * Mixin that ensures runtime-added keybindings are represented in KeyMapping MAP/ALL
 * before the controls list initializes.
 */
@Mixin(KeyBindsList.class)
public class KeyBindsListMixin {
    private static final Field MAP_FIELD = resolveMapField();
    private static final Field ALL_FIELD = resolveAllField();

    /**
     * Resolves the private static {@code KeyMapping.MAP} field via reflection.
     */
    private static Field resolveMapField() {
        try {
            Field field = KeyMapping.class.getDeclaredField("MAP");
            field.setAccessible(true);
            return field;
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Resolves the private static {@code KeyMapping.ALL} field via reflection.
     */
    private static Field resolveAllField() {
        try {
            Field field = KeyMapping.class.getDeclaredField("ALL");
            field.setAccessible(true);
            return field;
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Injects into list initialization to backfill missing key map indexes.
     */
    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        populateMapForAllKeybindings();
    }

    /**
     * Populates missing entries in the key mapping index table for all mappings.
     */
    private static void populateMapForAllKeybindings() {
        try {
            if (MAP_FIELD == null || ALL_FIELD == null) {
                return;
            }

            @SuppressWarnings("unchecked")
            Collection<KeyMapping> allMappings = (Collection<KeyMapping>) ALL_FIELD.get(null);
            @SuppressWarnings("unchecked")
            Map<String, Integer> keyMap = (Map<String, Integer>) MAP_FIELD.get(null);

            if (keyMap != null && allMappings != null) {
                int maxIndex = -1;
                for (Integer index : keyMap.values()) {
                    if (index > maxIndex) {
                        maxIndex = index;
                    }
                }

                int nextIndex = maxIndex + 1;
                for (KeyMapping keyMapping : allMappings) {
                    String name = keyMapping.getName();
                    if (!keyMap.containsKey(name)) {
                        keyMap.put(name, nextIndex++);
                    }
                }
            }
        } catch (Throwable e) {
        }
    }
}