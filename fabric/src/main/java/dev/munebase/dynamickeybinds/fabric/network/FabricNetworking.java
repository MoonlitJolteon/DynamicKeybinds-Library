package dev.munebase.dynamickeybinds.fabric.network;

import dev.munebase.dynamickeybinds.network.SyncKeybindsPacket;
import dev.munebase.dynamickeybinds.network.AddKeybindPacket;
import dev.munebase.dynamickeybinds.network.CommonPacketCodec;
import dev.munebase.dynamickeybinds.network.RemoveKeybindPacket;
import dev.munebase.dynamickeybinds.network.UpdateKeybindPacket;
import dev.munebase.dynamickeybinds.persistence.StoredKeybind;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static io.netty.buffer.Unpooled.buffer;

/**
 * Fabric networking utility for packet registration and transmission.
 *
 * Contains client/server receiver registration plus helper methods for sending
 * add/remove/update/sync keybind packets.
 */
public final class FabricNetworking {
    private static final Logger LOGGER = LoggerFactory.getLogger("DynamicKeybinds");
    private static final ResourceLocation SYNC_KEYBINDS_ID = new ResourceLocation("dynamickeybinds", "sync_keybinds");
    private static final ResourceLocation ADD_KEYBIND_ID = new ResourceLocation("dynamickeybinds", "add_keybind");
    private static final ResourceLocation REMOVE_KEYBIND_ID = new ResourceLocation("dynamickeybinds", "remove_keybind");
    private static final ResourceLocation UPDATE_KEYBIND_ID = new ResourceLocation("dynamickeybinds", "update_keybind");

    private FabricNetworking() {
    }

    /**
     * Registers client-side packet receivers.
     */
    public static void registerClientHandlers() {
        // Client-side: receive sync packet
        ClientPlayNetworking.registerGlobalReceiver(SYNC_KEYBINDS_ID, (client, handler, buf, responseSender) -> {
            try {
                SyncKeybindsPacket packet = decodeSyncKeybinds(buf);
                client.execute(() -> {
                    try {
                        dev.munebase.dynamickeybinds.fabric.FabricKeybindPersistence.handleServerSync(packet.getKeybinds());
                    } catch (Exception e) {
                        LOGGER.error("Error handling sync packet", e);
                    }
                });
            } catch (Exception e) {
                LOGGER.error("Error decoding sync packet", e);
            }
        });
    }

    /**
     * Registers server-side packet receivers.
     */
    public static void registerServerHandlers() {
        // Server-side: receive add keybind packet
        ServerPlayNetworking.registerGlobalReceiver(ADD_KEYBIND_ID, (server, player, handler, buf, responseSender) -> {
            try {
                AddKeybindPacket packet = decodeAddKeybind(buf);
                server.submit(() -> {
                    try {
                        dev.munebase.dynamickeybinds.fabric.server.FabricServerKeybindHandler.handleAddKeybind(player, packet);
                    } catch (Exception e) {
                        LOGGER.error("Error handling add keybind packet", e);
                    }
                });
            } catch (Exception e) {
                LOGGER.error("Error decoding add keybind packet", e);
            }
        });

        // Server-side: receive remove keybind packet
        ServerPlayNetworking.registerGlobalReceiver(REMOVE_KEYBIND_ID, (server, player, handler, buf, responseSender) -> {
            try {
                RemoveKeybindPacket packet = decodeRemoveKeybind(buf);
                server.submit(() -> {
                    try {
                        dev.munebase.dynamickeybinds.fabric.server.FabricServerKeybindHandler.handleRemoveKeybind(player, packet);
                    } catch (Exception e) {
                        LOGGER.error("Error handling remove keybind packet", e);
                    }
                });
            } catch (Exception e) {
                LOGGER.error("Error decoding remove keybind packet", e);
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(UPDATE_KEYBIND_ID, (server, player, handler, buf, responseSender) -> {
            try {
                UpdateKeybindPacket packet = decodeUpdateKeybind(buf);
                server.submit(() -> {
                    try {
                        dev.munebase.dynamickeybinds.fabric.server.FabricServerKeybindHandler.handleUpdateKeybind(player, packet);
                    } catch (Exception e) {
                        LOGGER.error("Error handling update keybind packet", e);
                    }
                });
            } catch (Exception e) {
                LOGGER.error("Error decoding update keybind packet", e);
            }
        });
    }

    /**
     * Sends a client add-keybind request packet.
     */
    public static void sendAddKeybindToServer(String id, int keyCode, String category, Optional<dev.munebase.dynamickeybinds.action.DynamicKeybindAction> action) {
        try {
            FriendlyByteBuf buf = new FriendlyByteBuf(buffer());
            CommonPacketCodec.encodeAddKeybind(new AddKeybindPacket(id, keyCode, category, action), buf);
            ClientPlayNetworking.send(ADD_KEYBIND_ID, buf);
        } catch (Exception e) {
            LOGGER.error("Error sending add keybind packet", e);
        }
    }

    /**
     * Sends a client remove-keybind request packet.
     */
    public static void sendRemoveKeybindToServer(String id) {
        try {
            FriendlyByteBuf buf = new FriendlyByteBuf(buffer());
            CommonPacketCodec.encodeRemoveKeybind(new RemoveKeybindPacket(id), buf);
            ClientPlayNetworking.send(REMOVE_KEYBIND_ID, buf);
        } catch (Exception e) {
            LOGGER.error("Error sending remove keybind packet", e);
        }
    }

    /**
     * Sends a client update-keybind request packet.
     */
    public static void sendUpdateKeybindToServer(String id, int keyCode) {
        try {
            FriendlyByteBuf buf = new FriendlyByteBuf(buffer());
            CommonPacketCodec.encodeUpdateKeybind(new UpdateKeybindPacket(id, keyCode), buf);
            ClientPlayNetworking.send(UPDATE_KEYBIND_ID, buf);
        } catch (Exception e) {
            LOGGER.error("Error sending update keybind packet", e);
        }
    }

    /**
     * Sends a server sync packet containing all keybinds for a player.
     */
    public static void sendSyncKeybindsToPlayer(net.minecraft.server.level.ServerPlayer player, java.util.List<StoredKeybind> keybinds) {
        try {
            FriendlyByteBuf buf = new FriendlyByteBuf(buffer());
            encodeSyncKeybinds(buf, keybinds);
            ServerPlayNetworking.send(player, SYNC_KEYBINDS_ID, buf);
        } catch (Exception e) {
            LOGGER.error("Error sending sync packet", e);
        }
    }

    // Encoding/Decoding
    private static SyncKeybindsPacket decodeSyncKeybinds(FriendlyByteBuf buf) {
        return CommonPacketCodec.decodeSyncKeybinds(buf);
    }

    private static void encodeSyncKeybinds(FriendlyByteBuf buf, java.util.List<StoredKeybind> keybinds) {
        CommonPacketCodec.encodeSyncKeybinds(buf, keybinds);
    }

    private static AddKeybindPacket decodeAddKeybind(FriendlyByteBuf buf) {
        return CommonPacketCodec.decodeAddKeybind(buf);
    }

    private static RemoveKeybindPacket decodeRemoveKeybind(FriendlyByteBuf buf) {
        return CommonPacketCodec.decodeRemoveKeybind(buf);
    }

    private static UpdateKeybindPacket decodeUpdateKeybind(FriendlyByteBuf buf) {
        return CommonPacketCodec.decodeUpdateKeybind(buf);
    }
}

