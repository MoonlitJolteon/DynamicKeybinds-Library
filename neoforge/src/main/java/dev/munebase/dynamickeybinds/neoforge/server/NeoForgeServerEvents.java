package dev.munebase.dynamickeybinds.neoforge.server;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * NeoForge server-side lifecycle events for DynamicKeybinds.
 */
@EventBusSubscriber(modid = "dynamickeybinds")
public final class NeoForgeServerEvents {

    private NeoForgeServerEvents() {
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
        NeoForgeServerKeybindHandler.syncKeybindsToPlayer(player);
    }
}

