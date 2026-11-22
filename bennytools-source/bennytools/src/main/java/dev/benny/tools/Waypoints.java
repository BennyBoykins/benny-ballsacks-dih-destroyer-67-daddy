package dev.benny.tools;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Named waypoints stored per dimension in config/bennytools_waypoints.txt (name|dim|x|y|z). */
public final class Waypoints {
    public record Wp(String name, String dim, double x, double y, double z) {
        public double distanceTo(double px, double py, double pz) {
            double dx = x - px, dy = y - py, dz = z - pz;
            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        }
    }

    private static final List<Wp> LIST = new ArrayList<>();

    private Waypoints() {}

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("bennytools_waypoints.txt");
    }

    public static String dimKey(ClientLevel level) {
        if (level.dimension() == Level.OVERWORLD) return "overworld";
        if (level.dimension() == Level.NETHER) return "nether";
        if (level.dimension() == Level.END) return "end";
        return "other";
    }

    public static void load() {
        LIST.clear();
        Path p = path();
        if (!Files.exists(p)) return;
        try {
            for (String line : Files.readAllLines(p)) {
                String[] t = line.split("\\|");
                if (t.length != 5) continue;
                try {
                    LIST.add(new Wp(t[0], t[1], Double.parseDouble(t[2]),
                            Double.parseDouble(t[3]), Double.parseDouble(t[4])));
                } catch (NumberFormatException ignored) {
                }
            }
        } catch (IOException ignored) {
        }
    }

    private static void save() {
        List<String> out = new ArrayList<>();
        for (Wp w : LIST) {
            out.add(String.format(Locale.ROOT, "%s|%s|%.2f|%.2f|%.2f", w.name(), w.dim(), w.x(), w.y(), w.z()));
        }
        try {
            Files.write(path(), out);
        } catch (IOException ignored) {
        }
    }

    public static void add(String name, String dim, double x, double y, double z) {
        remove(name, dim);
        LIST.add(new Wp(name, dim, x, y, z));
        save();
    }

    public static boolean remove(String name, String dim) {
        boolean removed = LIST.removeIf(w -> w.name().equalsIgnoreCase(name) && w.dim().equals(dim));
        if (removed) save();
        return removed;
    }

    public static List<Wp> inDimension(String dim) {
        List<Wp> out = new ArrayList<>();
        for (Wp w : LIST) if (w.dim().equals(dim)) out.add(w);
        return out;
    }

    public static List<Wp> nearest(String dim, double px, double py, double pz, int max) {
        List<Wp> out = inDimension(dim);
        out.sort(Comparator.comparingDouble(w -> w.distanceTo(px, py, pz)));
        return out.size() > max ? new ArrayList<>(out.subList(0, max)) : out;
    }
}
