package dev.benny.tools;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/** Settings for Staff Protection and Spectator Protection. Both panels preview live behind this screen. */
public class ProtectionScreen extends Screen {
    private final Screen parent;
    private static final int COL_W = 170, ROW = 24;

    public ProtectionScreen(Screen parent) {
        super(Component.literal("Staff / Spectator Protection"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x1 = this.width / 2 - COL_W - 8;
        int x2 = this.width / 2 + 8;
        int y = 70;

        // ---- Staff Protection column ----
        toggle(x1, y, "Enabled", () -> ProtectionConfig.staffEnabled, v -> ProtectionConfig.staffEnabled = v);
        toggle(x1, y + ROW, "Screen alert", () -> ProtectionConfig.staffAlert, v -> ProtectionConfig.staffAlert = v);
        slider(x1, y + ROW * 2, "Radius", "m", ProtectionConfig.MIN_RADIUS, ProtectionConfig.MAX_RADIUS,
                () -> ProtectionConfig.staffRadius, v -> ProtectionConfig.staffRadius = v);
        toggle(x1, y + ROW * 3, "Show distance", () -> ProtectionConfig.staffShowDistance, v -> ProtectionConfig.staffShowDistance = v);
        anchor(x1, y + ROW * 4, () -> ProtectionConfig.staffAnchor, a -> ProtectionConfig.staffAnchor = a);
        slider(x1, y + ROW * 5, "X offset", "", 0, 400, () -> ProtectionConfig.staffX, v -> ProtectionConfig.staffX = v);
        slider(x1, y + ROW * 6, "Y offset", "", 0, 400, () -> ProtectionConfig.staffY, v -> ProtectionConfig.staffY = v);

        // ---- Spectator Protection column ----
        toggle(x2, y, "Enabled", () -> ProtectionConfig.specEnabled, v -> ProtectionConfig.specEnabled = v);
        toggle(x2, y + ROW, "Screen alert", () -> ProtectionConfig.specAlert, v -> ProtectionConfig.specAlert = v);
        anchor(x2, y + ROW * 4, () -> ProtectionConfig.specAnchor, a -> ProtectionConfig.specAnchor = a);
        slider(x2, y + ROW * 5, "X offset", "", 0, 400, () -> ProtectionConfig.specX, v -> ProtectionConfig.specX = v);
        slider(x2, y + ROW * 6, "Y offset", "", 0, 400, () -> ProtectionConfig.specY, v -> ProtectionConfig.specY = v);

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(this.width / 2 - 50, y + ROW * 7 + 14, 100, 20).build());
    }

    private void toggle(int x, int y, String label, BooleanSupplier get, Consumer<Boolean> set) {
        Supplier<Component> msg = () -> Component.literal(label + ": " + (get.getAsBoolean() ? "ON" : "OFF"));
        addRenderableWidget(Button.builder(msg.get(), b -> {
            set.accept(!get.getAsBoolean());
            b.setMessage(msg.get());
        }).bounds(x, y, COL_W, 20).build());
    }

    private void anchor(int x, int y, Supplier<ProtectionConfig.Anchor> get, Consumer<ProtectionConfig.Anchor> set) {
        Supplier<Component> msg = () -> Component.literal("Position: " + get.get().label);
        addRenderableWidget(Button.builder(msg.get(), b -> {
            set.accept(get.get().next());
            b.setMessage(msg.get());
        }).bounds(x, y, COL_W, 20).build());
    }

    private void slider(int x, int y, String label, String suffix, int min, int max, IntSupplier get, IntConsumer set) {
        addRenderableWidget(new IntSlider(x, y, COL_W, 20, label, suffix, min, max, get.getAsInt(), set));
    }

    @Override
    public void onClose() {
        ProtectionConfig.save();
        if (this.minecraft != null) this.minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        int x1 = this.width / 2 - COL_W - 8;
        int x2 = this.width / 2 + 8;
        g.drawCenteredString(this.font, this.title, this.width / 2, 14, 0xFFFFFFFF);
        g.drawCenteredString(this.font, "Both panels preview live on the screen.", this.width / 2, 28, 0xFFAAAAAA);
        g.drawString(this.font, "Staff Protection", x1, 54, 0xFFFFFFFF);
        g.drawString(this.font, "Spectator Protection", x2, 54, 0xFFFFFFFF);
        g.drawString(this.font, "Lists players within the radius", x1, 70 + ROW * 7 - 4, 0xFF888888);
        g.drawString(this.font, "Lists every player in spectator mode", x2, 70 + ROW * 7 - 4, 0xFF888888);
    }

    /** Integer slider. */
    private static class IntSlider extends AbstractSliderButton {
        private final String label, suffix;
        private final int min, max;
        private final IntConsumer setter;

        IntSlider(int x, int y, int w, int h, String label, String suffix, int min, int max, int current, IntConsumer setter) {
            super(x, y, w, h, Component.empty(), 0.0);
            this.label = label;
            this.suffix = suffix;
            this.min = min;
            this.max = max;
            this.setter = setter;
            this.value = (current - min) / (double) (max - min);
            updateMessage();
        }

        private int current() {
            return min + (int) Math.round(value * (max - min));
        }

        @Override
        protected void updateMessage() {
            if (label == null) return; // may be called by the superclass constructor
            setMessage(Component.literal(label + ": " + current() + suffix));
        }

        @Override
        protected void applyValue() {
            setter.accept(current());
        }
    }
}
