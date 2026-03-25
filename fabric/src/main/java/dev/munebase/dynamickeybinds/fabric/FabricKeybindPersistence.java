package dev.munebase.dynamickeybinds.fabric;

import dev.munebase.dynamickeybinds.client.CommonClientKeybindPersistence;
import dev.munebase.dynamickeybinds.persistence.StoredKeybind;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class FabricKeybindPersistence {
    private static final Logger LOGGER = LoggerFactory.getLogger("DynamicKeybinds");
    private static final CommonClientKeybindPersistence COMMON = new CommonClientKeybindPersistence(
        LOGGER,
        "Fabric",
        dev.munebase.dynamickeybinds.fabric.network.FabricNetworking::sendUpdateKeybindToServer
    );

    private FabricKeybindPersistence() {
    }

    public static void register() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> COMMON.onClientLogin());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> COMMON.onClientLogout());
    }

    public static void handleServerSync(List<StoredKeybind> serverKeybinds) {
        COMMON.handleServerSync(serverKeybinds);
    }

    public static void pollAndSyncDynamicKeyRebinds() {
        COMMON.pollAndSyncDynamicKeyRebinds();
    }
}
