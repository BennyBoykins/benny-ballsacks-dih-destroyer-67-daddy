package dev.benny.tools.mixin;

import dev.benny.tools.ZoomController;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void bennytools$zoom(Camera camera, float partialTick, boolean useFovSetting,
                                 CallbackInfoReturnable<Float> cir) {
        if (ZoomController.isZooming()) {
            cir.setReturnValue(cir.getReturnValueF() * ZoomController.multiplier(partialTick));
        }
    }
}
