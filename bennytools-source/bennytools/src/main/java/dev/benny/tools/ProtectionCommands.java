package dev.benny.tools;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.network.chat.Component;

/** /protect staff on|off, /protect spectator on|off, /protect radius <5-200>  (G opens the settings screen). */
public final class ProtectionCommands {
    private ProtectionCommands() {}

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("protect")
                        .then(ClientCommandManager.literal("staff")
                                .then(ClientCommandManager.literal("on").executes(ctx -> {
                                    ProtectionConfig.staffEnabled = true;
                                    ProtectionConfig.save();
                                    ctx.getSource().sendFeedback(Component.literal("Staff Protection: ON"));
                                    return 1;
                                }))
                                .then(ClientCommandManager.literal("off").executes(ctx -> {
                                    ProtectionConfig.staffEnabled = false;
                                    ProtectionConfig.save();
                                    ctx.getSource().sendFeedback(Component.literal("Staff Protection: OFF"));
                                    return 1;
                                })))
                        .then(ClientCommandManager.literal("spectator")
                                .then(ClientCommandManager.literal("on").executes(ctx -> {
                                    ProtectionConfig.specEnabled = true;
                                    ProtectionConfig.save();
                                    ctx.getSource().sendFeedback(Component.literal("Spectator Protection: ON"));
                                    return 1;
                                }))
                                .then(ClientCommandManager.literal("off").executes(ctx -> {
                                    ProtectionConfig.specEnabled = false;
                                    ProtectionConfig.save();
                                    ctx.getSource().sendFeedback(Component.literal("Spectator Protection: OFF"));
                                    return 1;
                                })))
                        .then(ClientCommandManager.literal("radius")
                                .then(ClientCommandManager.argument("blocks",
                                                IntegerArgumentType.integer(ProtectionConfig.MIN_RADIUS, ProtectionConfig.MAX_RADIUS))
                                        .executes(ctx -> {
                                            ProtectionConfig.staffRadius = IntegerArgumentType.getInteger(ctx, "blocks");
                                            ProtectionConfig.save();
                                            ctx.getSource().sendFeedback(Component.literal(
                                                    "Staff Protection radius: " + ProtectionConfig.staffRadius + " blocks"));
                                            return 1;
                                        })))));
    }
}
