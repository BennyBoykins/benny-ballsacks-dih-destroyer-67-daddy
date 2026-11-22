package dev.benny.tools;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/** In-game colour editor: pick a slot, drag RGBA sliders, or apply a preset. */
public class ThemeScreen extends Screen {
    private final Screen parent;
    private int selected = 0;
    private final List<ChannelSlider> sliders = new ArrayList<>();
    private Button chromaButton;

    private static final int LEFT = 20, TOP = 40, SLIDER_X = 150;

    public ThemeScreen(Screen parent) {
        super(Component.literal("Benny Tools - Theme"));
        this.parent = parent;
    }

    private Theme.Slot slot() {
        return Theme.Slot.values()[selected];
    }

    @Override
    protected void init() {
        sliders.clear();
        Theme.Slot[] slots = Theme.Slot.values();
        for (int i = 0; i < slots.length; i++) {
            final int idx = i;
            addRenderableWidget(Button.builder(Component.literal(slots[i].label), b -> {
                selected = idx;
                syncSliders();
            }).bounds(LEFT, TOP + i * 22, 118, 20).build());
        }

        String[] names = {"Red", "Green", "Blue", "Alpha"};
        int[] shifts = {16, 8, 0, 24};
        for (int i = 0; i < 4; i++) {
            ChannelSlider s = new ChannelSlider(SLIDER_X, TOP + i * 24, 150, 20, names[i], shifts[i]);
            sliders.add(s);
            addRenderableWidget(s);
        }

        chromaButton = addRenderableWidget(Button.builder(chromaLabel(), b -> {
            Theme.chroma = !Theme.chroma;
            b.setMessage(chromaLabel());
        }).bounds(SLIDER_X, TOP + 4 * 24 + 30, 150, 20).build());

        int x = LEFT;
        int y = this.height - 54;
        for (String preset : Theme.PRESETS.keySet()) {
            addRenderableWidget(Button.builder(Component.literal(preset), b -> {
                Theme.applyPreset(preset);
                syncSliders();
            }).bounds(x, y, 70, 20).build());
            x += 74;
        }

        addRenderableWidget(Button.builder(Component.literal("Reset"), b -> {
            Theme.reset();
            chromaButton.setMessage(chromaLabel());
            syncSliders();
        }).bounds(LEFT, this.height - 28, 80, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(LEFT + 90, this.height - 28, 80, 20).build());

        syncSliders();
    }

    private Component chromaLabel() {
        return Component.literal("Rainbow accent: " + (Theme.chroma ? "ON" : "OFF"));
    }

    private void syncSliders() {
        for (ChannelSlider s : sliders) s.sync();
    }

    @Override
    public void onClose() {
        Theme.save();
        if (this.minecraft != null) this.minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        g.drawCenteredString(this.font, this.title, this.width / 2, 14, 0xFFFFFFFF);
        g.drawString(this.font, "Editing: " + slot().label, SLIDER_X, TOP - 12, 0xFFAAAAAA);

        // swatch for the selected colour
        int sy = TOP + 4 * 24 + 4;
        g.fill(SLIDER_X - 1, sy - 1, SLIDER_X + 151, sy + 21, 0xFFFFFFFF);
        g.fill(SLIDER_X, sy, SLIDER_X + 150, sy + 20, 0xFF202020);
        g.fill(SLIDER_X, sy, SLIDER_X + 150, sy + 20, Theme.get(slot()));

        // live preview panel
        int px = 330, py = TOP;
        String[] lines = {"XYZ: 128.5 / 64.0 / -42.3", "> Home  120m", "Helmet 92%", "Chest 24%", "Boots 6%", "Totems: 3"};
        int[] cols = {Theme.text(), Theme.accent(), Theme.good(), Theme.warn(), Theme.bad(), Theme.text()};
        int w = 0;
        for (String s : lines) w = Math.max(w, this.font.width(s));
        g.fill(px - 4, py - 4, px + w + 4, py + lines.length * 12 + 2, Theme.bg());
        for (int i = 0; i < lines.length; i++) {
            g.drawString(this.font, lines[i], px, py + i * 12, cols[i]);
        }
        int ky = py + lines.length * 12 + 14;
        g.fill(px, ky, px + 24, ky + 20, Theme.keyActive());
        g.drawCenteredString(this.font, "W", px + 12, ky + 6, Theme.contrast(Theme.keyActive()));
        g.fill(px + 28, ky, px + 52, ky + 20, Theme.bg());
        g.drawCenteredString(this.font, "S", px + 40, ky + 6, Theme.text());
        g.drawString(this.font, "Preview", px, ky + 26, 0xFFAAAAAA);
    }

    /** One RGBA channel of the selected slot. */
    private class ChannelSlider extends AbstractSliderButton {
        private final String name;
        private final int shift;

        ChannelSlider(int x, int y, int w, int h, String name, int shift) {
            super(x, y, w, h, Component.empty(), 0.0);
            this.name = name;
            this.shift = shift;
            sync();
        }

        void sync() {
            int c = Theme.get(slot());
            this.value = ((c >>> shift) & 0xFF) / 255.0;
            this.active = shift != 24 || slot().hasAlpha;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            if (name == null) return; // may be called by the superclass constructor
            setMessage(Component.literal(name + ": " + (int) Math.round(value * 255)));
        }

        @Override
        protected void applyValue() {
            int c = Theme.get(slot());
            int v = (int) Math.round(value * 255);
            c = (c & ~(0xFF << shift)) | (v << shift);
            Theme.set(slot(), c);
        }
    }
}
