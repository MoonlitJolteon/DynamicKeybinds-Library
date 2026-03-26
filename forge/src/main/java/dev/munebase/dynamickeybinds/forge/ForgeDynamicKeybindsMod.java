package dev.munebase.dynamickeybinds.forge;

import java.util.Optional;

import dev.munebase.dynamickeybinds.DynamicKeyRegistry;
import dev.munebase.dynamickeybinds.DynamicKeyRegistryImpl;
import dev.munebase.dynamickeybinds.DynamicKeyRegistryProvider;
import dev.munebase.dynamickeybinds.action.DynamicKeybindAction;
import dev.munebase.dynamickeybinds.client.ClientRegistryNetworkBridge;
import dev.munebase.dynamickeybinds.client.ClientRegistryFactory;
import dev.munebase.dynamickeybinds.forge.network.ForgeNetworking;
import dev.munebase.dynamickeybinds.forge.server.ForgeServerEvents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Forge entry point for DynamicKeybinds.
 *
 * Initializes the shared key registry provider, server event hooks, and Forge
 * networking channel used to synchronize keybind changes and server state.
 */
@Mod("dynamickeybinds")
public class ForgeDynamicKeybindsMod {
    /** Forge-side singleton registry instance. */
    public static final DynamicKeyRegistry REGISTRY = new DynamicKeyRegistryImpl();
    private static final DynamicKeyRegistry CLIENT_REGISTRY = ClientRegistryFactory.createNetworkedRegistry(new ClientRegistryNetworkBridge() {
        @Override
        public void sendAdd(String id, int keyCode, String category, Optional<DynamicKeybindAction> action) {
            ForgeNetworking.sendAddKeybindToServer(id, keyCode, category, action);
        }

        @Override
        public void sendRemove(String id) {
            ForgeNetworking.sendRemoveKeybindToServer(id);
        }
    });

    /**
     * Constructs and initializes the Forge integration.
     */
    public ForgeDynamicKeybindsMod() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            DynamicKeyRegistryProvider.setRegistryProvider(ClientRegistryFactory.createProvider(CLIENT_REGISTRY));
        } else {
            DynamicKeyRegistryProvider.setRegistryProvider(() -> REGISTRY);
        }
        
        // Client-side event handlers are automatically registered by Forge via @Mod.EventBusSubscriber annotations
        // Register server-side events
        MinecraftForge.EVENT_BUS.register(ForgeServerEvents.class);
        
        // Register networking
        SimpleChannel channel = NetworkRegistry.newSimpleChannel(
            new net.minecraft.resources.ResourceLocation("dynamickeybinds", "main"),
            () -> ForgeNetworking.PROTOCOL_VERSION,
            ForgeNetworking.PROTOCOL_VERSION::equals,
            ForgeNetworking.PROTOCOL_VERSION::equals
        );
        ForgeNetworking.register(channel);
    }
}
