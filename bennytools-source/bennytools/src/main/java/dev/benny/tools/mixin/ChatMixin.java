package dev.benny.tools.mixin;

import dev.benny.tools.Redactor;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatComponent.class)
public abstract class ChatMixin {
    @ModifyVariable(method = "addMessage", at = @At("HEAD"), argsOnly = true)
    private Component bennytools$redact(Component message) {
        return Redactor.apply(message);
    }
}
