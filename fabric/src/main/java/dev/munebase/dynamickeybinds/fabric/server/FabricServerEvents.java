package dev.munebase.dynamickeybinds.fabric.server;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

/**
 * Fabric server lifecycle event hooks for DynamicKeybinds.
 */
public final class FabricServerEvents {

    private FabricServerEvents() {
    }

    /**
     * Registers Fabric server connection callbacks.
     */
    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            FabricServerKeybindHandler.syncKeybindsToPlayer(handler.getPlayer());
        });
    }
}

