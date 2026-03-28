package dev.munebase.dynamickeybinds.mixin;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.Map;

/**
 * Mixin override for key mapping ordering that tolerates missing runtime map entries.
 */
@Mixin(KeyMapping.class)
public class KeyMappingCompareMixin {
    private static final Field MAP_FIELD = resolveMapField();
    private static final Field CATEGORY_SORT_ORDER_FIELD = resolveCategorySortOrderField();

    private static Field resolveMapField() {
        try {
            Field field = KeyMapping.class.getDeclaredField("MAP");
            field.setAccessible(true);
            return field;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Field resolveCategorySortOrderField() {
        try {
            Field field = KeyMapping.class.getDeclaredField("CATEGORY_SORT_ORDER");
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
     *
     * @author Moonlit Productions
    * Reason: Ensures runtime-created key mappings without MAP entries can still be sorted safely.
     * @param other other key mapping to compare against
     * @return comparison result used for ordering
     */
    @Overwrite
    public int compareTo(KeyMapping other) {
        try {
            KeyMapping thisMapping = (KeyMapping) (Object) this;

            int categoryComparison = compareCategories(thisMapping, other);
            if (categoryComparison != 0) {
                return categoryComparison;
            }

            if (MAP_FIELD == null) {
                return compareNames(other);
            }

            @SuppressWarnings("unchecked")
            Map<String, Integer> keyMap = (Map<String, Integer>) MAP_FIELD.get(null);
            if (keyMap == null) {
                return compareNames(other);
            }

            String thisName = thisMapping.getName();
            String otherName = other.getName();

            Integer thisIndex = keyMap.get(thisName);
            Integer otherIndex = keyMap.get(otherName);

            if (thisIndex == null || otherIndex == null) {
                synchronizeMapEntry(keyMap, thisName);
                synchronizeMapEntry(keyMap, otherName);

                thisIndex = keyMap.get(thisName);
                otherIndex = keyMap.get(otherName);

                if (thisIndex == null || otherIndex == null) {
                    return compareNames(other);
                }
            }

            return thisIndex.compareTo(otherIndex);
        } catch (Throwable e) {
            return compareNames(other);
        }
    }

    /**
     * Fallback comparison by key name.
     *
     * @return comparison result
     */
    private int compareNames(KeyMapping other) {
        KeyMapping thisMapping = (KeyMapping) (Object) this;
        return thisMapping.getName().compareTo(other.getName());
    }

    private static int compareCategories(KeyMapping thisMapping, KeyMapping other) {
        try {
            if (CATEGORY_SORT_ORDER_FIELD != null) {
                @SuppressWarnings("unchecked")
                Map<String, Integer> categorySortOrder = (Map<String, Integer>) CATEGORY_SORT_ORDER_FIELD.get(null);
                if (categorySortOrder != null) {
                    Integer thisCategory = categorySortOrder.get(thisMapping.getCategory());
                    Integer otherCategory = categorySortOrder.get(other.getCategory());
                    if (thisCategory != null && otherCategory != null) {
                        return thisCategory.compareTo(otherCategory);
                    }
                    if (thisCategory == null && otherCategory == null) {
                        return thisMapping.getCategory().compareTo(other.getCategory());
                    }
                    return Comparator.nullsLast(Integer::compareTo).compare(thisCategory, otherCategory);
                }
            }
        } catch (Throwable ignored) {
        }

        return thisMapping.getCategory().compareTo(other.getCategory());
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
                int maxIndex = keyMap.values().stream().mapToInt(Integer::intValue).max().orElse(-1);
                keyMap.put(name, maxIndex + 1);
            }
        } catch (Throwable ignored) {
        }
    }
}