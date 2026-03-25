package dev.munebase.dynamickeybinds.fabric;

import dev.munebase.dynamickeybinds.DynamicKeyRegistry;
import dev.munebase.dynamickeybinds.DynamicKeyRegistryImpl;
import dev.munebase.dynamickeybinds.DynamicKeyRegistryProvider;
import dev.munebase.dynamickeybinds.fabric.network.FabricNetworking;
import dev.munebase.dynamickeybinds.fabric.server.FabricServerEvents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
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
        
        // Register server-side handlers only on server
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            FabricNetworking.registerServerHandlers();
            FabricServerEvents.register();
        }
    }
}

