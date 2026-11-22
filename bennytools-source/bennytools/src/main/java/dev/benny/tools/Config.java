package dev.benny.tools;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Tiny properties-file config. Edit config/bennytools.properties or use the keybinds. */
public final class Config {
    public static boolean hud = true;
    public static boolean showFacing = true;
    public static boolean showBiome = true;
    public static boolean showFps = true;
    public static boolean showPing = true;
    public static boolean showConversion = true;
    public static boolean showWaypoints = true;
    public static boolean showArmor = true;
    public static boolean showKeystrokes = true;
    public static boolean armorWarnings = true;
    public static boolean streamer = false;
    public static boolean redactOwnName = true;
    /** Comma-separated words hidden from chat while streamer mode is on. */
    public static String redactWords = "";
    public static double zoomLevel = 4.0;

    private Config() {}

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("bennytools.properties");
    }

    public static void load() {
        Path p = path();
        if (!Files.exists(p)) { save(); return; }
        Properties props = new Properties();
        try (Reader r = Files.newBufferedReader(p)) {
            props.load(r);
            hud = bool(props, "hud", hud);
            showFacing = bool(props, "showFacing", showFacing);
            showBiome = bool(props, "showBiome", showBiome);
            showFps = bool(props, "showFps", showFps);
            showPing = bool(props, "showPing", showPing);
            showConversion = bool(props, "showConversion", showConversion);
            showWaypoints = bool(props, "showWaypoints", showWaypoints);
            showArmor = bool(props, "showArmor", showArmor);
            showKeystrokes = bool(props, "showKeystrokes", showKeystrokes);
            armorWarnings = bool(props, "armorWarnings", armorWarnings);
            streamer = bool(props, "streamer", streamer);
            redactOwnName = bool(props, "redactOwnName", redactOwnName);
            redactWords = props.getProperty("redactWords", redactWords);
            zoomLevel = Double.parseDouble(props.getProperty("zoomLevel", String.valueOf(zoomLevel)));
        } catch (IOException | NumberFormatException ignored) {
        }
    }

    public static void save() {
        Properties props = new Properties();
        props.setProperty("hud", String.valueOf(hud));
        props.setProperty("showFacing", String.valueOf(showFacing));
        props.setProperty("showBiome", String.valueOf(showBiome));
        props.setProperty("showFps", String.valueOf(showFps));
        props.setProperty("showPing", String.valueOf(showPing));
        props.setProperty("showConversion", String.valueOf(showConversion));
        props.setProperty("showWaypoints", String.valueOf(showWaypoints));
        props.setProperty("showArmor", String.valueOf(showArmor));
        props.setProperty("showKeystrokes", String.valueOf(showKeystrokes));
        props.setProperty("armorWarnings", String.valueOf(armorWarnings));
        props.setProperty("streamer", String.valueOf(streamer));
        props.setProperty("redactOwnName", String.valueOf(redactOwnName));
        props.setProperty("redactWords", redactWords);
        props.setProperty("zoomLevel", String.valueOf(zoomLevel));
        try (Writer w = Files.newBufferedWriter(path())) {
            props.store(w, "Benny Tools");
        } catch (IOException ignored) {
        }
    }

    private static boolean bool(Properties p, String k, boolean d) {
        return Boolean.parseBoolean(p.getProperty(k, String.valueOf(d)));
    }
}
