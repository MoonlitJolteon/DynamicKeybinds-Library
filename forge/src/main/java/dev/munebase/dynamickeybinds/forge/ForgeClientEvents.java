package dev.munebase.dynamickeybinds.forge;

import dev.munebase.dynamickeybinds.DynamicKeyRegistryProvider;
import dev.munebase.dynamickeybinds.client.CommonClientRuntime;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Forge client event hooks for runtime keybind lifecycle and polling.
 */
@Mod.EventBusSubscriber(modid = "dynamickeybinds", value = Dist.CLIENT)
public final class ForgeClientEvents {
    private ForgeClientEvents() {
    }

    /**
     * Registers all known dynamic key mappings during Forge key registration.
     *
     * @param event Forge key registration event
     */
    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        for (KeyMapping keyMapping : DynamicKeyRegistryProvider.getRegistry().getAllDynamicKeys()) {
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
        CommonClientRuntime.onClientTick(
            minecraft,
            DynamicKeyRegistryProvider.getRegistry(),
            ForgeKeybindPersistence::pollAndSyncDynamicKeyRebinds
        );
    }
}
