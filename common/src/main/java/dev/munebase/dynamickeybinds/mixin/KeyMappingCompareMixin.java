package dev.munebase.dynamickeybinds.mixin;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

/**
 * Makes key mapping ordering safe for categories created at runtime.
 *
 * <p>Vanilla looks every category up in {@code CATEGORY_SORT_ORDER}, which only knows categories
 * registered at startup, so comparing a dynamic keybind throws a NullPointerException.
 */
@Mixin(KeyMapping.class)
public abstract class KeyMappingCompareMixin {
    @Shadow
    @Final
    private static Map<String, Integer> CATEGORY_SORT_ORDER;

    @Shadow
    public abstract String getName();

    @Shadow
    public abstract String getCategory();

    /**
     * Orders by category (known categories first, in vanilla order), then by key name.
     *
     * @author Moonlit Productions
     * @reason Vanilla throws for categories missing from CATEGORY_SORT_ORDER, which dynamic keybinds create.
     */
    @Overwrite
    public int compareTo(KeyMapping other) {
        int categoryComparison = compareCategories(this.getCategory(), other.getCategory());
        if (categoryComparison != 0) {
            return categoryComparison;
        }
        return this.getName().compareTo(other.getName());
    }

    private static int compareCategories(String category, String otherCategory) {
        Integer order = CATEGORY_SORT_ORDER.get(category);
        Integer otherOrder = CATEGORY_SORT_ORDER.get(otherCategory);

        if (order != null && otherOrder != null) {
            return order.compareTo(otherOrder);
        }
        if (order != null) {
            return -1;
        }
        if (otherOrder != null) {
            return 1;
        }
        return category.compareTo(otherCategory);
    }
}
