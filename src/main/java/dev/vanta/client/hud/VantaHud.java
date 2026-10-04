package dev.vanta.client.hud;

import dev.vanta.client.VantaClient;
import dev.vanta.client.util.Reflect;
import dev.vanta.client.util.VantaSettings;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public final class VantaHud {
    private static final int ACCENT = 0xFFB15CFF;
    private static final int ACCENT2 = 0xFF7436C7;
    private static final int WHITE = 0xFFF6F0FF;
    private static final int MUTED = 0xFFAA9CB8;
    private static final int PANEL = 0xD9100B18;
    private static final Map<String, float[]> CURRENT = new HashMap<>();
    private static final Map<String, Integer> EFFECT_MAX = new HashMap<>();
    private static boolean zoomHeld;
    private static Double oldFov;

    private VantaHud() {}
    public static void setZoomHeld(boolean held) { zoomHeld = held; }

    public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        applyZoom(mc);

        int[] stats = pos("stats");
        int sy = stats[1];
        if (VantaClient.MODULES.on("FPS Counter")) sy = infoRow(g, mc, stats[0], sy, "FPS", Integer.toString(fps(mc)));
        if (VantaClient.MODULES.on("Ping Display")) sy = infoRow(g, mc, stats[0], sy, "PING", ping(mc) + " ms");
        if (VantaClient.MODULES.on("Coordinates")) sy = infoRow(g, mc, stats[0], sy, "XYZ", String.format("%.1f  %.1f  %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ()));
        if (VantaClient.MODULES.on("Direction")) sy = infoRow(g, mc, stats[0], sy, "DIR", direction(mc.player.getYRot()));
        if (VantaClient.MODULES.on("CPS Counter")) sy = infoRow(g, mc, stats[0], sy, "CPS", Integer.toString(VantaClient.cps()));
        if (VantaClient.MODULES.on("Memory Usage")) {
            Runtime r = Runtime.getRuntime();
            long used = (r.totalMemory() - r.freeMemory()) / 1024 / 1024;
            long max = r.maxMemory() / 1024 / 1024;
            sy = infoRow(g, mc, stats[0], sy, "RAM", used + "/" + max + " MB");
        }
        if (VantaClient.MODULES.on("Clock")) sy = infoRow(g, mc, stats[0], sy, "TIME", LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        if (VantaClient.MODULES.on("Server Address")) sy = infoRow(g, mc, stats[0], sy, "SERVER", server(mc));
        if (VantaClient.MODULES.on("Player Count")) sy = infoRow(g, mc, stats[0], sy, "PLAYERS", playerCount(mc));

        if (VantaClient.MODULES.on("Saturation HUD")) drawSaturation(g, mc, pos("saturation"));
        if (VantaClient.MODULES.on("Armor HUD")) drawArmor(g, mc, pos("armor"));
        if (VantaClient.MODULES.on("Totem Counter")) drawTotems(g, mc, pos("totems"));
        if (VantaClient.MODULES.on("Status Effect Bars")) drawEffects(g, mc, pos("effects"));
        if (VantaClient.MODULES.on("Keystrokes")) drawKeys(g, mc, pos("keys"));
        if (VantaClient.MODULES.on("Crosshair")) drawCrosshair(g, mc);
    }

    private static int[] pos(String id) {
        HudLayout.Pos target = HudLayout.get(id);
        float[] p = CURRENT.computeIfAbsent(id, k -> new float[]{target.x(), target.y()});
        float speed = VantaSettings.smoothHud ? 0.22f : 1f;
        p[0] += (target.x() - p[0]) * speed;
        p[1] += (target.y() - p[1]) * speed;
        return new int[]{Math.round(p[0]), Math.round(p[1])};
    }

    private static int infoRow(GuiGraphicsExtractor g, Minecraft mc, int x, int y, String label, String value) {
        int w = Math.max(104, mc.font.width(label + "  " + value) + 14);
        panel(g, x, y, w, 15);
        g.fill(x, y, x + 2, y + 15, ACCENT);
        g.text(mc.font, label, x + 6, y + 3, ACCENT, true);
        g.text(mc.font, value, x + 9 + mc.font.width(label), y + 3, WHITE, false);
        return y + 17;
    }

    /** AppleSkin-style information without copying AppleSkin textures/code. */
    private static void drawSaturation(GuiGraphicsExtractor g, Minecraft mc, int[] p) {
        int x = p[0], y = p[1];
        int food = mc.player.getFoodData().getFoodLevel();
        float sat = mc.player.getFoodData().getSaturationLevel();
        float exhaustion = 0f;
        Object ex = Reflect.call(mc.player.getFoodData(), "getExhaustionLevel");
        if (ex instanceof Number n) exhaustion = n.floatValue();
        panel(g, x, y, 126, 31);
        g.text(mc.font, "SATURATION", x + 7, y + 4, ACCENT, true);
        g.text(mc.font, String.format("%.1f", sat), x + 93, y + 4, WHITE, false);
        int px = x + 7, py = y + 17;
        for (int i = 0; i < 10; i++) {
            int hungerNeed = (i + 1) * 2;
            int bg = hungerNeed <= food ? 0xFF49384F : 0xFF241B2B;
            g.fill(px + i * 11, py, px + i * 11 + 9, py + 7, bg);
            float pipSat = Math.max(0f, Math.min(2f, sat - i * 2f));
            int fill = Math.round((pipSat / 2f) * 9f);
            if (fill > 0) g.fill(px + i * 11, py, px + i * 11 + fill, py + 7, ACCENT);
        }
        int exW = Math.min(108, Math.round((exhaustion / 4f) * 108f));
        if (exW > 0) g.fill(x + 7, y + 27, x + 7 + exW, y + 29, 0xFFEA8CFF);
    }

    /** Minimal vanilla-like armor widget with real item models and durability. */
    private static void drawArmor(GuiGraphicsExtractor g, Minecraft mc, int[] p) {
        int x = p[0], y = p[1];
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        panel(g, x, y, 106, 29);
        g.text(mc.font, "ARMOR", x + 6, y + 3, ACCENT, true);
        for (int i = 0; i < slots.length; i++) {
            ItemStack s = mc.player.getItemBySlot(slots[i]);
            int ix = x + 7 + i * 24;
            if (!s.isEmpty()) {
                try { g.item(s, ix, y + 10); } catch (Throwable ignored) {}
                if (s.isDamageableItem()) {
                    int left = Math.max(0, s.getMaxDamage() - s.getDamageValue());
                    int pct = s.getMaxDamage() == 0 ? 100 : Math.round(left * 100f / s.getMaxDamage());
                    int c = pct <= 15 ? 0xFFFF5D73 : pct <= 35 ? 0xFFFFCA5C : 0xFF9CFFB2;
                    g.text(mc.font, Integer.toString(pct), ix + 4, y + 21, c, true);
                }
            } else {
                g.outline(ix, y + 10, 16, 16, 0x553D2B50);
            }
        }
    }

    private static void drawTotems(GuiGraphicsExtractor g, Minecraft mc, int[] p) {
        int x = p[0], y = p[1], count = countTotems(mc);
        panel(g, x, y, 70, 25);
        ItemStack icon = new ItemStack(Items.TOTEM_OF_UNDYING);
        try { g.item(icon, x + 5, y + 5); } catch (Throwable ignored) {}
        g.text(mc.font, "x" + count, x + 27, y + 9, count > 0 ? WHITE : 0xFF746B7D, true);
    }

    /** Status-effect bars with a timer; max duration is learned from the longest duration seen. */
    private static void drawEffects(GuiGraphicsExtractor g, Minecraft mc, int[] p) {
        int x = p[0], y = p[1], shown = 0;
        for (Object inst : mc.player.getActiveEffects()) {
            if (shown >= 6) break;
            int duration = Reflect.intCall(inst, "getDuration", 0);
            Object effectHolder = Reflect.call(inst, "getEffect");
            Object effect = Reflect.call(effectHolder, "value");
            Object display = Reflect.call(effect, "getDisplayName");
            String name = display instanceof Component c ? c.getString() : "Effect";
            int amplifier = Reflect.intCall(inst, "getAmplifier", 0);
            String key = name + ":" + amplifier;
            int max = Math.max(duration, EFFECT_MAX.getOrDefault(key, duration));
            EFFECT_MAX.put(key, max);
            float ratio = max <= 0 ? 1f : Math.max(0f, Math.min(1f, duration / (float) max));
            int yy = y + shown * 21;
            panel(g, x, yy, 142, 19);
            String level = amplifier > 0 ? " " + (amplifier + 1) : "";
            g.text(mc.font, name + level, x + 6, yy + 3, WHITE, false);
            g.text(mc.font, durationText(duration), x + 105, yy + 3, MUTED, false);
            g.fill(x + 5, yy + 14, x + 137, yy + 17, 0xFF241A2E);
            g.fill(x + 5, yy + 14, x + 5 + Math.round(132 * ratio), yy + 17, ACCENT);
            shown++;
        }
    }

    private static String durationText(int ticks) {
        int sec = Math.max(0, ticks / 20); return String.format("%d:%02d", sec / 60, sec % 60);
    }

    private static void drawKeys(GuiGraphicsExtractor g, Minecraft mc, int[] p) {
        int x = p[0], y = p[1];
        boolean w = Reflect.keyDown(mc.options, "keyUp"), a = Reflect.keyDown(mc.options, "keyLeft");
        boolean s = Reflect.keyDown(mc.options, "keyDown"), d = Reflect.keyDown(mc.options, "keyRight");
        key(g, mc, x + 20, y, "W", w); key(g, mc, x, y + 20, "A", a); key(g, mc, x + 20, y + 20, "S", s); key(g, mc, x + 40, y + 20, "D", d);
    }
    private static void key(GuiGraphicsExtractor g, Minecraft mc, int x, int y, String t, boolean down) {
        g.fill(x, y, x + 18, y + 18, down ? 0xE58C41E8 : PANEL); g.outline(x, y, 18, 18, down ? ACCENT : 0x66402B55); g.text(mc.font, t, x + 6, y + 5, down ? WHITE : ACCENT, true);
    }
    private static void drawCrosshair(GuiGraphicsExtractor g, Minecraft mc) {
        int cx = mc.getWindow().getGuiScaledWidth()/2, cy = mc.getWindow().getGuiScaledHeight()/2;
        g.fill(cx - 7, cy, cx - 2, cy + 1, ACCENT); g.fill(cx + 3, cy, cx + 8, cy + 1, ACCENT); g.fill(cx, cy - 7, cx + 1, cy - 2, ACCENT); g.fill(cx, cy + 3, cx + 1, cy + 8, ACCENT);
    }
    private static void panel(GuiGraphicsExtractor g, int x, int y, int w, int h) { g.fill(x, y, x + w, y + h, PANEL); g.outline(x, y, w, h, 0x66381F50); }
    private static int fps(Minecraft mc) { return Reflect.intCall(mc, "getFps", 0); }
    private static int ping(Minecraft mc) {
        Object conn = Reflect.call(mc, "getConnection"), info = conn == null ? null : Reflect.call(conn, "getPlayerInfo", mc.player.getUUID());
        int p = Reflect.intCall(info, "getLatency", -1); if (p < 0) p = Reflect.intCall(info, "getPing", 0); return Math.max(0, p);
    }
    private static String server(Minecraft mc) { Object s = Reflect.call(mc, "getCurrentServer"), ip = s == null ? null : Reflect.field(s, "ip"); return ip == null ? "Singleplayer" : String.valueOf(ip); }
    private static String playerCount(Minecraft mc) { Object c = Reflect.call(mc, "getConnection"), l = c == null ? null : Reflect.call(c, "getOnlinePlayers"); return l instanceof java.util.Collection<?> col ? Integer.toString(col.size()) : "1"; }
    private static int countTotems(Minecraft mc) {
        int count = 0; var inv = mc.player.getInventory();
        for (int i=0;i<inv.getContainerSize();i++) { ItemStack s=inv.getItem(i); if (!s.isEmpty() && s.getItem()==Items.TOTEM_OF_UNDYING) count += s.getCount(); }
        return count;
    }
    private static String direction(float yaw) { return switch (Math.floorMod(Math.round(yaw/90f),4)) { case 0 -> "South"; case 1 -> "West"; case 2 -> "North"; default -> "East"; }; }

    private static void applyZoom(Minecraft mc) {
        if (!VantaClient.MODULES.on("Zoom")) { restoreFov(mc); return; }
        Object current = Reflect.getOption(mc.options, "fov"); if (!(current instanceof Number n)) return;
        if (zoomHeld) { if (oldFov == null) oldFov = n.doubleValue(); int now = n.intValue(); int target = VantaSettings.zoomFov; int next = Math.max(target, now - Math.max(1, (now-target)/3)); Reflect.setOption(mc.options, next, "fov"); }
        else restoreFov(mc);
    }
    private static void restoreFov(Minecraft mc) { if (oldFov != null) { Reflect.setOption(mc.options, oldFov.intValue(), "fov"); oldFov = null; } }
}
