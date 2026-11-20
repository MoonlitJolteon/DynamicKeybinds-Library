package dev.munebase.dynamickeybinds.compat;

import org.slf4j.Logger;

import java.lang.reflect.Field;

/**
 * Optional compatibility with TooManyKeybinds (Forge), done purely by reflection so there is
 * no compile-time or runtime dependency on it.
 *
 * <p>TooManyKeybinds builds its pages once at startup from {@code Options.keyMappings}, so keybinds
 * added or removed at runtime never show up. This rebuilds its page handler after our mappings change.
 */
public final class TooManyKeybindsCompat {
    private static final String CLIENT_MOD_HANDLER = "io.github.jkaano.toomanykeybinds.client.handler.ClientModHandler";
    private static final String PAGE_HANDLER = "io.github.jkaano.toomanykeybinds.client.pages.PageHandler";
    private static final String MENU_SCREEN = "io.github.jkaano.toomanykeybinds.client.screen.MenuScreen";

    // null = not yet checked, false = absent or incompatible (stop trying)
    private static Boolean available;
    private static Field pageHandlerField;
    private static Class<?> pageHandlerClass;
    private static Field currentPageField;
    private static Field currentPartitionField;

    private TooManyKeybindsCompat() {
    }

    /**
     * Rebuilds TooManyKeybinds' pages from the current key mappings. No-op if it isn't installed.
     * Must be called on the client thread.
     */
    public static void refreshPages(Logger logger) {
        if (Boolean.FALSE.equals(available) || (available == null && !resolve(logger))) {
            return;
        }

        try {
            // Not set up yet; it will read the current mappings itself when it is
            if (pageHandlerField.get(null) == null) {
                return;
            }
            pageHandlerField.set(null, pageHandlerClass.getDeclaredConstructor().newInstance());
            // The menu remembers its page index statically; it can point past the end once the page count shrinks
            currentPageField.setInt(null, 0);
            currentPartitionField.setInt(null, 0);
        } catch (Throwable e) {
            available = false;
            logger.warn("TooManyKeybinds compat failed; dynamic keybinds may not appear in its menu", e);
        }
    }

    private static boolean resolve(Logger logger) {
        try {
            Class<?> clientModHandler = Class.forName(CLIENT_MOD_HANDLER);
            pageHandlerClass = Class.forName(PAGE_HANDLER);
            Class<?> menuScreen = Class.forName(MENU_SCREEN);

            pageHandlerField = clientModHandler.getDeclaredField("pageHandler");
            currentPageField = menuScreen.getDeclaredField("currentPage");
            currentPartitionField = menuScreen.getDeclaredField("currentPartition");
            pageHandlerField.setAccessible(true);
            currentPageField.setAccessible(true);
            currentPartitionField.setAccessible(true);

            available = true;
            logger.info("TooManyKeybinds detected; enabling page refresh compat");
        } catch (ClassNotFoundException e) {
            available = false;
        } catch (Throwable e) {
            available = false;
            logger.warn("TooManyKeybinds found but its internals changed; compat disabled", e);
        }
        return available;
    }
}
