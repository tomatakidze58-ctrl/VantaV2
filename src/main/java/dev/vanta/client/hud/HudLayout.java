package dev.vanta.client.hud;

import java.util.LinkedHashMap;
import java.util.Map;

/** Target HUD positions. Actual on-screen positions ease toward these values. */
public final class HudLayout {
    public record Pos(int x, int y) {}
    private static final Map<String, Pos> POSITIONS = new LinkedHashMap<>();

    static {
        put("stats", 8, 8);
        put("saturation", 8, 92);
        put("armor", 8, 126);
        put("totems", 8, 164);
        put("effects", 8, 194);
        put("keys", 8, 282);
    }

    private HudLayout() {}

    public static Pos get(String id) { return POSITIONS.getOrDefault(id, new Pos(8, 8)); }
    public static void put(String id, int x, int y) { POSITIONS.put(id, new Pos(Math.max(0, x), Math.max(0, y))); }
    public static Map<String, Pos> all() { return Map.copyOf(POSITIONS); }
}
