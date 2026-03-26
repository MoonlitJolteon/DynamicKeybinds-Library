package dev.munebase.dynamickeybinds.fabric;

import java.util.Optional;

import dev.munebase.dynamickeybinds.*;
import dev.munebase.dynamickeybinds.action.DynamicKeybindAction;
import dev.munebase.dynamickeybinds.action.DynamicKeybindActionRegistry;
import dev.munebase.dynamickeybinds.client.ClientRegistryFactory;
import dev.munebase.dynamickeybinds.client.ClientRegistryNetworkBridge;
import dev.munebase.dynamickeybinds.command.CommonDynamicKeyCommands;
import dev.munebase.dynamickeybinds.fabric.network.FabricNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Fabric client-side initializer for DynamicKeybinds.
 * Sets up dynamic keybind registration and event handling.
 */
@Environment(EnvType.CLIENT)
public class FabricDynamicKeyInitializer implements ClientModInitializer {
    private static DynamicKeyRegistry registry;

    @Override
    public void onInitializeClient() {
        registry = ClientRegistryFactory.createNetworkedRegistry(new ClientRegistryNetworkBridge() {
            @Override
            public void sendAdd(String id, int keyCode, String category, Optional<DynamicKeybindAction> action) {
                FabricNetworking.sendAddKeybindToServer(id, keyCode, category, action);
            }

            @Override
            public void sendRemove(String id) {
                FabricNetworking.sendRemoveKeybindToServer(id);
            }
        });
        DynamicKeyRegistryProvider.setRegistryProvider(ClientRegistryFactory.createProvider(registry));

        // Register client-side networking handlers
        FabricNetworking.registerClientHandlers();
        DynamicKeybindActionRegistry.register(CommonDynamicKeyCommands.DEFAULT_HANDLER_ACTION_ID, (actionID, data) -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null) {
                return;
            }

            String keybindId = data.getString("KeyID");
            minecraft.player.sendSystemMessage(Component.literal("Dynamic key pressed: " + keybindId));
        });
        
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
                    DynamicKeybindActionRegistry.dispatch(
                        action.get().actionID(),
                        action.get().data()
                    );
                }
            }
        }
    }
}
