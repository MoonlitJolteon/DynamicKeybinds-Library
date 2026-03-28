package dev.munebase.dynamickeybinds.fabric;

import dev.munebase.dynamickeybinds.DynamicKeyRegistry;
import dev.munebase.dynamickeybinds.client.CommonClientBootstrap;
import dev.munebase.dynamickeybinds.client.CommonClientRuntime;
import dev.munebase.dynamickeybinds.fabric.network.FabricNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
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
        registry = CommonClientBootstrap.initializeNetworkedRegistry(
            FabricNetworking::sendAddKeybindToServer,
            FabricNetworking::sendRemoveKeybindToServer
        );

        // Register client-side networking handlers
        FabricNetworking.registerClientHandlers();
        CommonClientRuntime.ensureDefaultHandlerRegistered();
        
        FabricKeybindPersistence.register();
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        FabricCommandRegistry.registerCommands();
    }

    /**
     * Called at the end of each client tick.
     * Polls dynamic keybinds and triggers their actions.
     */
    private void onClientTick(Minecraft client) {
        CommonClientRuntime.onClientTick(client, registry, FabricKeybindPersistence::pollAndSyncDynamicKeyRebinds);
    }
}
