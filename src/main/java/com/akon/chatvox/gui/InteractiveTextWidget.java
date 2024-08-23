package com.akon.chatvox.gui;

import com.akon.chatvox.mixin.ScreenAccessor;
import lombok.Getter;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.math.MathHelper;

public class InteractiveTextWidget extends ButtonWidget {

	@Getter
	private final Text text;
	@Getter(lazy = true)
	private final Text hoverText = Texts.setStyleIfAbsent(this.text.copy(), Style.EMPTY.withUnderline(true));
	private final TextRenderer textRenderer;
	@Getter
	private final boolean clickable;

	public InteractiveTextWidget(int x, int y, Text text, Screen screen) {
		super(x, y, ((ScreenAccessor)screen).getTextRenderer().getWidth(text), 10, text, button -> screen.handleTextClick(text.getStyle()), DEFAULT_NARRATION_SUPPLIER);
		this.text = text;
		this.textRenderer = ((ScreenAccessor)screen).getTextRenderer();
		this.clickable = text.getStyle().getClickEvent() != null;
	}

	@Override
	public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		var text = this.isMouseOver(mouseX, mouseY) && this.clickable ? this.getHoverText() : this.text;
		context.drawTextWithShadow(this.textRenderer, text, this.getX(), this.getY(), 0xFFFFFF | MathHelper.ceil(this.alpha * 255.0F) << 24);
		if (this.isMouseOver(mouseX, mouseY)) {
			context.drawHoverEvent(this.textRenderer, text.getStyle(), mouseX, mouseY);
		}
	}

	@Override
	public void playDownSound(SoundManager soundManager) {
		if (this.clickable)
			super.playDownSound(soundManager);
	}
}
