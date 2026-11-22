package dev.benny.tools;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.network.chat.Component;

/** /theme preset <name> | hex <slot> <hex> | chroma | reset  (the K key opens the visual editor). */
public final class ThemeCommands {
    private ThemeCommands() {}

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("theme")
                        .then(ClientCommandManager.literal("preset")
                                .then(ClientCommandManager.argument("name", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            for (String n : Theme.PRESETS.keySet()) builder.suggest(n);
                                            return builder.buildFuture();
                                        })
                                        .executes(ctx -> {
                                            String name = StringArgumentType.getString(ctx, "name");
                                            if (!Theme.hasPreset(name)) {
                                                ctx.getSource().sendFeedback(Component.literal(
                                                        "Unknown preset. Options: " + String.join(", ", Theme.PRESETS.keySet())));
                                                return 0;
                                            }
                                            Theme.applyPreset(name);
                                            Theme.save();
                                            ctx.getSource().sendFeedback(Component.literal("Applied preset " + name));
                                            return 1;
                                        })))
                        .then(ClientCommandManager.literal("hex")
                                .then(ClientCommandManager.argument("slot", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            for (Theme.Slot s : Theme.Slot.values()) builder.suggest(s.name().toLowerCase());
                                            return builder.buildFuture();
                                        })
                                        .then(ClientCommandManager.argument("color", StringArgumentType.word())
                                                .executes(ctx -> {
                                                    Theme.Slot slot = Theme.slotByName(StringArgumentType.getString(ctx, "slot"));
                                                    if (slot == null) {
                                                        ctx.getSource().sendFeedback(Component.literal("Unknown slot."));
                                                        return 0;
                                                    }
                                                    int alpha = (Theme.get(slot) >>> 24) & 0xFF;
                                                    Integer parsed = Theme.parseHex(StringArgumentType.getString(ctx, "color"), alpha);
                                                    if (parsed == null) {
                                                        ctx.getSource().sendFeedback(Component.literal("Use RRGGBB or AARRGGBB, e.g. FF8AD8."));
                                                        return 0;
                                                    }
                                                    Theme.set(slot, parsed);
                                                    Theme.save();
                                                    ctx.getSource().sendFeedback(Component.literal("Set " + slot.label + "."));
                                                    return 1;
                                                }))))
                        .then(ClientCommandManager.literal("chroma")
                                .executes(ctx -> {
                                    Theme.chroma = !Theme.chroma;
                                    Theme.save();
                                    ctx.getSource().sendFeedback(Component.literal("Rainbow accent " + (Theme.chroma ? "ON" : "OFF")));
                                    return 1;
                                }))
                        .then(ClientCommandManager.literal("reset")
                                .executes(ctx -> {
                                    Theme.reset();
                                    Theme.save();
                                    ctx.getSource().sendFeedback(Component.literal("Theme reset."));
                                    return 1;
                                }))));
    }
}
