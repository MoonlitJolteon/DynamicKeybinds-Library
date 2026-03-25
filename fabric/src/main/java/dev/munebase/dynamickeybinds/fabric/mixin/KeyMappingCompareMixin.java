package dev.munebase.dynamickeybinds.fabric.mixin;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Mixin override for key mapping ordering that tolerates missing runtime map entries.
 */
@Mixin(KeyMapping.class)
public class KeyMappingCompareMixin {
    private static final Field MAP_FIELD = resolveMapField();

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
     * Compares key mappings using MAP indices and falls back to name ordering when needed.
     *
     * This mixin ensures runtime key mappings without MAP entries still compare safely.
     * @author Moonlit Productions
     * @param other other key mapping to compare against
     * @return comparison result used for ordering
     */
    @Overwrite
    public int compareTo(KeyMapping other) {
        try {
            if (MAP_FIELD == null) {
                return compareNames();
            }

            @SuppressWarnings("unchecked")
            Map<String, Integer> keyMap = (Map<String, Integer>) MAP_FIELD.get(null);
            if (keyMap == null) {
                return compareNames();
            }

            KeyMapping thisMapping = (KeyMapping) (Object) this;
            String thisName = thisMapping.getName();
            String otherName = other.getName();
            
            Integer thisIndex = keyMap.get(thisName);
            Integer otherIndex = keyMap.get(otherName);

            // Ensure both have entries in the map before comparison
            if (thisIndex == null || otherIndex == null) {
                synchronizeMapEntry(keyMap, thisName);
                synchronizeMapEntry(keyMap, otherName);
                
                // Re-fetch after synchronization
                thisIndex = keyMap.get(thisName);
                otherIndex = keyMap.get(otherName);
                
                // If still missing, fall back to name comparison
                if (thisIndex == null || otherIndex == null) {
                    return compareNames();
                }
            }

            return thisIndex.compareTo(otherIndex);
        } catch (Throwable e) {
            return compareNames();
        }
    }

    /**
     * Fallback comparison by key name.
     *
     * @return comparison result
     */
    private int compareNames() {
        KeyMapping thisMapping = (KeyMapping) (Object) this;
        return thisMapping.getName().compareTo(((KeyMapping) (Object) this).getName());
    }

    /**
     * Ensures a key name exists in the index map by assigning a new trailing index.
     *
     * @param keyMap key index map
     * @param name key name
     */
    private static void synchronizeMapEntry(Map<String, Integer> keyMap, String name) {
        try {
            if (!keyMap.containsKey(name)) {
                // Assign a high index to maintain existing order
                int maxIndex = keyMap.values().stream().mapToInt(Integer::intValue).max().orElse(-1);
                keyMap.put(name, maxIndex + 1);
            }
        } catch (Throwable ignored) {
        }
    }
}


