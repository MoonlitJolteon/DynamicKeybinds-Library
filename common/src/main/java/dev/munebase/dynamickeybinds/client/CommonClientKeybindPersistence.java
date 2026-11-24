package dev.munebase.dynamickeybinds.client;

import dev.munebase.dynamickeybinds.DynamicKeyRegistry;
import dev.munebase.dynamickeybinds.DynamicKeyRegistryProvider;
import dev.munebase.dynamickeybinds.persistence.StoredKey;
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
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Shared client-side persistence/sync flow used by both Forge and Fabric.
 */
public final class CommonClientKeybindPersistence {
    private final Logger logger;
    private final String platformName;
    private final BiConsumer<String, StoredKey> updateSender;

    private String currentWorldPath;
    private String currentPlayerUUID;
    private final Map<String, StoredKey> lastSyncedDynamicKeycodes = new HashMap<>();

    // Set when the Controls screen closes; other mods (e.g. TooManyKeybinds) temporarily rebind keys and must not be persisted
    private static volatile boolean rebindSyncRequested = false;

    /**
     * Requests that dynamic key rebinds be sent to the server on the next client tick.
     * Called when the vanilla Controls screen closes, which is where players rebind keys.
     */
    public static void requestRebindSync() {
        rebindSyncRequested = true;
    }

    public CommonClientKeybindPersistence(Logger logger, String platformName, BiConsumer<String, StoredKey> updateSender) {
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
            ServerSynchronizedDynamicKeyRegistry synchronizedRegistry = requireSynchronizedRegistry(registry);
            synchronizedRegistry.applyServerSnapshot(serverKeybinds, logger);

            logger.info("{}: Registered {} keybinds from server sync", platformName, serverKeybinds.size());
            snapshotDynamicKeycodes(registry);
        } catch (Exception e) {
            logger.error("Error handling server sync", e);
        }
    }

    public void pollAndSyncDynamicKeyRebinds() {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            if (!rebindSyncRequested || minecraft.player == null || minecraft.getConnection() == null) {
                return;
            }
            rebindSyncRequested = false;

            DynamicKeyRegistry registry = DynamicKeyRegistryProvider.getRegistry();
            for (KeyMapping keyMapping : registry.getAllDynamicKeys()) {
                Optional<String> id = registry.getKeyBindId(keyMapping);
                if (id.isEmpty()) {
                    continue;
                }
                StoredKey key = KeyMappingUtil.toStored(keyMapping);
                StoredKey previous = lastSyncedDynamicKeycodes.get(id.get());
                if (previous == null) {
                    continue;
                }
                if (!previous.equals(key)) {
                    updateSender.accept(id.get(), key);
                    lastSyncedDynamicKeycodes.put(id.get(), key);
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
            ServerSynchronizedDynamicKeyRegistry synchronizedRegistry = requireSynchronizedRegistry(registry);
            synchronizedRegistry.clearLocalState(logger);

            lastSyncedDynamicKeycodes.clear();
        } catch (Exception e) {
            logger.error("Error during login", e);
        }
    }

    public void onClientLogout() {
        try {
            DynamicKeyRegistry registry = DynamicKeyRegistryProvider.getRegistry();

            // Unregister all dynamic keybinds - they're only stored on the server
            ServerSynchronizedDynamicKeyRegistry synchronizedRegistry = requireSynchronizedRegistry(registry);
            synchronizedRegistry.clearLocalState(logger);

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
            Optional<String> id = registry.getKeyBindId(keyMapping);
            if (id.isPresent()) {
                lastSyncedDynamicKeycodes.put(id.get(), KeyMappingUtil.toStored(keyMapping));
            }
        }
    }

    private static ServerSynchronizedDynamicKeyRegistry requireSynchronizedRegistry(DynamicKeyRegistry registry) {
        if (registry instanceof ServerSynchronizedDynamicKeyRegistry synchronizedRegistry) {
            return synchronizedRegistry;
        }
        throw new IllegalStateException("Client registry must implement ServerSynchronizedDynamicKeyRegistry");
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
