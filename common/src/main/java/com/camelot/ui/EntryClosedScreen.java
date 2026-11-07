package com.camelot.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Small second-level screen used when the server closed the config entry. */
public final class EntryClosedScreen extends Screen {

    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int MAX_TEXT_WIDTH = 260;

    private final Screen parent;
    private final Component message;
    private final Component done;

    public EntryClosedScreen(Screen parent, Component title, Component message, Component done) {
        super(title);
        this.parent = parent;
        this.message = message;
        this.done = done;
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(done, button -> onClose())
                .bounds(width / 2 - 100, height / 2 + 40, 200, 20)
                .build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int centerX = width / 2;
        graphics.text(font, title, centerX - font.width(title) / 2, height / 2 - 40, TEXT_COLOR, true);
        int textWidth = Math.min(MAX_TEXT_WIDTH, width - 40);
        graphics.textWithWordWrap(font, message, centerX - textWidth / 2, height / 2 - 16, textWidth, TEXT_COLOR);
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreenAndShow(parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
