package com.camelot.ui;

import com.camelot.ui.Screens;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public final class EntryClosedScreen extends Screen {

    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int MAX_TEXT_WIDTH = 260;
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int LINE_HEIGHT = 10;

    private final Screen parent;
    private final Component message;
    private final Component done;

    private List<FormattedCharSequence> wrapped = List.of();

    public EntryClosedScreen(Screen parent, Component title, Component message, Component done) {
        super(title);
        this.parent = parent;
        this.message = message;
        this.done = done;
    }

    @Override
    protected void init() {
        wrapped = font.split(message, Math.min(MAX_TEXT_WIDTH, width - 40));
        int textHeight = LINE_HEIGHT + 6 + wrapped.size() * LINE_HEIGHT;
        int top = (height - textHeight - 12 - BUTTON_HEIGHT) / 2;
        addRenderableWidget(Button.builder(done, button -> onClose())
                .bounds(width / 2 - BUTTON_WIDTH / 2, top + textHeight + 12, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int textHeight = LINE_HEIGHT + 6 + wrapped.size() * LINE_HEIGHT;
        int top = (height - textHeight - 12 - BUTTON_HEIGHT) / 2;
        graphics.text(font, title, (width - font.width(title)) / 2, top, TEXT_COLOR, true);
        int y = top + LINE_HEIGHT + 6;
        for (FormattedCharSequence line : wrapped) {
            graphics.text(font, line, (width - font.width(line)) / 2, y, TEXT_COLOR, false);
            y += LINE_HEIGHT;
        }
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            Screens.open(parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
