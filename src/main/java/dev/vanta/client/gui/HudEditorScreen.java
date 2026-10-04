package dev.vanta.client.gui;

import dev.vanta.client.VantaClient;
import dev.vanta.client.hud.HudLayout;
import dev.vanta.client.util.Config;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/** Drag the translucent boxes with the left mouse button. */
public final class HudEditorScreen extends Screen {
    private final Map<String, int[]> sizes = new LinkedHashMap<>();
    private String dragging;
    private double grabX, grabY;

    public HudEditorScreen() {
        super(Component.literal("Vanta HUD Editor"));
        sizes.put("stats", new int[]{128, 78}); sizes.put("saturation", new int[]{126, 31});
        sizes.put("armor", new int[]{106, 29}); sizes.put("totems", new int[]{70, 25});
        sizes.put("effects", new int[]{142, 82}); sizes.put("keys", new int[]{58, 38});
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        g.fill(0, 0, width, height, 0x99100618);
        g.centeredText(font, "VANTA HUD EDITOR", width/2, 14, 0xFFF5EDFF, true);
        g.centeredText(font, "Drag widgets • Right Ctrl opens this screen", width/2, 30, 0xFFAF9ABE, false);
        for (var e : sizes.entrySet()) {
            HudLayout.Pos p = HudLayout.get(e.getKey()); int[] s = e.getValue();
            boolean active = e.getKey().equals(dragging);
            g.fill(p.x(), p.y(), p.x()+s[0], p.y()+s[1], active ? 0x995D249A : 0x77130B1E);
            g.outline(p.x(), p.y(), s[0], s[1], active ? 0xFFB15CFF : 0xAA623789);
            g.text(font, label(e.getKey()), p.x()+5, p.y()+5, 0xFFEADFFF, true);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            for (var e : sizes.entrySet()) {
                HudLayout.Pos p = HudLayout.get(e.getKey()); int[] s = e.getValue();
                if (event.x() >= p.x() && event.x() <= p.x()+s[0] && event.y() >= p.y() && event.y() <= p.y()+s[1]) {
                    dragging = e.getKey(); grabX = event.x()-p.x(); grabY = event.y()-p.y(); return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (dragging != null) {
            int[] s = sizes.get(dragging);
            int nx = (int)Math.round(event.x()-grabX), ny = (int)Math.round(event.y()-grabY);
            nx = Math.max(0, Math.min(width-s[0], nx)); ny = Math.max(42, Math.min(height-s[1], ny));
            HudLayout.put(dragging, nx, ny); return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (dragging != null) { dragging = null; Config.save(VantaClient.MODULES); return true; }
        return super.mouseReleased(event);
    }

    private static String label(String id) { return switch (id) { case "stats" -> "Info"; case "saturation" -> "Saturation"; case "armor" -> "Armor"; case "totems" -> "Totems"; case "effects" -> "Effects"; case "keys" -> "Keystrokes"; default -> id; }; }
    @Override public boolean isPauseScreen() { return false; }
}
