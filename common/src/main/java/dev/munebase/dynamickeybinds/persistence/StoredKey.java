package dev.munebase.dynamickeybinds.persistence;

/**
 * A bound input: its type plus the code within that type. Plain strings keep this free of client
 * classes so the dedicated server can persist and relay it.
 *
 * @param type one of {@link #KEYSYM}, {@link #SCANCODE}, {@link #MOUSE}
 * @param code GLFW key code, scancode or mouse button, depending on type (-1 = unbound)
 */
public record StoredKey(String type, int code) {
    public static final String KEYSYM = "keysym";
    public static final String SCANCODE = "scancode";
    public static final String MOUSE = "mouse";

    public static StoredKey keysym(int code) {
        return new StoredKey(KEYSYM, code);
    }

    /** Saves written before the type was stored: keyboard codes are -1 or 32+, so 0-7 was always a mouse button. */
    public static StoredKey legacy(int code) {
        return code >= 0 && code <= 7 ? new StoredKey(MOUSE, code) : keysym(code);
    }
}
