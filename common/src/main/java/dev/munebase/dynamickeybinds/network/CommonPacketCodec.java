package dev.munebase.dynamickeybinds.network;

import dev.munebase.dynamickeybinds.action.DynamicKeybindAction;
import dev.munebase.dynamickeybinds.persistence.StoredKeybind;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Shared packet serialization helpers used by both loader networking layers.
 */
public final class CommonPacketCodec {
    private CommonPacketCodec() {
    }

    /**
     * Decodes a sync keybinds packet from the buffer.
     * @param buf The buffer to read from
     * @return The decoded packet
     */
    public static SyncKeybindsPacket decodeSyncKeybinds(FriendlyByteBuf buf) {
        int count = buf.readInt();
        List<StoredKeybind> keybinds = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String id = buf.readUtf();
            int keyCode = buf.readInt();
            String category = buf.readUtf();
            Optional<DynamicKeybindAction> action = readOptionalAction(buf);
            keybinds.add(new StoredKeybind(id, keyCode, category, action));
        }
        return new SyncKeybindsPacket(keybinds);
    }

    /**
     * Encodes a list of keybinds into the buffer.
     * @param buf The buffer to write to
     * @param keybinds The keybinds to encode
     */
    public static void encodeSyncKeybinds(FriendlyByteBuf buf, List<StoredKeybind> keybinds) {
        buf.writeInt(keybinds.size());
        for (StoredKeybind keybind : keybinds) {
            buf.writeUtf(keybind.id());
            buf.writeInt(keybind.keyCode());
            buf.writeUtf(keybind.category());
            writeOptionalAction(buf, keybind.action());
        }
    }

    /**
     * Decodes an add keybind packet from the buffer.
     * @param buf The buffer to read from
     * @return The decoded packet
     */
    public static AddKeybindPacket decodeAddKeybind(FriendlyByteBuf buf) {
        String id = buf.readUtf();
        int keyCode = buf.readInt();
        String category = buf.readUtf();
        Optional<DynamicKeybindAction> action = readOptionalAction(buf);
        return new AddKeybindPacket(id, keyCode, category, action);
    }

    /**
     * Encodes an add keybind packet into the buffer.
     * @param pkt The packet to encode
     * @param buf The buffer to write to
     */
    public static void encodeAddKeybind(AddKeybindPacket pkt, FriendlyByteBuf buf) {
        buf.writeUtf(pkt.getId());
        buf.writeInt(pkt.getKeyCode());
        buf.writeUtf(pkt.getCategory());
        writeOptionalAction(buf, pkt.getAction());
    }

    /**
     * Decodes a remove keybind packet from the buffer.
     * @param buf The buffer to read from
     * @return The decoded packet
     */
    public static RemoveKeybindPacket decodeRemoveKeybind(FriendlyByteBuf buf) {
        String id = buf.readUtf();
        return new RemoveKeybindPacket(id);
    }

    /**
     * Encodes a remove keybind packet into the buffer.
     * @param pkt The packet to encode
     * @param buf The buffer to write to
     */
    public static void encodeRemoveKeybind(RemoveKeybindPacket pkt, FriendlyByteBuf buf) {
        buf.writeUtf(pkt.getId());
    }

    /**
     * Decodes an update keybind packet from the buffer.
     * @param buf The buffer to read from
     * @return The decoded packet
     */
    public static UpdateKeybindPacket decodeUpdateKeybind(FriendlyByteBuf buf) {
        String id = buf.readUtf();
        int keyCode = buf.readInt();
        return new UpdateKeybindPacket(id, keyCode);
    }

    /**
     * Encodes an update keybind packet into the buffer.
     * @param pkt The packet to encode
     * @param buf The buffer to write to
     */
    public static void encodeUpdateKeybind(UpdateKeybindPacket pkt, FriendlyByteBuf buf) {
        buf.writeUtf(pkt.getId());
        buf.writeInt(pkt.getKeyCode());
    }

    private static Optional<DynamicKeybindAction> readOptionalAction(FriendlyByteBuf buf) {
        if (!buf.readBoolean()) {
            return Optional.empty();
        }
        String actionId = buf.readUtf();
        CompoundTag data = buf.readNbt();
        return Optional.of(new DynamicKeybindAction(actionId, data == null ? new CompoundTag() : data));
    }

    private static void writeOptionalAction(FriendlyByteBuf buf, Optional<DynamicKeybindAction> action) {
        Optional<DynamicKeybindAction> safeAction = action == null ? Optional.empty() : action;
        buf.writeBoolean(safeAction.isPresent());
        if (safeAction.isPresent()) {
            DynamicKeybindAction value = safeAction.get();
            buf.writeUtf(value.actionID());
            buf.writeNbt(value.data());
        }
    }
}
