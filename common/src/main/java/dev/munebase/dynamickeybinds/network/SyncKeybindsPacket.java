package dev.munebase.dynamickeybinds.network;

import dev.munebase.dynamickeybinds.persistence.StoredKeybind;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Packet sent from server to client to sync all keybinds for the joining player.
 */
public class SyncKeybindsPacket implements CustomPacketPayload {
    public static final Type<SyncKeybindsPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath("dynamickeybinds", "sync_keybinds"));

    public static final StreamCodec<FriendlyByteBuf, SyncKeybindsPacket> STREAM_CODEC = StreamCodec.of(
        (buf, packet) -> CommonPacketCodec.encodeSyncKeybinds(buf, packet.getKeybinds()),
        CommonPacketCodec::decodeSyncKeybinds
    );

    private final List<StoredKeybind> keybinds;

    /**
     * Creates a sync packet with the given keybinds.
     * @param keybinds The keybinds to sync
     */
    public SyncKeybindsPacket(List<StoredKeybind> keybinds) {
        this.keybinds = keybinds;
    }

    /**
     * Gets the keybinds in this packet.
     * @return The list of keybinds
     */
    public List<StoredKeybind> getKeybinds() {
        return keybinds;
    }

    @Override
    public Type<SyncKeybindsPacket> type() {
        return TYPE;
    }
}
