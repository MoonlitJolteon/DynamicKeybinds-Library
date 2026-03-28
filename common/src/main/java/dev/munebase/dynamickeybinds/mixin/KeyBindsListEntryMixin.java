package dev.munebase.dynamickeybinds.mixin;

import dev.munebase.dynamickeybinds.client.DynamicKeyMapping;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.controls.KeyBindsList;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyBindsList.KeyEntry.class)
public abstract class KeyBindsListEntryMixin {

    @Shadow
    @Final
    private KeyMapping key;

    @Shadow
    @Final
    @Mutable
    private Component name;

    /**
     * Replaces only the left-side label component after entry construction.
     *
     * The key button text is unaffected because this only updates the KeyEntry
     * label field (`name`) and does not touch bound-key message generation.
     */
    @Inject(method = "<init>", at = @At("TAIL"))
    private void useDisplaySpecLabel(KeyBindsList list, KeyMapping keyMapping, Component originalName, CallbackInfo ci) {
        if (this.key instanceof DynamicKeyMapping dynamicKeyMapping) {
            this.name = dynamicKeyMapping.getDisplaySpec().toComponent().orElse(originalName);
        }
    }
}