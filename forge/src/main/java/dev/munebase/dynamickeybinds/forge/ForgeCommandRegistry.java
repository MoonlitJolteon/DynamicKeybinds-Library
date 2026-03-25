package dev.munebase.dynamickeybinds.forge;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.munebase.dynamickeybinds.command.CommonDynamicKeyCommands;
import dev.munebase.dynamickeybinds.action.DynamicKeybindAction;
import dev.munebase.dynamickeybinds.forge.network.ForgeNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

/**
 * Registers Forge client-side commands for dynamic keybind management.
 */
@Mod.EventBusSubscriber(modid = "dynamickeybinds", value = Dist.CLIENT)
public final class ForgeCommandRegistry {

    private ForgeCommandRegistry() {
    }

    /**
     * Registers the `/dynamickey` command tree.
     *
     * @param event Forge client command registration event
     */
    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("dynamickey")
                .then(Commands.literal("add")
                    .then(Commands.argument("id", StringArgumentType.word())
                        .then(Commands.argument("keycode", IntegerArgumentType.integer())
                            .then(Commands.argument("category", StringArgumentType.word())
                                .executes(ctx -> add(ctx.getSource(),
                                    StringArgumentType.getString(ctx, "id"),
                                    IntegerArgumentType.getInteger(ctx, "keycode"),
                                    StringArgumentType.getString(ctx, "category"),
                                    Optional.empty()))
                                .then(Commands.argument("action", StringArgumentType.word())
                                    .executes(ctx -> add(ctx.getSource(),
                                        StringArgumentType.getString(ctx, "id"),
                                        IntegerArgumentType.getInteger(ctx, "keycode"),
                                        StringArgumentType.getString(ctx, "category"),
                                        Optional.of(new DynamicKeybindAction(StringArgumentType.getString(ctx, "action"))))))))))
                .then(Commands.literal("list").executes(ctx -> list(ctx.getSource())))
                .then(Commands.literal("remove")
                    .then(Commands.argument("id", StringArgumentType.word())
                        .executes(ctx -> remove(ctx.getSource(), StringArgumentType.getString(ctx, "id")))))
        );
    }

    /**
     * Sends a request to the server to add a dynamic keybind.
     */
    private static int add(CommandSourceStack source, String id, int keyCode, String category, Optional<DynamicKeybindAction> action) {
        CompoundTag defaultActionData = new CompoundTag();
        defaultActionData.putString("KeyID", id);
        Optional<DynamicKeybindAction> effectiveAction = action.isPresent()
            ? action
            : CommonDynamicKeyCommands.createDefaultDebugAction(id, defaultActionData);

        if (!ForgeNetworking.sendAddKeybindToServer(id, keyCode, category, effectiveAction)) {
            source.sendSystemMessage(Component.literal(CommonDynamicKeyCommands.formatNetworkingNotInitializedMessage()));
            return 0;
        }

        source.sendSystemMessage(Component.literal(CommonDynamicKeyCommands.formatAddRequestMessage(id)));
        return 1;
    }

    /**
     * Lists client-known dynamic keybinds.
     */
    private static int list(CommandSourceStack source) {
        var lines = CommonDynamicKeyCommands.formatListOutput();
        for (String line : lines) {
            source.sendSystemMessage(Component.literal(line));
        }
        return CommonDynamicKeyCommands.listResultCode(lines);
    }

    /**
     * Sends a request to the server to remove a dynamic keybind.
     */
    private static int remove(CommandSourceStack source, String id) {
        if (!ForgeNetworking.sendRemoveKeybindToServer(id)) {
            source.sendSystemMessage(Component.literal(CommonDynamicKeyCommands.formatNetworkingNotInitializedMessage()));
            return 0;
        }

        source.sendSystemMessage(Component.literal(CommonDynamicKeyCommands.formatRemoveRequestMessage(id)));
        return 1;
    }
}
