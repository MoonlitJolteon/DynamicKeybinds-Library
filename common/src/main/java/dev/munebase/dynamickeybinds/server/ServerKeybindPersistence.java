package dev.munebase.dynamickeybinds.server;

import dev.munebase.dynamickeybinds.action.DynamicKeybindAction;
import dev.munebase.dynamickeybinds.persistence.StoredKeybind;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NbtIo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Server-side keybind persistence. Stores keybinds in the server's world data directory.
 */
public final class ServerKeybindPersistence {
    private static final Logger LOGGER = LoggerFactory.getLogger("DynamicKeybinds");
    private static final String KEYBINDS_FILE = "dynamickeybinds.nbt";

    private ServerKeybindPersistence() {
    }

    /**
     * Load keybinds for a player from the server world.
     * @param worldDataPath Path to the world's data directory (e.g., server/world/data)
     * @param playerUUID Player UUID as string
     * @return List of stored keybinds, empty if none found
     */
    public static List<StoredKeybind> loadKeybinds(Path worldDataPath, String playerUUID) {
        try {
            Path keybindsFile = worldDataPath.resolve(KEYBINDS_FILE);
            if (!Files.exists(keybindsFile)) {
                return new ArrayList<>();
            }

            CompoundTag rootTag = readRootTag(keybindsFile);
            if (rootTag == null) {
                return new ArrayList<>();
            }
            CompoundTag playersTag = rootTag.getCompound("players");
            CompoundTag playerTag = playersTag.getCompound(playerUUID);
            ListTag keybindsTag = playerTag.getList("keybinds", Tag.TAG_COMPOUND);

            List<StoredKeybind> keybinds = new ArrayList<>();
            for (int i = 0; i < keybindsTag.size(); i++) {
                CompoundTag keybindTag = keybindsTag.getCompound(i);
                String id = keybindTag.getString("id");
                int keyCode = keybindTag.getInt("keyCode");
                String category = keybindTag.getString("category");
                Optional<DynamicKeybindAction> action = Optional.empty();
                if (keybindTag.contains("action")) {
                    CompoundTag actionTag = keybindTag.getCompound("action");
                    String actionID = actionTag.getString("actionID");
                    CompoundTag data = actionTag.getCompound("data");
                    action = Optional.of(new DynamicKeybindAction(actionID, data));
                }
                keybinds.add(new StoredKeybind(id, keyCode, category, action));
            }

            LOGGER.info("Server: Loaded {} keybinds for player {}", keybinds.size(), playerUUID);
            return keybinds;
        } catch (Exception e) {
            LOGGER.error("Error loading server keybinds", e);
            return new ArrayList<>();
        }
    }

    /**
     * Save keybinds for a player to the server world.
     * @param worldDataPath Path to the world's data directory
     * @param playerUUID Player UUID as string
     * @param keybinds List of keybinds to save
     */
    public static void saveKeybinds(Path worldDataPath, String playerUUID, List<StoredKeybind> keybinds) {
        try {
            Files.createDirectories(worldDataPath);
            
            Path keybindsFile = worldDataPath.resolve(KEYBINDS_FILE);
            
            // Load existing data or create new
            CompoundTag rootTag;
            if (Files.exists(keybindsFile)) {
                rootTag = readRootTag(keybindsFile);
                if (rootTag == null) {
                    rootTag = new CompoundTag();
                    rootTag.put("players", new CompoundTag());
                }
            } else {
                rootTag = new CompoundTag();
                rootTag.put("players", new CompoundTag());
            }

            CompoundTag playersTag = rootTag.getCompound("players");
            CompoundTag playerTag = new CompoundTag();

            // Create keybinds list
            ListTag keybindsTag = new ListTag();
            for (StoredKeybind keybind : keybinds) {
                CompoundTag keybindTag = new CompoundTag();
                keybindTag.putString("id", keybind.id());
                keybindTag.putInt("keyCode", keybind.keyCode());
                keybindTag.putString("category", keybind.category());
                if (keybind.action().isPresent()) {
                    DynamicKeybindAction action = keybind.action().get();
                    CompoundTag actionTag = new CompoundTag();
                    actionTag.putString("actionID", action.actionID());
                    actionTag.put("data", action.data().copy());
                    keybindTag.put("action", actionTag);
                }
                keybindsTag.add(keybindTag);
            }
            playerTag.put("keybinds", keybindsTag);

            // Update players tag
            playersTag.put(playerUUID, playerTag);
            rootTag.put("players", playersTag);

            // Write back to file
            NbtIo.writeCompressed(rootTag, keybindsFile.toFile());
            LOGGER.info("Server: Saved {} keybinds for player {}", keybinds.size(), playerUUID);
        } catch (Exception e) {
            LOGGER.error("Error saving server keybinds", e);
        }
    }

    private static CompoundTag readRootTag(Path keybindsFile) {
        try {
            return NbtIo.readCompressed(keybindsFile.toFile());
        } catch (IOException e) {
            LOGGER.error("Could not read server keybinds file {}", keybindsFile, e);
            return null;
        }
    }
}

