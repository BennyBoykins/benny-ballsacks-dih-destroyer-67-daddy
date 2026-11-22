package dev.benny.tools;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.level.GameType;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Computes who is nearby / in spectator mode once per tick and raises on-screen alerts for new entries. */
public final class ProtectionTracker {
    /** distance is -1 when the player's entity isn't loaded on your client. */
    public record Seen(String name, float distance) {}

    public static List<Seen> nearby = List.of();
    public static List<Seen> spectators = List.of();

    private static final Set<String> prevNearby = new HashSet<>();
    private static final Set<String> prevSpec = new HashSet<>();
    private static Object lastLevel;
    private static int warmup;
    private static Method nameMethod;

    private ProtectionTracker() {}

    public static void tick(Minecraft mc) {
        if (mc.player == null || mc.level == null) {
            nearby = List.of();
            spectators = List.of();
            prevNearby.clear();
            prevSpec.clear();
            lastLevel = null;
            return;
        }
        if (mc.level != lastLevel) {
            // new world/dimension: don't spam alerts for everyone already there
            lastLevel = mc.level;
            prevNearby.clear();
            prevSpec.clear();
            warmup = 40;
        }
        boolean quiet = warmup > 0;
        if (warmup > 0) warmup--;

        String self = mc.player.getName().getString();

        // every player entity your client currently knows about, with distance
        Map<String, Float> known = new HashMap<>();
        for (AbstractClientPlayer p : mc.level.players()) {
            if (p == mc.player) continue;
            known.put(p.getName().getString(), mc.player.distanceTo(p));
        }

        // ---- Staff Protection ----
        List<Seen> near = new ArrayList<>();
        if (ProtectionConfig.staffEnabled) {
            for (Map.Entry<String, Float> e : known.entrySet()) {
                if (e.getValue() <= ProtectionConfig.staffRadius) near.add(new Seen(e.getKey(), e.getValue()));
            }
            near.sort(Comparator.comparingDouble(Seen::distance));
        }
        Set<String> nearNames = new HashSet<>();
        for (Seen s : near) nearNames.add(s.name());
        if (!quiet && ProtectionConfig.staffAlert) {
            for (Seen s : near) {
                if (!prevNearby.contains(s.name())) {
                    ProtectionHud.alert("! " + s.name() + " entered " + ProtectionConfig.staffRadius
                            + "m (" + Math.round(s.distance()) + "m)", Theme.bad());
                }
            }
        }
        prevNearby.clear();
        prevNearby.addAll(nearNames);
        nearby = near;

        // ---- Spectator Protection ----
        List<Seen> specs = new ArrayList<>();
        if (ProtectionConfig.specEnabled && mc.getConnection() != null) {
            for (PlayerInfo info : mc.getConnection().getOnlinePlayers()) {
                if (info.getGameMode() != GameType.SPECTATOR) continue;
                String name = nameOf(info);
                if (name == null || name.equals(self)) continue;
                specs.add(new Seen(name, known.getOrDefault(name, -1f)));
            }
            specs.sort(Comparator.comparing(Seen::name, String.CASE_INSENSITIVE_ORDER));
        }
        Set<String> specNames = new HashSet<>();
        for (Seen s : specs) specNames.add(s.name());
        if (!quiet && ProtectionConfig.specAlert) {
            for (Seen s : specs) {
                if (!prevSpec.contains(s.name())) {
                    ProtectionHud.alert("! " + s.name() + " is in spectator mode", Theme.warn());
                }
            }
        }
        prevSpec.clear();
        prevSpec.addAll(specNames);
        spectators = specs;
    }

    /**
     * Reads the profile name by reflection: the authlib GameProfile accessor was renamed
     * (getName() -> name()) between versions, and this works with either.
     */
    private static String nameOf(PlayerInfo info) {
        try {
            Object profile = info.getProfile();
            if (nameMethod == null || !nameMethod.getDeclaringClass().isInstance(profile)) {
                try { nameMethod = profile.getClass().getMethod("name"); }
                catch (NoSuchMethodException e) { nameMethod = profile.getClass().getMethod("getName"); }
            }
            Object r = nameMethod.invoke(profile);
            return r == null ? null : r.toString();
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }
}
