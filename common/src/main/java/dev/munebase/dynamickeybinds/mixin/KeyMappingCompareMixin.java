package dev.munebase.dynamickeybinds.mixin;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/**
 * Makes key mapping ordering safe for categories created at runtime.
 *
 * <p>Vanilla looks every category up in {@code CATEGORY_SORT_ORDER}, which only knows categories
 * registered at startup, so comparing a dynamic keybind throws a NullPointerException. We register
 * unknown categories up front instead of overwriting {@code compareTo}, so other mods (e.g. Essential)
 * can still inject into it.
 */
@Mixin(KeyMapping.class)
public abstract class KeyMappingCompareMixin {
    @Shadow
    @Final
    private static Map<String, Integer> CATEGORY_SORT_ORDER;

    @Shadow
    public abstract String getCategory();

    @Inject(method = "compareTo(Lnet/minecraft/client/KeyMapping;)I", at = @At("HEAD"))
    private void dynamickeybinds$registerCategories(KeyMapping other, CallbackInfoReturnable<Integer> cir) {
        dynamickeybinds$register(this.getCategory());
        dynamickeybinds$register(other.getCategory());
    }

    private static void dynamickeybinds$register(String category) {
        CATEGORY_SORT_ORDER.putIfAbsent(category, CATEGORY_SORT_ORDER.size() + 1);
    }
}
