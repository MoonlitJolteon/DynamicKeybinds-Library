package dev.munebase.dynamickeybinds.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Network packet sent from client to server to request updating a keybind's key code.
 * 
 * Allows clients to rebind an existing keybind to a different key without removing
 * it. This is used when players customize their keybind settings. The update is
 * persisted on the server and synchronized to all clients.
 * 
 * <p><strong>Direction:</strong> Client -> Server</p>
 * <p><strong>Handler:</strong> NeoForgeServerKeybindHandler or FabricServerKeybindHandler</p>
 */
public class UpdateKeybindPacket implements CustomPacketPayload {
    public static final Type<UpdateKeybindPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath("dynamickeybinds", "update_keybind"));

    public static final StreamCodec<FriendlyByteBuf, UpdateKeybindPacket> STREAM_CODEC = StreamCodec.of(
        (buf, packet) -> CommonPacketCodec.encodeUpdateKeybind(packet, buf),
        CommonPacketCodec::decodeUpdateKeybind
    );

    /** ID of the keybind to update */
    private final String id;
    
    /** New GLFW key code for this keybind */
    private final int keyCode;

    /**
     * Construct a new UpdateKeybindPacket.
     *
     * @param id the keybind ID to update
     * @param keyCode the new GLFW key code
     */
    public UpdateKeybindPacket(String id, int keyCode) {
        this.id = id;
        this.keyCode = keyCode;
    }

    /**
     * Get the keybind ID to update.
     *
     * @return the keybind identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Get the new key code.
     *
     * @return the new GLFW key code
     */
    public int getKeyCode() {
        return keyCode;
    }

    @Override
    public Type<UpdateKeybindPacket> type() {
        return TYPE;
    }
}
