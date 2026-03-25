package dev.munebase.dynamickeybinds.server;

import dev.munebase.dynamickeybinds.network.AddKeybindPacket;
import dev.munebase.dynamickeybinds.network.RemoveKeybindPacket;
import dev.munebase.dynamickeybinds.network.UpdateKeybindPacket;
import dev.munebase.dynamickeybinds.persistence.StoredKeybind;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.List;
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
        try {
            String playerUUID = player.getUUID().toString();
            Path worldDataPath = getWorldDataPath(player);
            List<StoredKeybind> keybinds = ServerKeybindPersistence.loadKeybinds(worldDataPath, playerUUID);

            for (StoredKeybind kb : keybinds) {
                if (kb.id().equals(pkt.getId())) {
                    logger.warn("Keybind {} already exists for player {}", pkt.getId(), playerUUID);
                    return;
                }
            }

            keybinds.add(new StoredKeybind(pkt.getId(), pkt.getKeyCode(), pkt.getCategory(), pkt.getAction()));
            ServerKeybindPersistence.saveKeybinds(worldDataPath, playerUUID, keybinds);
            syncSender.accept(player, keybinds);
            pkt.getAction().ifPresent(action -> player.sendSystemMessage(
                Component.literal("Dynamic keybind action: " + action.actionID())
            ));
            logger.info("Server: Added keybind {} for player {}", pkt.getId(), playerUUID);
        } catch (Exception e) {
            logger.error("Error handling add keybind", e);
        }
    }

    /**
     * Handles removing a keybind for a player.
     * @param player The player removing the keybind
     * @param pkt The remove keybind packet
     */
    public void handleRemoveKeybind(ServerPlayer player, RemoveKeybindPacket pkt) {
        try {
            String playerUUID = player.getUUID().toString();
            Path worldDataPath = getWorldDataPath(player);
            List<StoredKeybind> keybinds = ServerKeybindPersistence.loadKeybinds(worldDataPath, playerUUID);

            boolean removed = keybinds.removeIf(kb -> kb.id().equals(pkt.getId()));
            if (!removed) {
                logger.warn("Keybind {} not found for player {}", pkt.getId(), playerUUID);
                return;
            }

            ServerKeybindPersistence.saveKeybinds(worldDataPath, playerUUID, keybinds);
            syncSender.accept(player, keybinds);
            logger.info("Server: Removed keybind {} for player {}", pkt.getId(), playerUUID);
        } catch (Exception e) {
            logger.error("Error handling remove keybind", e);
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
                    keybinds.set(i, new StoredKeybind(keybind.id(), pkt.getKeyCode(), keybind.category(), keybind.action()));
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
        return player.getServer().getServerDirectory().toPath().resolve("world").resolve("data");
    }
}
