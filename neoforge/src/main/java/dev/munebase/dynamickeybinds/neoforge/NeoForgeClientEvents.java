package dev.munebase.dynamickeybinds.neoforge;

import dev.munebase.dynamickeybinds.DynamicKeyRegistryProvider;
import dev.munebase.dynamickeybinds.client.CommonClientRuntime;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * NeoForge client event hooks for runtime keybind lifecycle and polling.
 */
@EventBusSubscriber(modid = "dynamickeybinds", value = Dist.CLIENT)
public final class NeoForgeClientEvents {
    private NeoForgeClientEvents() {
    }

    /**
     * Polls dynamic keybinds each client tick and dispatches bound actions.
     *
     * @param event client tick event
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        CommonClientRuntime.onClientTick(
            minecraft,
            DynamicKeyRegistryProvider.getRegistry(),
            NeoForgeKeybindPersistence::pollAndSyncDynamicKeyRebinds
        );
    }
}
