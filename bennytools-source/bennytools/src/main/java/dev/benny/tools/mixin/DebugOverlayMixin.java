package dev.benny.tools.mixin;

import dev.benny.tools.Config;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DebugScreenOverlay.class)
public abstract class DebugOverlayMixin {
    /** Streamer mode blocks the F3 screen, which shows coordinates and the server address. */
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void bennytools$hideF3(CallbackInfo ci) {
        if (Config.streamer) ci.cancel();
    }
}
