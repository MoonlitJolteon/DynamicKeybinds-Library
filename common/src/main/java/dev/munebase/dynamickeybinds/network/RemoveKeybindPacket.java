package dev.munebase.dynamickeybinds.network;

/**
 * Network packet sent from client to server to request removing a keybind.
 * 
 * Allows clients to unregister a previously added keybind. The keybind is removed
 * from the server-side registry and the removal is synchronized to all clients.
 * 
 * <p><strong>Direction:</strong> Client -> Server</p>
 * <p><strong>Handler:</strong> Server-side keybind handler (ForgeServerKeybindHandler or FabricServerKeybindHandler)</p>
 */
public class RemoveKeybindPacket {
    /** ID of the keybind to remove */
    private final String id;

    /**
     * Construct a new RemoveKeybindPacket.
     *
     * @param id the keybind ID to remove
     */
    public RemoveKeybindPacket(String id) {
        this.id = id;
    }

    /**
     * Get the keybind ID to remove.
     *
     * @return the keybind identifier
     */
    public String getId() {
        return id;
    }
}

