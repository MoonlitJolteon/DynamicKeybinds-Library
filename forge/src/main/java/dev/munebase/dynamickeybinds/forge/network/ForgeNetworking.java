package dev.munebase.dynamickeybinds.forge.network;

import dev.munebase.dynamickeybinds.network.SyncKeybindsPacket;
import dev.munebase.dynamickeybinds.network.AddKeybindPacket;
import dev.munebase.dynamickeybinds.network.CommonPacketCodec;
import dev.munebase.dynamickeybinds.network.RemoveKeybindPacket;
import dev.munebase.dynamickeybinds.network.UpdateKeybindPacket;
import dev.munebase.dynamickeybinds.action.DynamicKeybindAction;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraft.network.FriendlyByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;
import java.util.Optional;

/**
 * Forge packet registration and codec/handler utilities.
 *
 * Defines packet IDs, serialization, and handling for synchronization and
 * client-to-server keybind change requests.
 */
public final class ForgeNetworking {
    private static final Logger LOGGER = LoggerFactory.getLogger("DynamicKeybinds");
    public static final String PROTOCOL_VERSION = "1";
    public static SimpleChannel CHANNEL = null;
    private static int packetId = 0;

    private ForgeNetworking() {
    }

    /**
     * Registers all DynamicKeybinds packets on the provided Forge channel.
     *
     * @param channel initialized Forge simple channel
     */
    public static void register(SimpleChannel channel) {
        CHANNEL = channel;
        
        channel.messageBuilder(SyncKeybindsPacket.class, packetId++)
            .decoder(ForgeNetworking::decodeSyncKeybinds)
            .encoder(ForgeNetworking::encodeSyncKeybinds)
            .consumerNetworkThread(ForgeNetworking::handleSyncKeybinds)
            .add();

        channel.messageBuilder(AddKeybindPacket.class, packetId++)
            .decoder(ForgeNetworking::decodeAddKeybind)
            .encoder(ForgeNetworking::encodeAddKeybind)
            .consumerNetworkThread(ForgeNetworking::handleAddKeybind)
            .add();

        channel.messageBuilder(RemoveKeybindPacket.class, packetId++)
            .decoder(ForgeNetworking::decodeRemoveKeybind)
            .encoder(ForgeNetworking::encodeRemoveKeybind)
            .consumerNetworkThread(ForgeNetworking::handleRemoveKeybind)
            .add();

        channel.messageBuilder(UpdateKeybindPacket.class, packetId++)
            .decoder(ForgeNetworking::decodeUpdateKeybind)
            .encoder(ForgeNetworking::encodeUpdateKeybind)
            .consumerNetworkThread(ForgeNetworking::handleUpdateKeybind)
            .add();
    }

    /**
     * Sends a client add-keybind request packet to the server.
     *
     * @return true when sent, false when channel is unavailable
     */
    public static boolean sendAddKeybindToServer(String id, int keyCode, String category, Optional<DynamicKeybindAction> action) {
        if (CHANNEL == null) {
            return false;
        }
        CHANNEL.sendToServer(new AddKeybindPacket(id, keyCode, category, action));
        return true;
    }

    /**
     * Sends a client remove-keybind request packet to the server.
     *
     * @return true when sent, false when channel is unavailable
     */
    public static boolean sendRemoveKeybindToServer(String id) {
        if (CHANNEL == null) {
            return false;
        }
        CHANNEL.sendToServer(new RemoveKeybindPacket(id));
        return true;
    }

    // SyncKeybindsPacket
    private static SyncKeybindsPacket decodeSyncKeybinds(FriendlyByteBuf buf) {
        try {
            return CommonPacketCodec.decodeSyncKeybinds(buf);
        } catch (Exception e) {
            LOGGER.error("Error decoding SyncKeybindsPacket", e);
            return new SyncKeybindsPacket(new java.util.ArrayList<>());
        }
    }

    private static void encodeSyncKeybinds(SyncKeybindsPacket pkt, FriendlyByteBuf buf) {
        try {
            CommonPacketCodec.encodeSyncKeybinds(buf, pkt.getKeybinds());
        } catch (Exception e) {
            LOGGER.error("Error encoding SyncKeybindsPacket", e);
        }
    }

