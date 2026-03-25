package dev.munebase.dynamickeybinds.forge;

import dev.munebase.dynamickeybinds.DynamicKeyRegistryProvider;
import dev.munebase.dynamickeybinds.action.DynamicKeybindActionRegistry;
import dev.munebase.dynamickeybinds.client.RuntimeKeyMappingManager;
import dev.munebase.dynamickeybinds.command.CommonDynamicKeyCommands;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Forge client event hooks for runtime keybind lifecycle and polling.
 */
@Mod.EventBusSubscriber(modid = "dynamickeybinds", value = Dist.CLIENT)
public final class ForgeClientEvents {
    private static final Logger LOGGER = LoggerFactory.getLogger("DynamicKeybinds");
    private static boolean defaultHandlerRegistered = false;

    private ForgeClientEvents() {
    }

    /**
     * Adds a key mapping at runtime to Minecraft options if it is not already present.
     *
     * @param keyMapping key mapping to append
     */
    public static void registerRuntimeKey(KeyMapping keyMapping) {
        RuntimeKeyMappingManager.registerRuntimeKey(keyMapping, LOGGER);
    }

    /**
     * Removes a runtime key mapping from Minecraft options.
     *
     * @param keyMapping key mapping to remove
     */
    public static void unregisterRuntimeKey(KeyMapping keyMapping) {
        RuntimeKeyMappingManager.unregisterRuntimeKey(keyMapping, LOGGER);
    }

    /**
     * Registers all known dynamic key mappings during Forge key registration.
     *
     * @param event Forge key registration event
     */
    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        for (KeyMapping keyMapping : ForgeDynamicKeybindsMod.REGISTRY.getAllDynamicKeys()) {
            event.register(keyMapping);
        }
    }

    /**
     * Polls dynamic keybinds each client tick and dispatches bound actions.
     *
     * @param event client tick event
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (!defaultHandlerRegistered) {
            DynamicKeybindActionRegistry.register(CommonDynamicKeyCommands.DEFAULT_HANDLER_ACTION_ID, (actionID, data) -> {
                Minecraft innerMinecraft = Minecraft.getInstance();
                if (innerMinecraft.player == null) {
                    return;
                }

                String keybindId = data.getString("KeyID");
                innerMinecraft.player.sendSystemMessage(Component.literal("Dynamic key pressed: " + keybindId));
            });
            defaultHandlerRegistered = true;
        }

        ForgeKeybindPersistence.pollAndSyncDynamicKeyRebinds();
        if (minecraft.screen != null) {
            return;
        }

        for (KeyMapping keyBinding : DynamicKeyRegistryProvider.getRegistry().getAllDynamicKeys()) {
            while (keyBinding.consumeClick()) {
                var action = DynamicKeyRegistryProvider.getRegistry().getKeyBindAction(keyBinding);
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
