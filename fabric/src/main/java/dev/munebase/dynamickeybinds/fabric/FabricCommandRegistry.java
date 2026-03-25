package dev.munebase.dynamickeybinds.fabric;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.munebase.dynamickeybinds.action.DynamicKeybindAction;
import dev.munebase.dynamickeybinds.command.CommonDynamicKeyCommands;
import dev.munebase.dynamickeybinds.fabric.network.FabricNetworking;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import java.util.Optional;

/**
 * Registers Fabric client commands for managing dynamic keybinds.
 */
@Environment(EnvType.CLIENT)
public final class FabricCommandRegistry {
    private FabricCommandRegistry() {
    }

    /**
     * Registers the `/dynamickey` client command tree.
     */
    public static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                ClientCommandManager.literal("dynamickey")
                    .then(ClientCommandManager.literal("add")
                        .then(ClientCommandManager.argument("id", StringArgumentType.word())
                            .then(ClientCommandManager.argument("keycode", IntegerArgumentType.integer())
                                .then(ClientCommandManager.argument("category", StringArgumentType.word())
                                    .executes(ctx -> add(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "id"),
                                        IntegerArgumentType.getInteger(ctx, "keycode"),
                                        StringArgumentType.getString(ctx, "category"),
                                        Optional.empty()))
                                    .then(ClientCommandManager.argument("action", StringArgumentType.word())
                                        .executes(ctx -> add(ctx.getSource(),
                                            StringArgumentType.getString(ctx, "id"),
                                            IntegerArgumentType.getInteger(ctx, "keycode"),
                                            StringArgumentType.getString(ctx, "category"),
                                            Optional.of(new DynamicKeybindAction(StringArgumentType.getString(ctx, "action"))))))))))
                    .then(ClientCommandManager.literal("list")
                        .executes(ctx -> list(ctx.getSource())))
                    .then(ClientCommandManager.literal("remove")
                        .then(ClientCommandManager.argument("id", StringArgumentType.word())
                            .executes(ctx -> remove(ctx.getSource(), StringArgumentType.getString(ctx, "id")))))
            );
        });
    }

    /**
     * Sends an add-keybind request to the server.
        *
        * @param source command source
        * @param id unique keybind identifier
        * @param keyCode GLFW key code
        * @param category keybind category
        * @return command result code
     */
    private static int add(FabricClientCommandSource source, String id, int keyCode, String category, Optional<DynamicKeybindAction> action) {
        CompoundTag data = new CompoundTag();
        data.putString("KeyID", id);
        Optional<DynamicKeybindAction> effectiveAction = action.isPresent()
            ? action
            : CommonDynamicKeyCommands.createDefaultDebugAction(id, data);

        // Send packet to server
        FabricNetworking.sendAddKeybindToServer(id, keyCode, category, effectiveAction);
        source.sendFeedback(Component.literal(CommonDynamicKeyCommands.formatAddRequestMessage(id)));
        return 1;
    }

    /**
     * Lists currently known dynamic keybind IDs on the client.
        *
        * @param source command source
        * @return number of listed keybinds
     */
    private static int list(FabricClientCommandSource source) {
        var lines = CommonDynamicKeyCommands.formatListOutput();
        for (String line : lines) {
            source.sendFeedback(Component.literal(line));
        }
        return CommonDynamicKeyCommands.listResultCode(lines);
    }

    /**
     * Sends a remove-keybind request to the server.
        *
        * @param source command source
        * @param id keybind identifier to remove
        * @return command result code
     */
    private static int remove(FabricClientCommandSource source, String id) {
        // Send packet to server
        FabricNetworking.sendRemoveKeybindToServer(id);
        source.sendFeedback(Component.literal(CommonDynamicKeyCommands.formatRemoveRequestMessage(id)));
        return 1;
    }
}
