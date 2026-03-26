package dev.munebase.dynamickeybinds.client;

import dev.munebase.dynamickeybinds.action.DynamicKeybindAction;

import java.util.Optional;

/**
 * Loader-provided client networking bridge used by the dynamic key registry.
 */
public interface ClientRegistryNetworkBridge {
    /**
     * Sends an add-keybind request to the connected server.
     */
    void sendAdd(String id, int keyCode, String category, Optional<DynamicKeybindAction> action);

    /**
     * Sends a remove-keybind request to the connected server.
     */
    void sendRemove(String id);
}
