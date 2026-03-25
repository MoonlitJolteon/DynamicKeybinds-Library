package dev.munebase.dynamickeybinds.client;

import dev.munebase.dynamickeybinds.DynamicKeyRegistry;
import dev.munebase.dynamickeybinds.DynamicKeyRegistryProvider;
import dev.munebase.dynamickeybinds.persistence.StoredKeybind;
import dev.munebase.dynamickeybinds.util.KeyMappingUtil;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Shared client-side persistence/sync flow used by both Forge and Fabric.
 */
public final class CommonClientKeybindPersistence {
    private final Logger logger;
    private final String platformName;
    private final BiConsumer<String, Integer> updateSender;

    private String currentWorldPath;
    private String currentPlayerUUID;
    private final Map<String, Integer> lastSyncedDynamicKeycodes = new HashMap<>();

    public CommonClientKeybindPersistence(Logger logger, String platformName, BiConsumer<String, Integer> updateSender) {
        this.logger = logger;
        this.platformName = platformName;
        this.updateSender = updateSender;
    }

    public void handleServerSync(List<StoredKeybind> serverKeybinds) {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null) {
                return;
            }

            DynamicKeyRegistry registry = DynamicKeyRegistryProvider.getRegistry();

            for (KeyMapping keyMapping : new ArrayList<>(registry.getAllDynamicKeys())) {
                registry.unregisterDynamicKey(keyMapping);
                RuntimeKeyMappingManager.unregisterRuntimeKey(keyMapping, logger);
            }

            for (StoredKeybind entry : serverKeybinds) {
                String cleanId = KeyMappingUtil.normalizeId(entry.id());
                try {
                    KeyMapping keyMapping = registry.registerDynamicKey(
                        cleanId,
                        entry.keyCode(),
                        entry.category(),
                        entry.action()
                    );
                    RuntimeKeyMappingManager.registerRuntimeKey(keyMapping, logger);
                } catch (IllegalArgumentException e) {
                    logger.error("Failed to register keybind: {}", cleanId, e);
                }
            }

            logger.info("{}: Registered {} keybinds from server sync", platformName, serverKeybinds.size());
            snapshotDynamicKeycodes(registry);
        } catch (Exception e) {
            logger.error("Error handling server sync", e);
        }
    }

    public void pollAndSyncDynamicKeyRebinds() {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null || minecraft.getConnection() == null) {
                return;
            }

            DynamicKeyRegistry registry = DynamicKeyRegistryProvider.getRegistry();
            for (KeyMapping keyMapping : registry.getAllDynamicKeys()) {
                String id = KeyMappingUtil.normalizeId(keyMapping.getName());
                int keyCode = KeyMappingUtil.extractKeyCode(keyMapping);
                Integer previous = lastSyncedDynamicKeycodes.get(id);
                if (previous == null || previous != keyCode) {
                    updateSender.accept(id, keyCode);
                    lastSyncedDynamicKeycodes.put(id, keyCode);
                }
            }
        } catch (Exception e) {
            logger.error("Error syncing dynamic key rebinds", e);
        }
    }

    public void onClientLogin() {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null || minecraft.level == null) {
                return;
            }

            currentPlayerUUID = minecraft.player.getUUID().toString();
            currentWorldPath = resolveWorldPath().toString();

            logger.info("{}: Logging in. Awaiting server keybinds sync. Player: {}", platformName, currentPlayerUUID);

            // Clear any existing dynamic keybinds - they will be loaded from server sync
            DynamicKeyRegistry registry = DynamicKeyRegistryProvider.getRegistry();
            for (KeyMapping keyMapping : new ArrayList<>(registry.getAllDynamicKeys())) {
                registry.unregisterDynamicKey(keyMapping);
                RuntimeKeyMappingManager.unregisterRuntimeKey(keyMapping, logger);
            }

            lastSyncedDynamicKeycodes.clear();
        } catch (Exception e) {
            logger.error("Error during login", e);
        }
    }

    public void onClientLogout() {
        try {
            DynamicKeyRegistry registry = DynamicKeyRegistryProvider.getRegistry();

            // Unregister all dynamic keybinds - they're only stored on the server
            for (KeyMapping keyMapping : new ArrayList<>(registry.getAllDynamicKeys())) {
                registry.unregisterDynamicKey(keyMapping);
                RuntimeKeyMappingManager.unregisterRuntimeKey(keyMapping, logger);
            }

            currentWorldPath = null;
            currentPlayerUUID = null;
            lastSyncedDynamicKeycodes.clear();

            logger.info("{}: Logged out and cleared all client-side keybinds", platformName);
        } catch (Exception e) {
            logger.error("Error during logout", e);
        }
    }

    private void snapshotDynamicKeycodes(DynamicKeyRegistry registry) {
        lastSyncedDynamicKeycodes.clear();
        for (KeyMapping keyMapping : registry.getAllDynamicKeys()) {
            String id = KeyMappingUtil.normalizeId(keyMapping.getName());
            lastSyncedDynamicKeycodes.put(id, KeyMappingUtil.extractKeyCode(keyMapping));
        }
    }

    private static Path resolveWorldPath() {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.getSingleplayerServer() != null) {
            String levelName = minecraft.getSingleplayerServer().getWorldData().getLevelName();
            return minecraft.gameDirectory.toPath().resolve("saves").resolve(levelName);
        }

        if (minecraft.player != null && minecraft.getConnection() != null) {
            String serverAddress = minecraft.getConnection().getConnection().getRemoteAddress().toString();
            String safeName = serverAddress.replaceAll("[^a-zA-Z0-9_-]", "_");
            return minecraft.gameDirectory.toPath().resolve("dynamickeybinds-servers").resolve(safeName);
        }

        return minecraft.gameDirectory.toPath().resolve("dynamickeybinds-temp");
    }
}
