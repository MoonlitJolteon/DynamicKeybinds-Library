package dev.munebase.dynamickeybinds.forge.server;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * Forge server-side lifecycle events for DynamicKeybinds.
 */
@Mod.EventBusSubscriber(modid = "dynamickeybinds")
public final class ForgeServerEvents {

    private ForgeServerEvents() {
    }

    /**
     * Syncs persisted keybinds to a player when they log in.
     *
     * @param event login event
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity().level().isClientSide) {
            return; // Only handle server-side
        }
        
        net.minecraft.server.level.ServerPlayer player = (net.minecraft.server.level.ServerPlayer) event.getEntity();
        ForgeServerKeybindHandler.syncKeybindsToPlayer(player);
    }
}

