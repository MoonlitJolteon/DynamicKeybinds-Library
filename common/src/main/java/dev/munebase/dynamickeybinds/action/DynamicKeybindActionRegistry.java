package dev.munebase.dynamickeybinds.action;

import net.minecraft.nbt.CompoundTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global registry for custom keybind action handlers.
 *
 * This static registry is the central dispatch point for dynamic keybind actions.
 * Third-party mods register handlers here, and those handlers are invoked whenever
 * a keybind with the matching action ID is pressed.
 */
public final class DynamicKeybindActionRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger("DynamicKeybinds");
    private static final Map<String, DynamicKeybindActionHandler> HANDLERS = new ConcurrentHashMap<>();

    private DynamicKeybindActionRegistry() {
    }

    /**
     * Register a handler for a specific action ID.
     *
     * @param actionID unique action identifier (for example, {@code mymod:cast_spell})
     * @param handler action handler implementation
     */
    public static void register(String actionID, DynamicKeybindActionHandler handler) {
        if (HANDLERS.containsKey(actionID)) {
            LOGGER.warn("Action handler for '{}' already registered, overwriting", actionID);
        }

        HANDLERS.put(actionID, handler);
        LOGGER.info("Registered action handler: {}", actionID);
    }

    /**
     * Dispatch an action to its registered handler.
     *
     * @param actionID action identifier
     * @param data NBT payload associated with this action instance
     */
    public static void dispatch(String actionID, CompoundTag data) {
        DynamicKeybindActionHandler handler = HANDLERS.get(actionID);
        if (handler == null) {
            LOGGER.warn("No handler registered for action: {}", actionID);
            return;
        }

        try {
            handler.onAction(actionID, data);
        } catch (Exception e) {
            LOGGER.error("Error dispatching action: {}", actionID, e);
        }
    }

    /**
     * Get a registered handler by action ID.
     *
     * @param actionID action identifier
     * @return registered handler, or {@code null} if no handler exists
     */
    public static DynamicKeybindActionHandler getHandler(String actionID) {
        return HANDLERS.get(actionID);
    }
}
