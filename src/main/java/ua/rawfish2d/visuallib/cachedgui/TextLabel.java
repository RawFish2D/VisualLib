package ua.rawfish2d.visuallib.cachedgui;

import lombok.Setter;
import ua.rawfish2d.visuallib.utils.RenderContext;

public class TextLabel extends GuiElement {
	@Setter
	private String text;

	public TextLabel(String text, float x, float y) {
		this.text = text;
		this.setPos(x, y);
	}

	public TextLabel(String text, float x, float y, float width, float height) {
		this.text = text;
		this.setPos(x, y);
		this.size.set(width, height);
	}

	@Override
	public void draw(RenderContext renderContext) {
		int stringWidth = (int) ((size.x / 2f) - (renderContext.getFontRenderer().getStringWidth(text) / 2f));
		renderContext.getFontRenderer().drawString(text, stringWidth + pos.x, pos.y, 0xFFFFFFFF);
	}
}
