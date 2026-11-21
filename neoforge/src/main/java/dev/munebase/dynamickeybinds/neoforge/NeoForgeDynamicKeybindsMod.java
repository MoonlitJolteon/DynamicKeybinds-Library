package dev.munebase.dynamickeybinds.neoforge;

import dev.munebase.dynamickeybinds.client.CommonClientBootstrap;
import dev.munebase.dynamickeybinds.neoforge.network.NeoForgeNetworking;
import dev.munebase.dynamickeybinds.neoforge.server.NeoForgeServerKeybindHandler;
import dev.munebase.dynamickeybinds.server.DynamicKeyServerApi;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

/**
 * NeoForge entry point for DynamicKeybinds.
 *
 * Initializes the shared key registry provider and the payloads used to synchronize
 * keybind changes and server state. Game-bus event handlers register themselves
 * through {@code @EventBusSubscriber}.
 */
@Mod("dynamickeybinds")
public class NeoForgeDynamicKeybindsMod {
    /**
     * Constructs and initializes the NeoForge integration.
     *
     * @param modBus this mod's event bus
     */
    public NeoForgeDynamicKeybindsMod(IEventBus modBus) {
        DynamicKeyServerApi.setRegistry(NeoForgeServerKeybindHandler.registry());

        if (FMLEnvironment.dist == Dist.CLIENT) {
            CommonClientBootstrap.initializeNetworkedRegistry(
                NeoForgeNetworking::sendAddKeybindToServer,
                NeoForgeNetworking::sendRemoveKeybindToServer
            );
        }

        modBus.addListener(NeoForgeNetworking::registerPayloads);
    }
}
