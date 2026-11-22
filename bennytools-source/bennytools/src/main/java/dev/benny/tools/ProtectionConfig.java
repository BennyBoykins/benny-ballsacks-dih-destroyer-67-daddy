package dev.benny.tools;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Settings for the Staff Protection and Spectator Protection modules. */
public final class ProtectionConfig {
    /** Where a panel is pinned on screen; the X/Y offsets move it away from that edge. */
    public enum Anchor {
        TOP_LEFT("Top left"), TOP_CENTER("Top center"), TOP_RIGHT("Top right"),
        BOTTOM_LEFT("Bottom left"), BOTTOM_CENTER("Bottom center"), BOTTOM_RIGHT("Bottom right");

        public final String label;

        Anchor(String label) { this.label = label; }

        public Anchor next() {
            Anchor[] v = values();
            return v[(ordinal() + 1) % v.length];
        }

        public int x(int screenW, int w, int off) {
            return switch (this) {
                case TOP_LEFT, BOTTOM_LEFT -> off;
                case TOP_CENTER, BOTTOM_CENTER -> (screenW - w) / 2 + off;
                case TOP_RIGHT, BOTTOM_RIGHT -> screenW - w - off;
            };
        }

        public int y(int screenH, int h, int off) {
            return switch (this) {
                case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> off;
                case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> screenH - h - off;
            };
        }

        public static Anchor parse(String s, Anchor fallback) {
            try { return valueOf(s.trim().toUpperCase()); } catch (RuntimeException e) { return fallback; }
        }
    }

    // ---- Staff Protection: players near you ----
    public static boolean staffEnabled = true;
    public static boolean staffAlert = true;
    public static boolean staffShowDistance = true;
    public static int staffRadius = 50;
    public static Anchor staffAnchor = Anchor.TOP_LEFT;
    public static int staffX = 6, staffY = 90;

    // ---- Spectator Protection: players in spectator mode ----
    public static boolean specEnabled = true;
    public static boolean specAlert = true;
    public static Anchor specAnchor = Anchor.TOP_RIGHT;
    public static int specX = 6, specY = 70;

    public static final int MIN_RADIUS = 5, MAX_RADIUS = 200;

    private ProtectionConfig() {}

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("bennytools_protection.properties");
    }

    public static void load() {
        Path p = path();
        if (!Files.exists(p)) { save(); return; }
        Properties pr = new Properties();
        try (Reader r = Files.newBufferedReader(p)) {
            pr.load(r);
            staffEnabled = bool(pr, "staffEnabled", staffEnabled);
            staffAlert = bool(pr, "staffAlert", staffAlert);
            staffShowDistance = bool(pr, "staffShowDistance", staffShowDistance);
            staffRadius = clamp(num(pr, "staffRadius", staffRadius), MIN_RADIUS, MAX_RADIUS);
            staffAnchor = Anchor.parse(pr.getProperty("staffAnchor", staffAnchor.name()), staffAnchor);
            staffX = clamp(num(pr, "staffX", staffX), 0, 600);
            staffY = clamp(num(pr, "staffY", staffY), 0, 600);
            specEnabled = bool(pr, "specEnabled", specEnabled);
            specAlert = bool(pr, "specAlert", specAlert);
            specAnchor = Anchor.parse(pr.getProperty("specAnchor", specAnchor.name()), specAnchor);
            specX = clamp(num(pr, "specX", specX), 0, 600);
            specY = clamp(num(pr, "specY", specY), 0, 600);
        } catch (IOException ignored) {
        }
    }

    public static void save() {
        Properties pr = new Properties();
        pr.setProperty("staffEnabled", String.valueOf(staffEnabled));
        pr.setProperty("staffAlert", String.valueOf(staffAlert));
        pr.setProperty("staffShowDistance", String.valueOf(staffShowDistance));
        pr.setProperty("staffRadius", String.valueOf(staffRadius));
        pr.setProperty("staffAnchor", staffAnchor.name());
        pr.setProperty("staffX", String.valueOf(staffX));
        pr.setProperty("staffY", String.valueOf(staffY));
        pr.setProperty("specEnabled", String.valueOf(specEnabled));
        pr.setProperty("specAlert", String.valueOf(specAlert));
        pr.setProperty("specAnchor", specAnchor.name());
        pr.setProperty("specX", String.valueOf(specX));
        pr.setProperty("specY", String.valueOf(specY));
        try (Writer w = Files.newBufferedWriter(path())) {
            pr.store(w, "Benny Tools - Staff / Spectator Protection");
        } catch (IOException ignored) {
        }
    }

    public static int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }

    private static boolean bool(Properties p, String k, boolean d) {
        return Boolean.parseBoolean(p.getProperty(k, String.valueOf(d)));
    }

    private static int num(Properties p, String k, int d) {
        try { return Integer.parseInt(p.getProperty(k, String.valueOf(d)).trim()); }
        catch (NumberFormatException e) { return d; }
    }
}
