package dev.munebase.dynamickeybinds.model;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/**
 * Represents a single argument that can be used in dynamic keybind display strings.
 * 
 * Supports typed arguments: STRING, INT, TRANSLATION_KEY, COMPONENT.
 * Used in conjunction with {@link DisplaySpec} to provide dynamic values for
 * translation key formatting.
 */
public sealed interface DisplayArg {
    
    /**
     * Serialize this DisplayArg to NBT.
     */
    CompoundTag toNbt();
    
    /**
     * Get the type ID of this argument for serialization.
     */
    int getTypeId();
    
    /**
     * Get the raw object value of this argument (String, Integer, etc).
     */
    Object getValue();
    
    /**
     * Deserialize a DisplayArg from NBT.
     */
    static DisplayArg fromNbt(CompoundTag tag) {
        int typeId = tag.getInt("type");
        return switch (typeId) {
            case 1 -> new StringArg(tag.getString("value"));
            case 2 -> new IntArg(tag.getInt("value"));
            case 3 -> new TranslationKeyArg(tag.getString("value"));
            default -> throw new IllegalArgumentException("Unknown DisplayArg type: " + typeId);
        };
    }
    
    /**
     * A string argument.
     */
    record StringArg(String value) implements DisplayArg {
        @Override
        public CompoundTag toNbt() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("type", 1);
            tag.putString("value", value);
            return tag;
        }
        
        @Override
        public int getTypeId() {
            return 1;
        }
        
        @Override
        public Object getValue() {
            return value;
        }
    }
    
    /**
     * An integer argument.
     */
    record IntArg(int value) implements DisplayArg {
        @Override
        public CompoundTag toNbt() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("type", 2);
            tag.putInt("value", value);
            return tag;
        }
        
        @Override
        public int getTypeId() {
            return 2;
        }
        
        @Override
        public Object getValue() {
            return value;
        }
    }
    
    /**
     * A translation key argument (for nested translations).
     */
    record TranslationKeyArg(String translationKey) implements DisplayArg {
        @Override
        public CompoundTag toNbt() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("type", 3);
            tag.putString("value", translationKey);
            return tag;
        }
        
        @Override
        public int getTypeId() {
            return 3;
        }
        
        @Override
        public Object getValue() {
            return translationKey;
        }
    }
}
