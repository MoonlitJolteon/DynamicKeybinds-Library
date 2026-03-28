package dev.munebase.dynamickeybinds.network;

import dev.munebase.dynamickeybinds.action.DynamicKeybindAction;
import dev.munebase.dynamickeybinds.model.DisplaySpec;

import java.util.Optional;

/**
 * Network packet sent from client to server to request adding a new keybind.
 * 
 * This packet allows clients to register new dynamic keybinds on the server side,
 * which are then persisted and synchronized to all players. It carries the
 * keybind ID, key code, category, action, and display metadata.
 * 
 * <p><strong>Direction:</strong> Client -> Server</p>
 * <p><strong>Handler:</strong> Server-side keybind handler (ForgeServerKeybindHandler or FabricServerKeybindHandler)</p>
 */
public class AddKeybindPacket {
    /** Unique identifier for the keybind (e.g., "mymod:ability_cast") */
    private final String id;
    
    /** GLFW key code for the keybind (e.g., GLFW.GLFW_KEY_Q = 81) */
    private final int keyCode;
    
    /** Category for menu organization (e.g., "mymod") */
    private final String category;

    /** Optional action to execute when the keybind is triggered. */
    private final Optional<DynamicKeybindAction> action;

    /** Optional display metadata for the keybind label. */
    private final DisplaySpec displaySpec;

    /**
     * Construct a new AddKeybindPacket.
     *
     * @param id unique keybind identifier
     * @param keyCode the GLFW key code
     * @param category the keybind category
     */
    public AddKeybindPacket(String id, int keyCode, String category) {
        this(id, keyCode, category, Optional.empty(), DisplaySpec.empty());
    }

    /**
     * Construct a new AddKeybindPacket with an optional action.
     *
     * @param id unique keybind identifier
     * @param keyCode the GLFW key code
     * @param category the keybind category
     * @param action optional action payload
     */
    public AddKeybindPacket(String id, int keyCode, String category, Optional<DynamicKeybindAction> action) {
        this(id, keyCode, category, action, DisplaySpec.empty());
    }

    /**
     * Construct a new AddKeybindPacket with action and display metadata.
     *
     * @param id unique keybind identifier
     * @param keyCode the GLFW key code
     * @param category the keybind category
     * @param action optional action payload
     * @param displaySpec optional display metadata
     */
    public AddKeybindPacket(String id, int keyCode, String category, Optional<DynamicKeybindAction> action, DisplaySpec displaySpec) {
        this.id = id;
        this.keyCode = keyCode;
        this.category = category;
        this.action = action == null ? Optional.empty() : action;
        this.displaySpec = displaySpec == null ? DisplaySpec.empty() : displaySpec;
    }

    /**
     * Get the keybind ID.
     *
     * @return the unique identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Get the key code.
     *
     * @return the GLFW key code
     */
    public int getKeyCode() {
        return keyCode;
    }

    /**
     * Get the keybind category.
     *
     * @return the category string
     */
    public String getCategory() {
        return category;
    }

    /**
     * Get the optional action payload.
     *
     * @return optional action
     */
    public Optional<DynamicKeybindAction> getAction() {
        return action;
    }

    /**
     * Get the display metadata.
     *
     * @return display spec
     */
    public DisplaySpec getDisplaySpec() {
        return displaySpec;
    }
}