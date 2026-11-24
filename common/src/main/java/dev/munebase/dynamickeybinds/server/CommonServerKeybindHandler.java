package dev.munebase.dynamickeybinds.server;

import dev.munebase.dynamickeybinds.action.DynamicKeybindAction;
import dev.munebase.dynamickeybinds.model.DisplaySpec;
import dev.munebase.dynamickeybinds.network.AddKeybindPacket;
import dev.munebase.dynamickeybinds.network.RemoveKeybindPacket;
import dev.munebase.dynamickeybinds.network.UpdateKeybindPacket;
import dev.munebase.dynamickeybinds.persistence.StoredKeybind;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Shared server-side keybind persistence and mutation flow.
 */
public final class CommonServerKeybindHandler {
    private final Logger logger;
    private final String platformName;
    private final BiConsumer<ServerPlayer, List<StoredKeybind>> syncSender;

    /**
     * Creates a new server keybind handler.
     * @param logger Logger instance
     * @param platformName Name of the mod loader platform (e.g., "Fabric", "Forge")
     * @param syncSender Function to sync keybinds to a player
     */
    public CommonServerKeybindHandler(Logger logger, String platformName, BiConsumer<ServerPlayer, List<StoredKeybind>> syncSender) {
        this.logger = logger;
        this.platformName = platformName;
        this.syncSender = syncSender;
    }

    /**
     * Syncs all keybinds for a player from persistent storage.
     * @param player The player to sync keybinds to
     */
    public void syncKeybindsToPlayer(ServerPlayer player) {
        try {
            String playerUUID = player.getUUID().toString();
            Path worldDataPath = getWorldDataPath(player);
            List<StoredKeybind> keybinds = ServerKeybindPersistence.loadKeybinds(worldDataPath, playerUUID);
            syncSender.accept(player, keybinds);
            logger.info("{}: Synced {} keybinds to player {}", platformName, keybinds.size(), playerUUID);
        } catch (Exception e) {
            logger.error("Error syncing keybinds to player", e);
        }
    }

    /**
     * Handles adding a new keybind for a player.
     * @param player The player adding the keybind
     * @param pkt The add keybind packet
     */
    public void handleAddKeybind(ServerPlayer player, AddKeybindPacket pkt) {
        registerForPlayer(player, pkt.getId(), pkt.getKeyCode(), pkt.getCategory(), pkt.getAction(), pkt.getDisplaySpec());
    }

    /**
     * Registers a keybind for a specific player.
     *
     * @return true when the keybind was added, false otherwise
     */
    public boolean registerForPlayer(ServerPlayer player, String id, int keyCode, String category, Optional<DynamicKeybindAction> action, DisplaySpec displaySpec) {
        try {
            String playerUUID = player.getUUID().toString();
            Path worldDataPath = getWorldDataPath(player);
            List<StoredKeybind> keybinds = ServerKeybindPersistence.loadKeybinds(worldDataPath, playerUUID);

            for (StoredKeybind kb : keybinds) {
                if (kb.id().equals(id)) {
                    logger.warn("Keybind {} already exists for player {}", id, playerUUID);
                    return false;
                }
            }

            keybinds.add(new StoredKeybind(id, keyCode, category, action == null ? Optional.empty() : action, displaySpec == null ? DisplaySpec.empty() : displaySpec));
            ServerKeybindPersistence.saveKeybinds(worldDataPath, playerUUID, keybinds);
            syncSender.accept(player, keybinds);
            logger.info("Server: Added keybind {} for player {}", id, playerUUID);
            return true;
        } catch (Exception e) {
            logger.error("Error handling add keybind", e);
            return false;
        }
    }

    /**
     * Handles removing a keybind for a player.
     * @param player The player removing the keybind
     * @param pkt The remove keybind packet
     */
    public void handleRemoveKeybind(ServerPlayer player, RemoveKeybindPacket pkt) {
        unregisterForPlayer(player, pkt.getId());
    }

    /**
     * Unregisters a keybind for a specific player.
     *
     * @return true when the keybind was removed, false otherwise
     */
    public boolean unregisterForPlayer(ServerPlayer player, String id) {
        try {
            String playerUUID = player.getUUID().toString();
            Path worldDataPath = getWorldDataPath(player);
            List<StoredKeybind> keybinds = ServerKeybindPersistence.loadKeybinds(worldDataPath, playerUUID);

            boolean removed = keybinds.removeIf(kb -> kb.id().equals(id));
            if (!removed) {
                logger.warn("Keybind {} not found for player {}", id, playerUUID);
                return false;
            }

            ServerKeybindPersistence.saveKeybinds(worldDataPath, playerUUID, keybinds);
            syncSender.accept(player, keybinds);
            logger.info("Server: Removed keybind {} for player {}", id, playerUUID);
            return true;
        } catch (Exception e) {
            logger.error("Error handling remove keybind", e);
            return false;
        }
    }

    /**
     * Handles updating a keybind for a player.
     * @param player The player updating the keybind
     * @param pkt The update keybind packet
     */
    public void handleUpdateKeybind(ServerPlayer player, UpdateKeybindPacket pkt) {
        try {
            String playerUUID = player.getUUID().toString();
            Path worldDataPath = getWorldDataPath(player);
            List<StoredKeybind> keybinds = ServerKeybindPersistence.loadKeybinds(worldDataPath, playerUUID);
            boolean updated = false;
            for (int i = 0; i < keybinds.size(); i++) {
                StoredKeybind keybind = keybinds.get(i);
                if (keybind.id().equals(pkt.getId())) {
                    keybinds.set(i, new StoredKeybind(keybind.id(), pkt.getKey(), keybind.category(), keybind.action(), keybind.displaySpec()));
                    updated = true;
                    break;
                }
            }

            if (!updated) {
                logger.warn("Keybind {} not found for update for player {}", pkt.getId(), playerUUID);
                return;
            }

            ServerKeybindPersistence.saveKeybinds(worldDataPath, playerUUID, keybinds);
            syncSender.accept(player, keybinds);
            logger.info("Server: Updated keybind {} for player {}", pkt.getId(), playerUUID);
        } catch (Exception e) {
            logger.error("Error handling update keybind", e);
        }
    }

    private static Path getWorldDataPath(ServerPlayer player) {
        return player.getServer().getWorldPath(LevelResource.ROOT).resolve("data");
    }
}
