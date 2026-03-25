package dev.munebase.dynamickeybinds;

import net.minecraft.commands.CommandSourceStack;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

/**
 * Default implementation of DynamicKeyCommandService.
 * 
 * Provides command execution logic for managing dynamic keybinds.
 * Handles validation, error reporting, and feedback to the command source.
 * 
 * <p><strong>Note:</strong> This implementation registers keybinds with empty actions.
 * Third-party mods should register action handlers separately via
 * {@link dev.munebase.dynamickeybinds.action.DynamicKeybindActionRegistry}.</p>
 */
public class DynamicKeyCommandServiceImpl implements DynamicKeyCommandService {
    /** The keybind registry to operate on */
    private final DynamicKeyRegistry registry;

    /**
     * Creates a new command service with the given registry.
     *
     * @param registry the DynamicKeyRegistry to use for keybind operations
     */
    public DynamicKeyCommandServiceImpl(DynamicKeyRegistry registry) {
        this.registry = registry;
    }

    @Override
    public int addKeybind(CommandContext<CommandSourceStack> context, String id, int keyCode, String category, String actionId) {
        CommandSourceStack source = context.getSource();

        try {
            // Register with empty action - actions should be registered separately via DynamicKeybindActionRegistry
            registry.registerDynamicKey(id, keyCode, category, java.util.Optional.empty());
            source.sendSuccess(() -> Component.literal("§aKeybind '§b" + id + "§a' added successfully."), true);
            return 1;
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }
    }

    @Override
    public int listKeybinds(CommandSourceStack source) {
        int count = 0;
        StringBuilder sb = new StringBuilder("§aDynamic Keybinds:\n");

        for (KeyMapping keyBinding : registry.getAllDynamicKeys()) {
            String name = keyBinding.getName();
            sb.append("  §b").append(name).append("\n");
            count++;
        }

        if (count == 0) {
            source.sendSuccess(() -> Component.literal("§7No dynamic keybinds registered."), false);
        } else {
            source.sendSuccess(() -> Component.literal(sb.toString()), false);
        }

        return count;
    }

    @Override
    public int removeKeybind(CommandSourceStack source, String id) {
        KeyMapping keyBinding = registry.getKeyBindById(id);

        if (keyBinding == null) {
            source.sendFailure(Component.literal("§cKeybind '§b" + id + "§c' not found."));
            return 0;
        }

        registry.unregisterDynamicKey(keyBinding);
        source.sendSuccess(() -> Component.literal("§aKeybind '§b" + id + "§a' removed successfully."), true);
        return 1;
    }
}
