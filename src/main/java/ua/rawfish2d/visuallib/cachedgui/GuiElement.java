package ua.rawfish2d.visuallib.cachedgui;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector2f;
import ua.rawfish2d.visuallib.utils.RenderContext;

public abstract class GuiElement {
	@Setter
	@Getter
	protected Vector2f size = new Vector2f(100f, 20f);
	@Getter
	protected Vector2f pos = new Vector2f(0, 0);
	protected boolean mouseOver = false;

	public void setPos(float x, float y) {
		pos.set(x, y);
	}

	public abstract void draw(RenderContext renderContext);

	public void mouseMove(int mouseX, int mouseY) {
		mouseOver = isMouseOver(mouseX, mouseY);
	}

	public void mouseClick(int mouseX, int mouseY, int mouseButton, int action) {

	}

	public boolean isMouseOver(int mouseX, int mouseY) {
		return mouseX >= pos.x && mouseX <= pos.x + size.x &&
				mouseY >= pos.y && mouseY <= pos.y + size.y;
	}
}