    private static void handleSyncKeybinds(SyncKeybindsPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            try {
                net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
                if (minecraft.player != null) {
                    dev.munebase.dynamickeybinds.forge.ForgeKeybindPersistence.handleServerSync(pkt.getKeybinds());
                }
            } catch (Exception e) {
                LOGGER.error("Error handling SyncKeybindsPacket", e);
            }
        });
        context.setPacketHandled(true);
    }

    // AddKeybindPacket
    private static AddKeybindPacket decodeAddKeybind(FriendlyByteBuf buf) {
        try {
            return CommonPacketCodec.decodeAddKeybind(buf);
        } catch (Exception e) {
            LOGGER.error("Error decoding AddKeybindPacket", e);
            return null;
        }
    }

    private static void encodeAddKeybind(AddKeybindPacket pkt, FriendlyByteBuf buf) {
        try {
            CommonPacketCodec.encodeAddKeybind(pkt, buf);
        } catch (Exception e) {
            LOGGER.error("Error encoding AddKeybindPacket", e);
        }
    }

    private static void handleAddKeybind(AddKeybindPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (pkt == null) {
            context.setPacketHandled(true);
            return;
        }
        context.enqueueWork(() -> {
            try {
                if (context.getDirection() == NetworkDirection.PLAY_TO_SERVER) {
                    net.minecraft.server.level.ServerPlayer player = context.getSender();
                    if (player != null) {
                        dev.munebase.dynamickeybinds.forge.server.ForgeServerKeybindHandler.handleAddKeybind(player, pkt);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Error handling AddKeybindPacket", e);
            }
        });
        context.setPacketHandled(true);
    }

    // RemoveKeybindPacket
    private static RemoveKeybindPacket decodeRemoveKeybind(FriendlyByteBuf buf) {
        try {
            return CommonPacketCodec.decodeRemoveKeybind(buf);
        } catch (Exception e) {
            LOGGER.error("Error decoding RemoveKeybindPacket", e);
            return null;
        }
    }

    private static void encodeRemoveKeybind(RemoveKeybindPacket pkt, FriendlyByteBuf buf) {
        try {
            CommonPacketCodec.encodeRemoveKeybind(pkt, buf);
        } catch (Exception e) {
            LOGGER.error("Error encoding RemoveKeybindPacket", e);
        }
    }

    private static void handleRemoveKeybind(RemoveKeybindPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (pkt == null) {
            context.setPacketHandled(true);
            return;
        }
        context.enqueueWork(() -> {
            try {
                if (context.getDirection() == NetworkDirection.PLAY_TO_SERVER) {
                    net.minecraft.server.level.ServerPlayer player = context.getSender();
                    if (player != null) {
                        dev.munebase.dynamickeybinds.forge.server.ForgeServerKeybindHandler.handleRemoveKeybind(player, pkt);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Error handling RemoveKeybindPacket", e);
            }
        });
        context.setPacketHandled(true);
    }

    // UpdateKeybindPacket
    private static UpdateKeybindPacket decodeUpdateKeybind(FriendlyByteBuf buf) {
        try {
            return CommonPacketCodec.decodeUpdateKeybind(buf);
        } catch (Exception e) {
            LOGGER.error("Error decoding UpdateKeybindPacket", e);
            return null;
        }
    }

    private static void encodeUpdateKeybind(UpdateKeybindPacket pkt, FriendlyByteBuf buf) {
        try {
            CommonPacketCodec.encodeUpdateKeybind(pkt, buf);
        } catch (Exception e) {
            LOGGER.error("Error encoding UpdateKeybindPacket", e);
        }
    }

    private static void handleUpdateKeybind(UpdateKeybindPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (pkt == null) {
            context.setPacketHandled(true);
            return;
        }
        context.enqueueWork(() -> {
            try {
                if (context.getDirection() == NetworkDirection.PLAY_TO_SERVER) {
                    net.minecraft.server.level.ServerPlayer player = context.getSender();
                    if (player != null) {
                        dev.munebase.dynamickeybinds.forge.server.ForgeServerKeybindHandler.handleUpdateKeybind(player, pkt);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Error handling UpdateKeybindPacket", e);
            }
        });
        context.setPacketHandled(true);
    }
}

