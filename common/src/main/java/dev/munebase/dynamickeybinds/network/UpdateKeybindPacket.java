package dev.munebase.dynamickeybinds.network;

/**
 * Network packet sent from client to server to request updating a keybind's key code.
 * 
 * Allows clients to rebind an existing keybind to a different key without removing
 * it. This is used when players customize their keybind settings. The update is
 * persisted on the server and synchronized to all clients.
 * 
 * <p><strong>Direction:</strong> Client -> Server</p>
 * <p><strong>Handler:</strong> ForgeServerKeybindHandler or FabricServerKeybindHandler</p>
 */
public class UpdateKeybindPacket {
    /** ID of the keybind to update */
    private final String id;
    
    /** New GLFW key code for this keybind */
    private final int keyCode;

    /**
     * Construct a new UpdateKeybindPacket.
     *
     * @param id the keybind ID to update
     * @param keyCode the new GLFW key code
     */
    public UpdateKeybindPacket(String id, int keyCode) {
        this.id = id;
        this.keyCode = keyCode;
    }

    /**
     * Get the keybind ID to update.
     *
     * @return the keybind identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Get the new key code.
     *
     * @return the new GLFW key code
     */
    public int getKeyCode() {
        return keyCode;
    }
}