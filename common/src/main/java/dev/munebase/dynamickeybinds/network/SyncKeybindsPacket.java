package dev.munebase.dynamickeybinds.network;

import dev.munebase.dynamickeybinds.persistence.StoredKeybind;
import java.util.ArrayList;
import java.util.List;

/**
 * Packet sent from server to client to sync all keybinds for the joining player.
 */
public class SyncKeybindsPacket {
    private final List<StoredKeybind> keybinds;

    /**
     * Creates a sync packet with the given keybinds.
     * @param keybinds The keybinds to sync
     */
    public SyncKeybindsPacket(List<StoredKeybind> keybinds) {
        this.keybinds = keybinds;
    }

    /**
     * Gets the keybinds in this packet.
     * @return The list of keybinds
     */
    public List<StoredKeybind> getKeybinds() {
        return keybinds;
    }
}

