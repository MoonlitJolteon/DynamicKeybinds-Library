package dev.munebase.dynamickeybinds.network;

import dev.munebase.dynamickeybinds.persistence.StoredKey;

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
    
    /** New input (type + code) for this keybind */
    private final StoredKey key;

    /**
     * Construct a new UpdateKeybindPacket.
     *
     * @param id the keybind ID to update
     * @param key the new input
     */
    public UpdateKeybindPacket(String id, StoredKey key) {
        this.id = id;
        this.key = key;
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
     * Get the new input.
     *
     * @return the new input
     */
    public StoredKey getKey() {
        return key;
    }
}