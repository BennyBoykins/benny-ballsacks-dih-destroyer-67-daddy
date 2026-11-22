package dev.benny.tools;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Waypoint list (top right), armor + totems (bottom left), keystrokes + CPS (bottom right). */
public final class InfoHud {
    private static final String[] ARROWS = {"\u2191", "\u2197", "\u2192", "\u2198", "\u2193", "\u2199", "\u2190", "\u2196"};

    private InfoHud() {}

    private static boolean hidden(Minecraft mc) {
        return Config.streamer || mc.player == null || mc.level == null || mc.options.hideGui;
    }

    // ---------------- waypoints ----------------
    public static void renderWaypoints(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (!Config.hud || !Config.showWaypoints || hidden(mc)) return;
        LocalPlayer p = mc.player;
        String dim = Waypoints.dimKey(mc.level);
        List<Waypoints.Wp> near = Waypoints.nearest(dim, p.getX(), p.getY(), p.getZ(), 4);
        if (near.isEmpty()) return;

        List<String> lines = new ArrayList<>();
        for (Waypoints.Wp w : near) {
            double dx = w.x() - p.getX(), dz = w.z() - p.getZ();
            float bearing = (float) Math.toDegrees(Math.atan2(-dx, dz));
            float diff = Mth.wrapDegrees(bearing - p.getYRot());
            int idx = Math.floorMod(Math.round(diff / 45f), 8);
            lines.add(String.format(Locale.ROOT, "%s %s  %dm", ARROWS[idx], w.name(),
                    Math.round(w.distanceTo(p.getX(), p.getY(), p.getZ()))));
        }
        Font font = mc.font;
        int w = 0;
        for (String s : lines) w = Math.max(w, font.width(s));
        int x = g.guiWidth() - w - 6, y = 6;
        g.fill(x - 3, y - 3, x + w + 3, y + lines.size() * (font.lineHeight + 2) + 1, Theme.bg());
        for (String s : lines) {
            g.drawString(font, s, x, y, Theme.accent());
            y += font.lineHeight + 2;
        }
    }

    // ---------------- armor + totems ----------------
    public static void renderArmor(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (!Config.hud || !Config.showArmor || hidden(mc)) return;
        LocalPlayer p = mc.player;
        Font font = mc.font;

        List<String> lines = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        for (int i = 0; i < ArmorMonitor.SLOTS.length; i++) {
            int pc = ArmorMonitor.percent(p, ArmorMonitor.SLOTS[i]);
            if (pc < 0) continue;
            lines.add(ArmorMonitor.NAMES[i] + " " + pc + "%");
            colors.add(pc <= 10 ? Theme.bad() : pc <= 30 ? Theme.warn() : Theme.good());
        }
        int totems = ArmorMonitor.totems(p);
        lines.add("Totems: " + totems);
        colors.add(totems == 0 ? Theme.bad() : Theme.text());

        int lh = font.lineHeight + 2;
        int w = 0;
        for (String s : lines) w = Math.max(w, font.width(s));
        int x = 6, y = g.guiHeight() - 6 - lines.size() * lh - 20;
        g.fill(x - 3, y - 3, x + w + 3, y + lines.size() * lh + 1, Theme.bg());
        for (int i = 0; i < lines.size(); i++) {
            g.drawString(font, lines.get(i), x, y, colors.get(i));
            y += lh;
        }
    }

    // ---------------- keystrokes + CPS ----------------
    public static void renderKeystrokes(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (!Config.hud || !Config.showKeystrokes || hidden(mc)) return;
        Font font = mc.font;
        int size = 20, gap = 2;
        int width = size * 3 + gap * 2;
        int x0 = g.guiWidth() - width - 8;
        int y0 = g.guiHeight() - (size * 3 + gap * 2 + font.lineHeight + 6) - 28;

        key(g, font, x0 + size + gap, y0, size, "W", mc.options.keyUp.isDown());
        key(g, font, x0, y0 + size + gap, size, "A", mc.options.keyLeft.isDown());
        key(g, font, x0 + size + gap, y0 + size + gap, size, "S", mc.options.keyDown.isDown());
        key(g, font, x0 + (size + gap) * 2, y0 + size + gap, size, "D", mc.options.keyRight.isDown());

        int jy = y0 + (size + gap) * 2;
        boolean jump = mc.options.keyJump.isDown();
        g.fill(x0, jy, x0 + width, jy + 12, jump ? Theme.keyActive() : Theme.bg());
        g.drawCenteredString(font, "SPACE", x0 + width / 2, jy + 2, jump ? Theme.contrast(Theme.keyActive()) : Theme.text());

        String cps = ClickTracker.left() + " | " + ClickTracker.right() + " CPS";
        g.drawCenteredString(font, cps, x0 + width / 2, jy + 16, Theme.text());
    }

    private static void key(GuiGraphics g, Font font, int x, int y, int size, String label, boolean down) {
        g.fill(x, y, x + size, y + size, down ? Theme.keyActive() : Theme.bg());
        g.drawCenteredString(font, label, x + size / 2, y + (size - font.lineHeight) / 2 + 1,
                down ? Theme.contrast(Theme.keyActive()) : Theme.text());
    }
}
