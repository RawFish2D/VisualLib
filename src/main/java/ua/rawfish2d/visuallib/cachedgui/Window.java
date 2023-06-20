package ua.rawfish2d.visuallib.cachedgui;

import org.joml.Vector2f;
import ua.rawfish2d.visuallib.cachedgui.utils.RenderUtils;
import ua.rawfish2d.visuallib.utils.RenderContext;

import java.util.ArrayList;
import java.util.List;

public class Window extends GuiElement {
	private final TextLabel title;
	private final List<GuiElement> guiElementList = new ArrayList<>();
	private float dragX;
	private float dragY;
	private float lastDragX;
	private float lastDragY;
	private boolean dragging;

	public Window(String title, float x, float y, float width, float height) {
		this.title = new TextLabel(title, x, y, width, 20f);
		this.setPos(x, y);
		this.size.set(width, height);
	}

	public void addElement(GuiElement element, boolean guiPosRelativeToWindow) {
		if (guiPosRelativeToWindow) {
			final Vector2f guiPos = element.getPos();
			guiPos.set(pos.x + guiPos.x, pos.y + guiPos.y);
		}
		this.guiElementList.add(element);
	}

	public void setTitle(String title) {
		this.title.setText(title);
	}

	@Override
	public void draw(RenderContext renderContext) {
		//RenderUtils.drawBorderedQuad(renderContext, pos.x, pos.y, pos.x + size.x, pos.y + size.y, 1f, 0xFF00002F, 0xFF007F00);
		//fancy
		RenderUtils.drawBorderedQuad(renderContext, pos.x, pos.y, pos.x + size.x, pos.y + size.y, 1f, 0x9F000000, 0xFFAF00FF);
		title.draw(renderContext);

		for (GuiElement element : guiElementList) {
			element.draw(renderContext);
		}
	}

	@Override
	public void setPos(float x, float y) {
		super.setPos(x, y);
		this.dragX = x;
		this.dragY = y;
		this.lastDragX = dragX;
		this.lastDragY = dragY;
	}

	public void setWindowPos(float x, float y, boolean drag) {
		for (GuiElement element : guiElementList) {
			final Vector2f guiPos = element.getPos();
			float relativeX = guiPos.x - pos.x;
			float relativeY = guiPos.y - pos.y;
			guiPos.set(x + relativeX, y + relativeY);
		}
		//pos.set(mouseX - pos.x + dragPosX, mouseY - pos.y + dragPosY);
		final Vector2f titlePos = title.getPos();
		float relativeX = titlePos.x - pos.x;
		float relativeY = titlePos.y - pos.y;
		titlePos.set(x + relativeX, y + relativeY);

		if (!drag) {
			setPos(x, y);
		} else {
			pos.x = x;
			pos.y = y;
			dragX = x;
			dragY = y;
		}
	}

	private void dragWindow(float mouseX, float mouseY) {
		final float newWindowPosX = mouseX - lastDragX;
		final float newWindowPosY = mouseY - lastDragY;
		setWindowPos(newWindowPosX, newWindowPosY, true);
	}

	@Override
	public void mouseMove(int mouseX, int mouseY) {
		title.mouseMove(mouseX, mouseY);
		for (GuiElement guiElement : guiElementList) {
			guiElement.mouseMove(mouseX, mouseY);
		}
		if (dragging) {
			dragWindow(mouseX, mouseY);
		}
	}

	@Override
	public void mouseClick(int mouseX, int mouseY, int mouseButton, int action) {
		for (GuiElement guiElement : guiElementList) {
			guiElement.mouseClick(mouseX, mouseY, mouseButton, action);
		}
		title.mouseClick(mouseX, mouseY, mouseButton, action);
		if (title.mouseOver && mouseButton == 0 && action == 1) {
			dragging = true;
			dragX = pos.x;
			dragY = pos.y;
			lastDragX = mouseX - dragX;
			lastDragY = mouseY - dragY;
		} else {
			dragging = false;
		}
	}
}
