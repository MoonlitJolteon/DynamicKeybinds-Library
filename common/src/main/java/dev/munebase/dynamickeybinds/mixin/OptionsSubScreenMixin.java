package dev.munebase.dynamickeybinds.mixin;

import dev.munebase.dynamickeybinds.client.CommonClientKeybindPersistence;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Syncs dynamic key rebinds to the server only when the Controls screen closes,
 * so temporary rebinds made by other mods are never persisted.
 */
@Mixin(OptionsSubScreen.class)
public class OptionsSubScreenMixin {
    @Inject(method = "removed", at = @At("HEAD"))
    private void dynamickeybinds$onRemoved(CallbackInfo ci) {
        // KeyBindsScreen doesn't override removed(), so hook its parent and filter
        if ((Object) this instanceof KeyBindsScreen) {
            CommonClientKeybindPersistence.requestRebindSync();
        }
    }
}
