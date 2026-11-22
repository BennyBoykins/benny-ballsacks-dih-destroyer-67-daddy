package dev.benny.tools.mixin;

import dev.benny.tools.ZoomController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    /** While the zoom key is held, the scroll wheel changes zoom instead of the hotbar slot. */
    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void bennytools$scroll(long window, double xOffset, double yOffset, CallbackInfo ci) {
        if (yOffset != 0 && ZoomController.isHeld(Minecraft.getInstance())) {
            ZoomController.scroll(yOffset);
            ci.cancel();
        }
    }
}
