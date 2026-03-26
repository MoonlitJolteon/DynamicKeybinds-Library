package dev.munebase.dynamickeybinds.client;

import dev.munebase.dynamickeybinds.DynamicKeyRegistry;
import dev.munebase.dynamickeybinds.persistence.StoredKeybind;
import org.slf4j.Logger;

import java.util.List;

/**
 * Optional client-side extension for registries backed by server synchronization.
 */
public interface ServerSynchronizedDynamicKeyRegistry extends DynamicKeyRegistry {
    /**
     * Replaces current client state with a server-authoritative snapshot.
     */
    void applyServerSnapshot(List<StoredKeybind> serverKeybinds, Logger logger);

    /**
     * Clears local keybind state without sending removal packets.
     */
    void clearLocalState(Logger logger);
}
