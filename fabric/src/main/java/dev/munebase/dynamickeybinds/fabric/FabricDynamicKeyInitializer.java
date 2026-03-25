package dev.munebase.dynamickeybinds.fabric;

import dev.munebase.dynamickeybinds.*;
import dev.munebase.dynamickeybinds.fabric.network.FabricNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

/**
 * Fabric client-side initializer for DynamicKeybinds.
 * Sets up dynamic keybind registration and event handling.
 */
@Environment(EnvType.CLIENT)
public class FabricDynamicKeyInitializer implements ClientModInitializer {
    private static DynamicKeyRegistry registry;

    @Override
    public void onInitializeClient() {
        registry = new DynamicKeyRegistryImpl();
        DynamicKeyRegistryProvider.setRegistryProvider(new FabricRegistryProvider(registry));

        // Register client-side networking handlers
        FabricNetworking.registerClientHandlers();
        
        FabricKeybindPersistence.register();
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        FabricCommandRegistry.registerCommands();
    }

    /**
     * Called at the end of each client tick.
     * Polls dynamic keybinds and triggers their actions.
     */
    private void onClientTick(Minecraft client) {
        FabricKeybindPersistence.pollAndSyncDynamicKeyRebinds();

        if (client.screen != null) {
            return;
        }

        for (KeyMapping keyBinding : registry.getAllDynamicKeys()) {
            while (keyBinding.consumeClick()) {
                var action = registry.getKeyBindAction(keyBinding);
                if (action.isPresent()) {
                    dev.munebase.dynamickeybinds.action.DynamicKeybindActionRegistry.dispatch(
                        action.get().actionID(),
                        action.get().data()
                    );
                }
            }
        }
    }

    /**
     * Provides the dynamic key registry for Fabric.
     */
    static class FabricRegistryProvider implements DynamicKeyRegistryProvider {
        private final DynamicKeyRegistry registry;

        FabricRegistryProvider(DynamicKeyRegistry registry) {
            this.registry = registry;
        }

        @Override
        public DynamicKeyRegistry getRegistryInstance() {
            return registry;
        }
    }
}
