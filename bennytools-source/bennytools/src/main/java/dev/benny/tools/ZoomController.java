package dev.benny.tools;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

/** Hold-to-zoom with smooth easing and scroll-wheel adjustment. */
public final class ZoomController {
    private static float prev = 0f;
    private static float cur = 0f;
    private static double level = Config.zoomLevel;

    private ZoomController() {}

    public static void tick(Minecraft mc) {
        boolean active = BennyTools.ZOOM.isDown() && mc.screen == null;
        prev = cur;
        float target = active ? 1f : 0f;
        cur += (target - cur) * 0.35f;
        if (Math.abs(target - cur) < 0.001f) cur = target;
    }

    public static boolean isZooming() {
        return cur > 0.001f || prev > 0.001f;
    }

    /** True only while the key is held, so scroll is captured only then. */
    public static boolean isHeld(Minecraft mc) {
        return BennyTools.ZOOM.isDown() && mc.screen == null;
    }

    /** Multiplier applied to the FOV, in (0, 1]. */
    public static float multiplier(float partialTick) {
        float p = Mth.lerp(partialTick, prev, cur);
        float eased = p * p * (3f - 2f * p);
        return (float) (1.0 + (1.0 / level - 1.0) * eased);
    }

    public static void scroll(double amount) {
        level = Mth.clamp(level * Math.pow(1.15, amount), 1.5, 30.0);
        Config.zoomLevel = level;
    }
}
