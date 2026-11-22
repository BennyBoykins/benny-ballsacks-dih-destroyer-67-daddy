package dev.benny.tools;

import net.fabricmc.loader.api.FabricLoader;

import java.awt.Color;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/** Colour theme shared by every HUD panel. Colours are ARGB ints. */
public final class Theme {
    public enum Slot {
        PANEL("Panel background", true),
        TEXT("Text", false),
        ACCENT("Waypoints / accent", false),
        GOOD("Good (healthy)", false),
        WARN("Warning", false),
        BAD("Bad (critical)", false),
        KEY("Key pressed", true);

        public final String label;
        public final boolean hasAlpha;

        Slot(String label, boolean hasAlpha) {
            this.label = label;
            this.hasAlpha = hasAlpha;
        }
    }

    // order: PANEL, TEXT, ACCENT, GOOD, WARN, BAD, KEY
    public static final Map<String, int[]> PRESETS = new LinkedHashMap<>();
    static {
        PRESETS.put("Default",  new int[]{0x80000000, 0xFFFFFFFF, 0xFFFFFF55, 0xFF55FF55, 0xFFFFAA00, 0xFFFF5555, 0xA0FFFFFF});
        PRESETS.put("Pink",     new int[]{0x90401030, 0xFFFFE6F2, 0xFFFF8AD8, 0xFF9AF0C8, 0xFFFFC46B, 0xFFFF6B8B, 0xB0FF9ED8});
        PRESETS.put("Mint",     new int[]{0x90102820, 0xFFE8FFF5, 0xFF7CFFCB, 0xFF7CFFCB, 0xFFFFE08A, 0xFFFF8A8A, 0xB07CFFCB});
        PRESETS.put("Midnight", new int[]{0xA0080818, 0xFFD6DCFF, 0xFF7C8CFF, 0xFF63E6A0, 0xFFFFC857, 0xFFFF6B6B, 0xB07C8CFF});
        PRESETS.put("Cyber",    new int[]{0xA0000000, 0xFF00FFEA, 0xFFFF2BD6, 0xFF39FF14, 0xFFFFEA00, 0xFFFF3131, 0xB000FFEA});
        PRESETS.put("Light",    new int[]{0xB0F5F5F5, 0xFF202020, 0xFF3355FF, 0xFF1E9E4A, 0xFFB86E00, 0xFFC62828, 0xB0303030});
    }

    private static final int[] colors = PRESETS.get("Default").clone();
    /** Animated rainbow for the accent colour. */
    public static boolean chroma = false;

    private Theme() {}

    // ---- raw access (used by the editor) ----
    public static int get(Slot s) { return colors[s.ordinal()]; }

    public static void set(Slot s, int argb) { colors[s.ordinal()] = argb; }

    public static void applyPreset(String name) {
        for (Map.Entry<String, int[]> e : PRESETS.entrySet()) {
            if (e.getKey().equalsIgnoreCase(name)) {
                System.arraycopy(e.getValue(), 0, colors, 0, colors.length);
                return;
            }
        }
    }

    public static boolean hasPreset(String name) {
        for (String k : PRESETS.keySet()) if (k.equalsIgnoreCase(name)) return true;
        return false;
    }

    public static void reset() {
        applyPreset("Default");
        chroma = false;
    }

    public static Slot slotByName(String name) {
        for (Slot s : Slot.values()) if (s.name().equalsIgnoreCase(name)) return s;
        return null;
    }

    // ---- what the HUD panels actually draw with ----
    public static int bg() { return colors[Slot.PANEL.ordinal()]; }

    public static int text() { return opaque(Slot.TEXT); }

    public static int good() { return opaque(Slot.GOOD); }

    public static int warn() { return opaque(Slot.WARN); }

    public static int bad() { return opaque(Slot.BAD); }

    public static int keyActive() { return colors[Slot.KEY.ordinal()]; }

    public static int accent() {
        if (chroma) {
            float hue = ((System.currentTimeMillis() / 20L) % 360L) / 360f;
            return Color.HSBtoRGB(hue, 0.55f, 1f) | 0xFF000000;
        }
        return opaque(Slot.ACCENT);
    }

    /** Black or white text, whichever reads better on top of the given (translucent) colour. */
    public static int contrast(int argb) {
        int a = (argb >>> 24) & 0xFF, r = (argb >> 16) & 0xFF, g = (argb >> 8) & 0xFF, b = argb & 0xFF;
        double lum = (0.299 * r + 0.587 * g + 0.114 * b) * (a / 255.0);
        return lum > 110 ? 0xFF000000 : 0xFFFFFFFF;
    }

    private static int opaque(Slot s) { return colors[s.ordinal()] | 0xFF000000; }

    // ---- persistence ----
    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("bennytools_theme.properties");
    }

    public static void load() {
        Path p = path();
        if (!Files.exists(p)) return;
        Properties props = new Properties();
        try (Reader r = Files.newBufferedReader(p)) {
            props.load(r);
            for (Slot s : Slot.values()) {
                String v = props.getProperty(s.name());
                if (v != null) {
                    try { colors[s.ordinal()] = (int) Long.parseLong(v.trim(), 16); }
                    catch (NumberFormatException ignored) { }
                }
            }
            chroma = Boolean.parseBoolean(props.getProperty("chroma", "false"));
        } catch (IOException ignored) {
        }
    }

    public static void save() {
        Properties props = new Properties();
        for (Slot s : Slot.values()) props.setProperty(s.name(), String.format("%08X", colors[s.ordinal()]));
        props.setProperty("chroma", String.valueOf(chroma));
        try (Writer w = Files.newBufferedWriter(path())) {
            props.store(w, "Benny Tools theme (ARGB hex)");
        } catch (IOException ignored) {
        }
    }

    /** Parses RRGGBB or AARRGGBB (optionally prefixed with #). Returns null on bad input. */
    public static Integer parseHex(String input, int existingAlpha) {
        String h = input.startsWith("#") ? input.substring(1) : input;
        try {
            if (h.length() == 6) return (existingAlpha << 24) | Integer.parseInt(h, 16);
            if (h.length() == 8) return (int) Long.parseLong(h, 16);
        } catch (NumberFormatException ignored) {
        }
        return null;
    }
}
