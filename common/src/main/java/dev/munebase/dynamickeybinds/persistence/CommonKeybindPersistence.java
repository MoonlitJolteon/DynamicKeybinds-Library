package dev.munebase.dynamickeybinds.persistence;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Manages persistence of dynamic keybinds to the world directory using NBT format.
 * Stores data at &lt;world&gt;/data/dynamickeybinds.nbt for world-specific keybinds.
 */
public final class CommonKeybindPersistence {
    private static final Logger LOGGER = LoggerFactory.getLogger("DynamicKeybinds");
    private static final String NBT_FILE_NAME = "dynamickeybinds.nbt";
    private static final String DATA_FOLDER = "data";
    private static final String KEYBINDS_TAG = "keybinds";
    private static final String PLAYERS_TAG = "players";

    private CommonKeybindPersistence() {
    }

    /**
     * Loads keybinds from the world NBT data file.
     * @param worldPath Path to the world directory
     * @param playerUUID UUID of the player
     * @return List of stored keybinds, or empty list if no file exists
     */
    public static List<StoredKeybind> loadKeybinds(Path worldPath, String playerUUID) {
        Path nbtPath = getNBTPath(worldPath);
        if (nbtPath == null || !Files.exists(nbtPath)) {
            LOGGER.info("No saved keybinds NBT file at {}", nbtPath);
            return new ArrayList<>();
        }

        try {
            CompoundTag rootTag = net.minecraft.nbt.NbtIo.read(nbtPath.toFile());
            if (rootTag == null) {
                LOGGER.warn("Root NBT tag is null");
                return new ArrayList<>();
            }

            CompoundTag playersTag = rootTag.getCompound(PLAYERS_TAG);
            if (!playersTag.contains(playerUUID)) {
                LOGGER.info("No keybinds for player {}", playerUUID);
                return new ArrayList<>();
            }

            CompoundTag playerTag = playersTag.getCompound(playerUUID);
            ListTag keybindsList = playerTag.getList(KEYBINDS_TAG, Tag.TAG_COMPOUND);
            
            List<StoredKeybind> entries = new ArrayList<>();
            for (int i = 0; i < keybindsList.size(); i++) {
                CompoundTag keybindTag = keybindsList.getCompound(i);
                String id = keybindTag.getString("id");
                int keyCode = keybindTag.getInt("keyCode");
                String category = keybindTag.getString("category");
                
                // Load action if present (backward compatible)
                java.util.Optional<dev.munebase.dynamickeybinds.action.DynamicKeybindAction> action = java.util.Optional.empty();
                if (keybindTag.contains("action")) {
                    CompoundTag actionTag = keybindTag.getCompound("action");
                    String actionID = actionTag.getString("actionID");
                    CompoundTag actionData = actionTag.getCompound("data");
                    action = java.util.Optional.of(new dev.munebase.dynamickeybinds.action.DynamicKeybindAction(actionID, actionData));
                }
                
                entries.add(new StoredKeybind(id, keyCode, category, action));
            }

            LOGGER.info("Loaded {} keybinds from NBT for player {}", entries.size(), playerUUID);
            return entries;
        } catch (IOException e) {
            LOGGER.error("Error loading keybinds from {}", nbtPath, e);
            return new ArrayList<>();
        }
    }

    /**
     * Saves keybinds to the world NBT data file.
     * @param worldPath Path to the world directory
     * @param playerUUID UUID of the player
     * @param keybinds List of keybinds to save
     */
    public static void saveKeybinds(Path worldPath, String playerUUID, Collection<StoredKeybind> keybinds) {
        Path nbtPath = getNBTPath(worldPath);
        if (nbtPath == null) {
            LOGGER.warn("NBT path is null");
            return;
        }

        try {
            Files.createDirectories(nbtPath.getParent());

            // Load existing root tag or create new one
            CompoundTag rootTag;
            if (Files.exists(nbtPath)) {
                rootTag = net.minecraft.nbt.NbtIo.read(nbtPath.toFile());
                if (rootTag == null) {
                    rootTag = new CompoundTag();
                }
            } else {
                rootTag = new CompoundTag();
            }

            // Get or create players compound tag
            CompoundTag playersTag = rootTag.contains(PLAYERS_TAG) 
                ? rootTag.getCompound(PLAYERS_TAG) 
                : new CompoundTag();

            // Create player compound tag with keybinds
            CompoundTag playerTag = new CompoundTag();
            ListTag keybindsList = new ListTag();

            for (StoredKeybind keybind : keybinds) {
                CompoundTag keybindTag = new CompoundTag();
                keybindTag.putString("id", keybind.id());
                keybindTag.putInt("keyCode", keybind.keyCode());
                keybindTag.putString("category", keybind.category());
                
                // Save action if present
                if (keybind.action().isPresent()) {
                    dev.munebase.dynamickeybinds.action.DynamicKeybindAction action = keybind.action().get();
                    CompoundTag actionTag = new CompoundTag();
                    actionTag.putString("actionID", action.actionID());
                    actionTag.put("data", action.data().copy());
                    keybindTag.put("action", actionTag);
                }
                
                keybindsList.add(keybindTag);
            }

            playerTag.put(KEYBINDS_TAG, keybindsList);
            playersTag.put(playerUUID, playerTag);
            rootTag.put(PLAYERS_TAG, playersTag);

            // Write NBT file
            net.minecraft.nbt.NbtIo.write(rootTag, nbtPath.toFile());
            LOGGER.info("Saved {} keybinds to NBT for player {}", keybinds.size(), playerUUID);
        } catch (IOException e) {
            LOGGER.error("Error saving keybinds to {}", nbtPath, e);
        }
    }

    /**
     * Gets the path to the keybinds NBT file.
     * @param worldPath Path to the world directory
     * @return Path to the NBT file at <world>/data/dynamickeybinds.nbt
     */
    private static Path getNBTPath(Path worldPath) {
        try {
            return worldPath.resolve(DATA_FOLDER).resolve(NBT_FILE_NAME);
        } catch (Exception e) {
            LOGGER.error("Error resolving NBT path", e);
            return null;
        }
    }
}
