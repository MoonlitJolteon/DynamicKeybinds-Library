package dev.munebase.dynamickeybinds.model;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Specification for how a dynamic keybind should be displayed in the UI.
 * 
 * Contains:
 * - translationKey: optional translation key (e.g., "key.mymod.my_instance")
 * - fallback: optional fallback text if translation key is not found
 * - args: optional list of typed arguments for translation string formatting
 * 
 * If empty (no translationKey), the keybind will use its default display (typically "key." + id).
 */
public record DisplaySpec(
    Optional<String> translationKey,
    Optional<String> fallback,
    List<DisplayArg> args
) {
    private static final Logger LOGGER = LoggerFactory.getLogger("DynamicKeybinds");
    
    public DisplaySpec {
        // Defensive copy of args list
        args = List.copyOf(args);
    }
    
    /**
     * Create an empty DisplaySpec (will use default display).
     */
    public static DisplaySpec empty() {
        return new DisplaySpec(Optional.empty(), Optional.empty(), List.of());
    }
    
    /**
     * Create a DisplaySpec with only a translation key.
     */
    public static DisplaySpec ofTranslationKey(String translationKey) {
        return new DisplaySpec(Optional.of(translationKey), Optional.empty(), List.of());
    }
    
    /**
     * Create a DisplaySpec with translation key and fallback.
     */
    public static DisplaySpec ofTranslationKeyWithFallback(String translationKey, String fallback) {
        return new DisplaySpec(Optional.of(translationKey), Optional.of(fallback), List.of());
    }
    
    /**
     * Create a DisplaySpec with translation key, fallback, and arguments.
     */
    public static DisplaySpec ofTranslationKeyWithFallbackAndArgs(String translationKey, String fallback, List<DisplayArg> args) {
        return new DisplaySpec(Optional.of(translationKey), Optional.of(fallback), args);
    }
    
    /**
     * Convert this DisplaySpec to a Component for rendering.
     * If translationKey is present, returns a translatable component.
     * Otherwise returns empty (caller should use default).
     */
    public Optional<Component> toComponent() {
        if (translationKey.isEmpty()) {
            return Optional.empty();
        }
        
        String key = translationKey.get();
        Object[] argArray = args.stream().map(DisplayArg::getValue).toArray();
        
        if (fallback.isPresent()) {
            return Optional.of(Component.translatableWithFallback(key, fallback.get(), argArray));
        } else {
            return Optional.of(Component.translatable(key, argArray));
        }
    }
    
    /**
     * Serialize this DisplaySpec to NBT.
     */
    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        
        if (translationKey.isPresent()) {
            tag.putString("translationKey", translationKey.get());
        }
        
        if (fallback.isPresent()) {
            tag.putString("fallback", fallback.get());
        }
        
        if (!args.isEmpty()) {
            ListTag argsList = new ListTag();
            for (DisplayArg arg : args) {
                argsList.add(arg.toNbt());
            }
            tag.put("args", argsList);
        }
        
        return tag;
    }
    
    /**
     * Deserialize a DisplaySpec from NBT.
     * If tag is empty or null, returns empty DisplaySpec.
     */
    public static DisplaySpec fromNbt(CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            return empty();
        }
        
        Optional<String> translationKey = tag.contains("translationKey") 
            ? Optional.of(tag.getString("translationKey"))
            : Optional.empty();
        
        Optional<String> fallback = tag.contains("fallback")
            ? Optional.of(tag.getString("fallback"))
            : Optional.empty();
        
        List<DisplayArg> args = new ArrayList<>();
        if (tag.contains("args", Tag.TAG_LIST)) {
            ListTag argsList = tag.getList("args", Tag.TAG_COMPOUND);
            for (int i = 0; i < argsList.size(); i++) {
                try {
                    args.add(DisplayArg.fromNbt(argsList.getCompound(i)));
                } catch (Exception e) {
                    LOGGER.warn("Error deserializing DisplayArg at index {}", i, e);
                }
            }
        }
        
        return new DisplaySpec(translationKey, fallback, args);
    }
    
    /**
     * Check if this DisplaySpec is effectively empty.
     */
    public boolean isEmpty() {
        return translationKey.isEmpty();
    }
}
