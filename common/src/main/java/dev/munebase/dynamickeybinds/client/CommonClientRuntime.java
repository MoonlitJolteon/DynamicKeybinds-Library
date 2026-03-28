package dev.munebase.dynamickeybinds.client;

import dev.munebase.dynamickeybinds.DynamicKeyRegistry;
import dev.munebase.dynamickeybinds.action.DynamicKeybindActionRegistry;
import dev.munebase.dynamickeybinds.command.CommonDynamicKeyCommands;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Shared client runtime helpers used by both Fabric and Forge.
 */
public final class CommonClientRuntime {
    private static boolean defaultHandlerRegistered = false;

    private CommonClientRuntime() {
    }

    /**
     * Ensures the default debug action handler is registered once.
     */
    public static void ensureDefaultHandlerRegistered() {
        if (defaultHandlerRegistered) {
            return;
        }

        DynamicKeybindActionRegistry.register(CommonDynamicKeyCommands.DEFAULT_HANDLER_ACTION_ID, (actionID, data) -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null) {
                return;
            }

            String keybindId = data.getString("KeyID");
            minecraft.player.sendSystemMessage(Component.literal("Dynamic key pressed: " + keybindId));
        });
        defaultHandlerRegistered = true;
    }

    /**
     * Runs common per-tick client processing for dynamic keybinds.
     *
     * @param minecraft active client instance
     * @param registry active dynamic key registry
     * @param pollAndSync poll callback for key rebind synchronization
     */
    public static void onClientTick(Minecraft minecraft, DynamicKeyRegistry registry, Runnable pollAndSync) {
        ensureDefaultHandlerRegistered();
        pollAndSync.run();

        if (minecraft.screen != null) {
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