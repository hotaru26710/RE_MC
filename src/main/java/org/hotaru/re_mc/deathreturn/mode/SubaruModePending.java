package org.hotaru.re_mc.deathreturn.mode;

public final class SubaruModePending {
    private static Object screen;
    private static boolean pending;

    private SubaruModePending() {
    }

    public static void reset(Object currentScreen) {
        screen = currentScreen;
        pending = false;
    }

    public static Object screen() {
        return screen;
    }

    public static boolean isPending() {
        return pending;
    }

    public static void setPending(boolean value) {
        pending = value;
    }

    public static boolean consumePending() {
        boolean value = pending;
        pending = false;
        screen = null;
        return value;
    }
}
