package dev.benny.tools;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Locale;

public class BennyTools implements ClientModInitializer {
    public static final String MOD_ID = "bennytools";

    public static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));

    public static final KeyMapping ZOOM = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.bennytools.zoom", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, CATEGORY));
    public static final KeyMapping TOGGLE_HUD = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.bennytools.toggle_hud", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H, CATEGORY));
    public static final KeyMapping TOGGLE_STREAMER = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.bennytools.toggle_streamer", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F9, CATEGORY));
    public static final KeyMapping QUICK_WAYPOINT = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.bennytools.quick_waypoint", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, CATEGORY));
    public static final KeyMapping OPEN_THEME = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.bennytools.open_theme", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, CATEGORY));
    public static final KeyMapping OPEN_PROTECTION = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.bennytools.open_protection", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, CATEGORY));

    @Override
    public void onInitializeClient() {
        Config.load();
        Waypoints.load();
        Theme.load();
        ProtectionConfig.load();

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "coords"), CoordsHud::render);
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "waypoints"), InfoHud::renderWaypoints);
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "armor"), InfoHud::renderArmor);
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "keystrokes"), InfoHud::renderKeystrokes);
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "protection"), ProtectionHud::renderPanels);
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "protection_alerts"), ProtectionHud::renderAlerts);

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            ZoomController.tick(mc);
            ClickTracker.tick(mc);
            ArmorMonitor.tick(mc);
            ProtectionTracker.tick(mc);

            while (TOGGLE_HUD.consumeClick()) {
                Config.hud = !Config.hud;
                Config.save();
                notify(mc, "HUD: " + (Config.hud ? "ON" : "OFF"));
            }
            while (TOGGLE_STREAMER.consumeClick()) {
                Config.streamer = !Config.streamer;
                Config.save();
                notify(mc, "Streamer mode: " + (Config.streamer ? "ON" : "OFF"));
            }
            while (OPEN_PROTECTION.consumeClick()) {
                if (mc.screen == null) mc.setScreen(new ProtectionScreen(null));
            }
            while (OPEN_THEME.consumeClick()) {
                if (mc.screen == null) mc.setScreen(new ThemeScreen(null));
            }
            while (QUICK_WAYPOINT.consumeClick()) {
                if (mc.player != null && mc.level != null) {
                    String dim = Waypoints.dimKey(mc.level);
                    String name = "WP" + (Waypoints.inDimension(dim).size() + 1);
                    LocalPlayer p = mc.player;
                    Waypoints.add(name, dim, p.getX(), p.getY(), p.getZ());
                    notify(mc, "Saved waypoint " + name);
                }
            }
        });

        registerCommands();
        ThemeCommands.register();
        ProtectionCommands.register();
    }

    private static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("wp")
                    .then(ClientCommandManager.literal("add")
                            .then(ClientCommandManager.argument("name", StringArgumentType.word())
                                    .executes(ctx -> {
                                        Minecraft mc = Minecraft.getInstance();
                                        if (mc.player == null || mc.level == null) return 0;
                                        String name = StringArgumentType.getString(ctx, "name");
                                        String dim = Waypoints.dimKey(mc.level);
                                        Waypoints.add(name, dim, mc.player.getX(), mc.player.getY(), mc.player.getZ());
                                        ctx.getSource().sendFeedback(Component.literal("Saved waypoint '" + name + "' in " + dim));
                                        return 1;
                                    })))
                    .then(ClientCommandManager.literal("remove")
                            .then(ClientCommandManager.argument("name", StringArgumentType.word())
                                    .executes(ctx -> {
                                        Minecraft mc = Minecraft.getInstance();
                                        if (mc.level == null) return 0;
                                        String name = StringArgumentType.getString(ctx, "name");
                                        boolean ok = Waypoints.remove(name, Waypoints.dimKey(mc.level));
                                        ctx.getSource().sendFeedback(Component.literal(
                                                ok ? "Removed '" + name + "'" : "No waypoint '" + name + "' in this dimension"));
                                        return ok ? 1 : 0;
                                    })))
                    .then(ClientCommandManager.literal("list")
                            .executes(ctx -> {
                                Minecraft mc = Minecraft.getInstance();
                                if (mc.level == null) return 0;
                                List<Waypoints.Wp> list = Waypoints.inDimension(Waypoints.dimKey(mc.level));
                                if (list.isEmpty()) {
                                    ctx.getSource().sendFeedback(Component.literal("No waypoints in this dimension."));
                                    return 1;
                                }
                                for (Waypoints.Wp w : list) {
                                    ctx.getSource().sendFeedback(Component.literal(String.format(Locale.ROOT,
                                            "%s: %.0f, %.0f, %.0f", w.name(), w.x(), w.y(), w.z())));
                                }
                                return 1;
                            })));

            dispatcher.register(ClientCommandManager.literal("redact")
                    .then(ClientCommandManager.literal("add")
                            .then(ClientCommandManager.argument("word", StringArgumentType.word())
                                    .executes(ctx -> {
                                        String word = StringArgumentType.getString(ctx, "word");
                                        Config.redactWords = Config.redactWords.isEmpty() ? word : Config.redactWords + "," + word;
                                        Config.save();
                                        ctx.getSource().sendFeedback(Component.literal("Redacting '" + word + "' while streamer mode is on."));
                                        return 1;
                                    })))
                    .then(ClientCommandManager.literal("clear")
                            .executes(ctx -> {
                                Config.redactWords = "";
                                Config.save();
                                ctx.getSource().sendFeedback(Component.literal("Cleared redaction list."));
                                return 1;
                            })));
        });
    }

    private static void notify(Minecraft mc, String msg) {
        if (mc.player != null) mc.player.displayClientMessage(Component.literal(msg), true);
    }
}
