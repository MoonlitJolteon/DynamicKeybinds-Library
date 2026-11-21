package dev.munebase.dynamickeybinds.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Network packet sent from client to server to request removing a keybind.
 * 
 * Allows clients to unregister a previously added keybind. The keybind is removed
 * from the server-side registry and the removal is synchronized to all clients.
 * 
 * <p><strong>Direction:</strong> Client -> Server</p>
 * <p><strong>Handler:</strong> Server-side keybind handler (NeoForgeServerKeybindHandler or FabricServerKeybindHandler)</p>
 */
public class RemoveKeybindPacket implements CustomPacketPayload {
    public static final Type<RemoveKeybindPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath("dynamickeybinds", "remove_keybind"));

    public static final StreamCodec<FriendlyByteBuf, RemoveKeybindPacket> STREAM_CODEC = StreamCodec.of(
        (buf, packet) -> CommonPacketCodec.encodeRemoveKeybind(packet, buf),
        CommonPacketCodec::decodeRemoveKeybind
    );

    /** ID of the keybind to remove */
    private final String id;

    /**
     * Construct a new RemoveKeybindPacket.
     *
     * @param id the keybind ID to remove
     */
    public RemoveKeybindPacket(String id) {
        this.id = id;
    }

    /**
     * Get the keybind ID to remove.
     *
     * @return the keybind identifier
     */
    public String getId() {
        return id;
    }

    @Override
    public Type<RemoveKeybindPacket> type() {
        return TYPE;
    }
}
