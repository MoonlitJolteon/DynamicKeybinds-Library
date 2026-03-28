package dev.munebase.dynamickeybinds.client;

import dev.munebase.dynamickeybinds.action.DynamicKeybindAction;
import dev.munebase.dynamickeybinds.model.DisplaySpec;

import java.util.Optional;

/**
 * Loader-provided client networking bridge used by the dynamic key registry.
 */
public interface ClientRegistryNetworkBridge {
    /**
     * Sends an add-keybind request to the connected server.
     */
    void sendAdd(String id, int keyCode, String category, Optional<DynamicKeybindAction> action, DisplaySpec displaySpec);

    /**
     * Sends a remove-keybind request to the connected server.
     */
    void sendRemove(String id);
}
