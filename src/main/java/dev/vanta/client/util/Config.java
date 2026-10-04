package dev.vanta.client.util;

import dev.vanta.client.hud.HudLayout;
import dev.vanta.client.module.Module;
import dev.vanta.client.module.ModuleManager;
import net.fabricmc.loader.api.FabricLoader;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class Config {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("vantaclient.properties");
    private Config() {}

    public static void load(ModuleManager modules) {
        if (!Files.exists(FILE)) return;
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(FILE)) {
            p.load(in);
            for (Module m : modules.all()) {
                String v = p.getProperty("module." + m.name);
                if (v != null) m.enabled = Boolean.parseBoolean(v);
            }
            for (String id : HudLayout.all().keySet()) {
                HudLayout.Pos fallback = HudLayout.get(id);
                int x = parseInt(p.getProperty("hud." + id + ".x"), fallback.x());
                int y = parseInt(p.getProperty("hud." + id + ".y"), fallback.y());
                HudLayout.put(id, x, y);
            }
            VantaSettings.lowFireOffset = parseFloat(p.getProperty("setting.lowFireOffset"), VantaSettings.lowFireOffset);
            VantaSettings.lowShieldOffset = parseFloat(p.getProperty("setting.lowShieldOffset"), VantaSettings.lowShieldOffset);
            VantaSettings.itemScale = parseFloat(p.getProperty("setting.itemScale"), VantaSettings.itemScale);
            VantaSettings.zoomFov = parseInt(p.getProperty("setting.zoomFov"), VantaSettings.zoomFov);
            VantaSettings.smoothHud = Boolean.parseBoolean(p.getProperty("setting.smoothHud", Boolean.toString(VantaSettings.smoothHud)));
        } catch (Exception ignored) {}
    }

    public static void save(ModuleManager modules) {
        Properties p = new Properties();
        for (Module m : modules.all()) p.setProperty("module." + m.name, Boolean.toString(m.enabled));
        for (var e : HudLayout.all().entrySet()) {
            p.setProperty("hud." + e.getKey() + ".x", Integer.toString(e.getValue().x()));
            p.setProperty("hud." + e.getKey() + ".y", Integer.toString(e.getValue().y()));
        }
        p.setProperty("setting.lowFireOffset", Float.toString(VantaSettings.lowFireOffset));
        p.setProperty("setting.lowShieldOffset", Float.toString(VantaSettings.lowShieldOffset));
        p.setProperty("setting.itemScale", Float.toString(VantaSettings.itemScale));
        p.setProperty("setting.zoomFov", Integer.toString(VantaSettings.zoomFov));
        p.setProperty("setting.smoothHud", Boolean.toString(VantaSettings.smoothHud));
        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream out = Files.newOutputStream(FILE)) { p.store(out, "Vanta Client"); }
        } catch (Exception ignored) {}
    }

    private static int parseInt(String value, int fallback) {
        try { return value == null ? fallback : Integer.parseInt(value); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    private static float parseFloat(String value, float fallback) {
        try { return value == null ? fallback : Float.parseFloat(value); }
        catch (NumberFormatException ignored) { return fallback; }
    }
}
