package dev.munebase.dynamickeybinds.forge;

import dev.munebase.dynamickeybinds.client.CommonClientKeybindPersistence;
import dev.munebase.dynamickeybinds.forge.network.ForgeNetworking;
import dev.munebase.dynamickeybinds.network.UpdateKeybindPacket;
import dev.munebase.dynamickeybinds.persistence.StoredKeybind;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Mod.EventBusSubscriber(modid = "dynamickeybinds", value = Dist.CLIENT)
public final class ForgeKeybindPersistence {
    private static final Logger LOGGER = LoggerFactory.getLogger("DynamicKeybinds");
    private static final CommonClientKeybindPersistence COMMON = new CommonClientKeybindPersistence(
        LOGGER,
        "Forge",
        (id, keyCode) -> ForgeNetworking.CHANNEL.sendToServer(
            new UpdateKeybindPacket(id, keyCode)
        )
    );

    private ForgeKeybindPersistence() {
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
