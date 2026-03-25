package dev.munebase.dynamickeybinds;

import net.minecraft.commands.CommandSourceStack;
import com.mojang.brigadier.context.CommandContext;

/**
 * Service for handling dynamic keybind commands.
 * 
 * This interface defines the contract for command operations on dynamic keybinds.
 * Implementations provide the actual command execution logic across different mod loaders.
 * 
 * <p><strong>Loader-specific Implementations:</strong></p>
 * <ul>
 *   <li>Forge: {@code ForgeCommandRegistry}</li>
 *   <li>Fabric: {@code FabricCommandRegistry}</li>
 * </ul>
 */
public interface DynamicKeyCommandService {
    /**
     * Add a new keybind via command.
     * 
     * This command is typically called by clients wishing to register a new dynamic keybind.
     * The keybind is registered locally on the client and may be persisted to the server.
     *
     * @param context the brigadier command context
     * @param id unique identifier for the keybind (e.g., "mymod:key_name")
     * @param keyCode the GLFW key code (numeric value)
     * @param category the keybind category for menu organization
     * @param actionId the action identifier (e.g., "mymod:action_id")
     * @return success status (1 for success, 0 for failure)
     */
    int addKeybind(CommandContext<CommandSourceStack> context, String id, int keyCode, String category, String actionId);

    /**
     * List all currently registered dynamic keybinds to the player.
     * 
     * Lists all keybinds in a formatted message to the command source.
     *
     * @param source the command source issuing the list command
     * @return success status (number of keybinds listed)
     */
    int listKeybinds(CommandSourceStack source);

    /**
     * Remove a dynamic keybind via command.
     * 
     * Unregisters the specified keybind, making it no longer functional.
     *
     * @param source the command source issuing the remove command
     * @param id the keybind identifier to remove
     * @return success status (1 for success, 0 for failure)
     */
    int removeKeybind(CommandSourceStack source, String id);
}
