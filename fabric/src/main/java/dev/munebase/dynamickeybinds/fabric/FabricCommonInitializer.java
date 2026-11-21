package dev.munebase.dynamickeybinds.fabric;

import dev.munebase.dynamickeybinds.DynamicKeyRegistry;
import dev.munebase.dynamickeybinds.DynamicKeyRegistryImpl;
import dev.munebase.dynamickeybinds.DynamicKeyRegistryProvider;
import dev.munebase.dynamickeybinds.fabric.network.FabricNetworking;
import dev.munebase.dynamickeybinds.fabric.server.FabricServerEvents;
import dev.munebase.dynamickeybinds.server.DynamicKeyServerApi;
import dev.munebase.dynamickeybinds.fabric.server.FabricServerKeybindHandler;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fabric common (both sides) initializer for DynamicKeybinds.
 * Handles server-side setup including networking and event handlers.
 */
public class FabricCommonInitializer implements ModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("DynamicKeybinds");

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Fabric common");

        DynamicKeyServerApi.setRegistry(FabricServerKeybindHandler.registry());

        // Payload types must be known on both sides; server handlers serve dedicated and integrated servers.
        FabricNetworking.registerPayloadTypes();
        FabricNetworking.registerServerHandlers();
        FabricServerEvents.register();
    }
}

