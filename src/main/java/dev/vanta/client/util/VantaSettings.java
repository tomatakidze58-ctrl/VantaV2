package dev.vanta.client.util;

/** Runtime tweak values. Kept deliberately small and renderer-safe. */
public final class VantaSettings {
    private VantaSettings() {}

    public static float lowFireOffset = 0.35f;
    public static float lowShieldOffset = 0.22f;
    public static float itemScale = 0.85f;
    public static int zoomFov = 30;
    public static boolean smoothHud = true;
}
