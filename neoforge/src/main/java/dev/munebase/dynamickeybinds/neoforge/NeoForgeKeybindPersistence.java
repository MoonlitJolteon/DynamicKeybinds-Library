package dev.munebase.dynamickeybinds.neoforge;

import dev.munebase.dynamickeybinds.client.CommonClientKeybindPersistence;
import dev.munebase.dynamickeybinds.neoforge.network.NeoForgeNetworking;
import dev.munebase.dynamickeybinds.persistence.StoredKeybind;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@EventBusSubscriber(modid = "dynamickeybinds", value = Dist.CLIENT)
public final class NeoForgeKeybindPersistence {
    private static final Logger LOGGER = LoggerFactory.getLogger("DynamicKeybinds");
    private static final CommonClientKeybindPersistence COMMON = new CommonClientKeybindPersistence(
        LOGGER,
        "NeoForge",
        NeoForgeNetworking::sendUpdateKeybindToServer
    );

    private NeoForgeKeybindPersistence() {
    }

    public static void handleServerSync(List<StoredKeybind> serverKeybinds) {
        COMMON.handleServerSync(serverKeybinds);
    }

    public static void pollAndSyncDynamicKeyRebinds() {
        COMMON.pollAndSyncDynamicKeyRebinds();
    }

    @SubscribeEvent
    public static void onClientLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        COMMON.onClientLogin();
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        COMMON.onClientLogout();
    }
}
