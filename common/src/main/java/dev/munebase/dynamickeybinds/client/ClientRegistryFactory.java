package dev.munebase.dynamickeybinds.client;

import dev.munebase.dynamickeybinds.DynamicKeyRegistry;
import dev.munebase.dynamickeybinds.DynamicKeyRegistryProvider;

/**
 * Shared helper for creating client-side dynamic key registry/provider wiring.
 */
public final class ClientRegistryFactory {
    private ClientRegistryFactory() {
    }

    public static DynamicKeyRegistry createNetworkedRegistry(ClientRegistryNetworkBridge bridge) {
        return new NetworkedDynamicKeyRegistry(bridge);
    }

    public static DynamicKeyRegistryProvider createProvider(DynamicKeyRegistry registry) {
        return () -> registry;
    }
}
