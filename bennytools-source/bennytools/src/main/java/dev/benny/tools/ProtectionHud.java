package dev.benny.tools;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public final class ProtectionHud {
    private static final int MAX_LINES = 10;
    private static final long ALERT_MS = 4000, FADE_MS = 700;
    private static final int MAX_ALERTS = 5;

    private record Alert(String text, int color, long expires) {}

    private static final List<Alert> ALERTS = new ArrayList<>();

    private ProtectionHud() {}

    public static void alert(String text, int color) {
        ALERTS.add(new Alert(text, color, System.currentTimeMillis() + ALERT_MS));
        while (ALERTS.size() > MAX_ALERTS) ALERTS.remove(0);
    }

    private static boolean previewing(Minecraft mc) {
        return mc.screen instanceof ProtectionScreen;
    }

    private static boolean hidden(Minecraft mc) {
        return Config.streamer || mc.player == null || mc.level == null || mc.options.hideGui;
    }

    // ---------------- the two panels ----------------
    public static void renderPanels(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        boolean preview = previewing(mc);
        if (!preview && (!Config.hud || hidden(mc))) return;

        if (ProtectionConfig.staffEnabled) {
            List<String> lines = new ArrayList<>();
            List<Integer> colors = new ArrayList<>();
            List<ProtectionTracker.Seen> src = ProtectionTracker.nearby;
            if (src.isEmpty() && preview) {
                src = List.of(new ProtectionTracker.Seen("ExamplePlayer", 8f),
                        new ProtectionTracker.Seen("AnotherOne", 31f));
            }
            for (int i = 0; i < src.size() && i < MAX_LINES; i++) {
                ProtectionTracker.Seen s = src.get(i);
                lines.add(ProtectionConfig.staffShowDistance ? s.name() + "  " + Math.round(s.distance()) + "m" : s.name());
                colors.add(s.distance() < 10 ? Theme.bad() : s.distance() < 25 ? Theme.warn() : Theme.text());
            }
            if (src.size() > MAX_LINES) { lines.add("+" + (src.size() - MAX_LINES) + " more"); colors.add(Theme.text()); }
            if (lines.isEmpty()) { lines.add("Nobody nearby"); colors.add(Theme.good()); }
            panel(g, mc, "Staff Protection (" + ProtectionConfig.staffRadius + "m)", Theme.accent(),
                    lines, colors, ProtectionConfig.staffAnchor, ProtectionConfig.staffX, ProtectionConfig.staffY);
        }

        if (ProtectionConfig.specEnabled) {
            List<String> lines = new ArrayList<>();
            List<Integer> colors = new ArrayList<>();
            List<ProtectionTracker.Seen> src = ProtectionTracker.spectators;
            if (src.isEmpty() && preview) src = List.of(new ProtectionTracker.Seen("SomeSpectator", 14f));
            for (int i = 0; i < src.size() && i < MAX_LINES; i++) {
                ProtectionTracker.Seen s = src.get(i);
                lines.add(s.distance() >= 0 ? s.name() + "  " + Math.round(s.distance()) + "m" : s.name());
                colors.add(Theme.warn());
            }
            if (src.size() > MAX_LINES) { lines.add("+" + (src.size() - MAX_LINES) + " more"); colors.add(Theme.text()); }
            if (lines.isEmpty()) { lines.add("No spectators"); colors.add(Theme.good()); }
            panel(g, mc, "Spectator Protection (" + src.size() + ")", Theme.accent(),
                    lines, colors, ProtectionConfig.specAnchor, ProtectionConfig.specX, ProtectionConfig.specY);
        }
    }

    private static void panel(GuiGraphics g, Minecraft mc, String title, int titleColor,
                              List<String> lines, List<Integer> colors,
                              ProtectionConfig.Anchor anchor, int ox, int oy) {
        Font f = mc.font;
        int lh = f.lineHeight + 2;
        int w = f.width(title);
        for (String s : lines) w = Math.max(w, f.width(s));
        int h = (lines.size() + 1) * lh;
        int x = Math.max(3, Math.min(anchor.x(g.guiWidth(), w, ox), g.guiWidth() - w - 3));
        int y = Math.max(3, Math.min(anchor.y(g.guiHeight(), h, oy), g.guiHeight() - h - 3));

        g.fill(x - 3, y - 3, x + w + 3, y + h + 1, Theme.bg());
        g.drawString(f, title, x, y, titleColor);
        y += lh;
        for (int i = 0; i < lines.size(); i++) {
            g.drawString(f, lines.get(i), x, y, colors.get(i));
            y += lh;
        }
    }

    // ---------------- alert banners (top centre) ----------------
    public static void renderAlerts(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (hidden(mc)) return;
        long now = System.currentTimeMillis();
        ALERTS.removeIf(a -> a.expires() <= now);
        Font f = mc.font;
        int y = 28;
        for (Alert a : ALERTS) {
            long left = a.expires() - now;
            float alpha = left < FADE_MS ? left / (float) FADE_MS : 1f;
            int textAlpha = Mth.clamp((int) (alpha * 255), 5, 255);
            int bgAlpha = Mth.clamp((int) (alpha * 170), 0, 255);
            int tw = f.width(a.text());
            int x = (g.guiWidth() - tw) / 2;
            g.fill(x - 6, y - 3, x + tw + 6, y + f.lineHeight + 3, bgAlpha << 24);
            g.drawString(f, a.text(), x, y, (textAlpha << 24) | (a.color() & 0xFFFFFF));
            y += f.lineHeight + 8;
        }
    }
}
