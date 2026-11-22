package dev.benny.tools;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Small top-left info panel: XYZ, facing, biome, dimension conversion, FPS, ping. */
public final class CoordsHud {
    private CoordsHud() {}

    public static void render(GuiGraphics g, DeltaTracker delta) {
        if (!Config.hud || Config.streamer) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || level == null || mc.options.hideGui) return;

        List<String> lines = new ArrayList<>();
        lines.add(String.format(Locale.ROOT, "XYZ: %.1f / %.1f / %.1f", player.getX(), player.getY(), player.getZ()));

        if (Config.showFacing) {
            Direction d = player.getDirection();
            lines.add("Facing: " + facing(d));
        }
        if (Config.showBiome) {
            lines.add("Biome: " + biome(level, player.blockPosition()));
        }
        if (Config.showConversion) {
            if (level.dimension() == Level.OVERWORLD) {
                lines.add(String.format(Locale.ROOT, "Nether: %d / %d",
                        Math.floorDiv(player.getBlockX(), 8), Math.floorDiv(player.getBlockZ(), 8)));
            } else if (level.dimension() == Level.NETHER) {
                lines.add(String.format(Locale.ROOT, "Overworld: %d / %d",
                        player.getBlockX() * 8, player.getBlockZ() * 8));
            }
        }
        StringBuilder perf = new StringBuilder();
        if (Config.showFps) perf.append(mc.getFps()).append(" FPS");
        if (Config.showPing && mc.getConnection() != null) {
            PlayerInfo info = mc.getConnection().getPlayerInfo(player.getUUID());
            if (info != null) {
                if (perf.length() > 0) perf.append("  |  ");
                perf.append(info.getLatency()).append(" ms");
            }
        }
        if (perf.length() > 0) lines.add(perf.toString());

        Font font = mc.font;
        int x = 6, y = 6, w = 0;
        for (String s : lines) w = Math.max(w, font.width(s));
        int h = lines.size() * (font.lineHeight + 2);
        g.fill(x - 3, y - 3, x + w + 3, y + h + 1, Theme.bg());
        int ty = y;
        for (String s : lines) {
            g.drawString(font, s, x, ty, Theme.text());
            ty += font.lineHeight + 2;
        }
    }

    private static String facing(Direction d) {
        return switch (d) {
            case NORTH -> "North (-Z)";
            case SOUTH -> "South (+Z)";
            case EAST -> "East (+X)";
            case WEST -> "West (-X)";
            default -> d.toString();
        };
    }

    /** Parses the biome id from the registry key's string form to stay independent of method renames. */
    private static String biome(ClientLevel level, BlockPos pos) {
        return level.getBiome(pos).unwrapKey().map(key -> {
            String s = key.toString();               // ResourceKey[minecraft:worldgen/biome / minecraft:plains]
            int slash = s.lastIndexOf('/');
            String id = (slash >= 0 ? s.substring(slash + 1) : s).replace("]", "").trim();
            int colon = id.indexOf(':');
            if (colon >= 0) id = id.substring(colon + 1);
            String[] parts = id.split("_");
            StringBuilder sb = new StringBuilder();
            for (String part : parts) {
                if (part.isEmpty()) continue;
                if (sb.length() > 0) sb.append(' ');
                sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
            return sb.toString();
        }).orElse("Unknown");
    }
}
