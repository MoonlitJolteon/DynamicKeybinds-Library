package dev.munebase.dynamickeybinds.client;

import dev.munebase.dynamickeybinds.DynamicKeyRegistry;
import dev.munebase.dynamickeybinds.DynamicKeyRegistryProvider;
import dev.munebase.dynamickeybinds.action.DynamicKeybindAction;
import dev.munebase.dynamickeybinds.model.DisplaySpec;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Shared client bootstrap helpers for networked dynamic key registry setup.
 */
public final class CommonClientBootstrap {

    @FunctionalInterface
    public interface AddRequestSender {
        void send(String id, int keyCode, String category, Optional<DynamicKeybindAction> action, DisplaySpec displaySpec);
    }

    private CommonClientBootstrap() {
    }

    /**
     * Creates a networked client registry and installs it as the active provider.
     *
     * @param networkBridge bridge used to send add/remove operations to the server
     * @return created client registry
     */
    public static DynamicKeyRegistry initializeNetworkedRegistry(ClientRegistryNetworkBridge networkBridge) {
        DynamicKeyRegistry registry = new NetworkedDynamicKeyRegistry(networkBridge);
        DynamicKeyRegistryProvider.setRegistryProvider(() -> registry);
        return registry;
    }

    /**
     * Creates a networked client registry and installs it as the active provider.
     *
     * @param addSender callback to send add requests to the server
     * @param removeSender callback to send remove requests to the server
     * @return created client registry
     */
    public static DynamicKeyRegistry initializeNetworkedRegistry(AddRequestSender addSender, Consumer<String> removeSender) {
        return initializeNetworkedRegistry(new ClientRegistryNetworkBridge() {
            @Override
            public void sendAdd(String id, int keyCode, String category, Optional<DynamicKeybindAction> action, DisplaySpec displaySpec) {
                addSender.send(id, keyCode, category, action, displaySpec);
            }

            @Override
            public void sendRemove(String id) {
                removeSender.accept(id);
            }
        });
    }
}