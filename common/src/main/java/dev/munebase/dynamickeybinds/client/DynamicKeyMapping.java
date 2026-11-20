package dev.munebase.dynamickeybinds.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.munebase.dynamickeybinds.model.DisplaySpec;
import net.minecraft.client.KeyMapping;

/**
 * KeyMapping with optional dynamic display metadata.
 */
public class DynamicKeyMapping extends KeyMapping {
    private DisplaySpec displaySpec;

    public DynamicKeyMapping(String id, int keyCode, String category, DisplaySpec displaySpec) {
        super("key." + id, InputConstants.Type.KEYSYM, keyCode, categoryKey(category));
        this.displaySpec = displaySpec == null ? DisplaySpec.empty() : displaySpec;
    }

    /**
     * The KeyMapping category string used for a dynamic keybind category.
     */
    public static String categoryKey(String category) {
        return "category." + category;
    }

    public DisplaySpec getDisplaySpec() {
        return displaySpec;
    }

    public void setDisplaySpec(DisplaySpec displaySpec) {
        this.displaySpec = displaySpec == null ? DisplaySpec.empty() : displaySpec;
    }
}
