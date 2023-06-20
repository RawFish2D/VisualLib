package ua.rawfish2d.visuallib.cachedgui;

import lombok.Setter;
import org.joml.Vector4f;
import ua.rawfish2d.visuallib.cachedgui.utils.RenderUtils;
import ua.rawfish2d.visuallib.callable.ButtonCallback;
import ua.rawfish2d.visuallib.utils.RenderBuffer;
import ua.rawfish2d.visuallib.utils.RenderContext;

public class TextButtonToggle extends GuiElement {
	private final String text;
	@Setter
	private boolean pressed;
	@Setter
	protected ButtonCallback callback;

	public TextButtonToggle(String text, float x, float y) {
		this.text = text;
		this.setPos(x, y);
	}

	public TextButtonToggle(String text, float x, float y, float width, float height) {
		this.text = text;
		this.setPos(x, y);
		this.size.set(width, height);
	}

	@Override
	public void draw(RenderContext renderContext) {
		/*
		if (pressed) {
			RenderUtils.drawBorderedQuad(renderContext, pos.x, pos.y, pos.x + size.x, pos.y + size.y, 1f, 0xFF00007F, 0xFF00FF00);
		} else {
			RenderUtils.drawBorderedQuad(renderContext, pos.x, pos.y, pos.x + size.x, pos.y + size.y, 1f, 0xFF00007F, 0xFFFF0000);
		}
		renderContext.getFontRenderer().drawString(text, pos.x, pos.y, 0xFFFFFFFF);
		*/
		// fancy button
		//RenderUtils.drawQuad(renderContext, pos.x, pos.y, pos.x + size.x, pos.y + size.y, color);
		float x = pos.x;
		float y = pos.y;
		float x2 = pos.x + size.x;
		float y2 = pos.y + size.y;
		int borderColor = 0xFF000000;

		int topQuadColor;
		int bottomQuadColor;

		final int topActiveMouseColor = 0xFFAFBFFF;
		final int topMouseColor = 0xFF6F6F6F;
		final int topActiveColor = 0xFFAF00FF;
		final int topColor = 0xFF0F0F0F;
		final int bottomActiveMouseColor = 0xFFCFBFFF;
		final int bottomMouseColor = 0xFF6F6F6F;
		final int bottomActiveColor = 0xFFCF00FF;
		final int bottomColor = 0xFF2F2F2F;

		if (pressed && mouseOver) {
			topQuadColor = topActiveMouseColor;
			bottomQuadColor = bottomActiveMouseColor;
		} else if (!pressed && mouseOver) {
			topQuadColor = topMouseColor;
			bottomQuadColor = bottomMouseColor;
		} else if (pressed) {
			topQuadColor = topActiveColor;
			bottomQuadColor = bottomActiveColor;
		} else { // !toggled && !mouseOver
			topQuadColor = topColor;
			bottomQuadColor = bottomColor;
		}

		Vector4f geomUV = renderContext.getFontRenderer().getGeomUV();

		RenderBuffer renderBuffer = renderContext.getRenderBuffer();
		renderBuffer.addVertex(x, y, topQuadColor, geomUV.x, geomUV.z);
		renderBuffer.addVertex(x, y2, bottomQuadColor, geomUV.x, geomUV.w);
		renderBuffer.addVertex(x2, y2, bottomQuadColor, geomUV.y, geomUV.w);
		renderBuffer.addVertex(x2, y, topQuadColor, geomUV.y, geomUV.z);
		renderBuffer.addIndexCount(6);
		RenderUtils.drawBorder(renderContext, x, y, x2, y2, 1f, borderColor);

		int stringWidth = (int) ((size.x / 2f) - (renderContext.getFontRenderer().getStringWidth(text) / 2f));
		renderContext.getFontRenderer().drawStringWithShadow(text, stringWidth + pos.x, pos.y - 2f, 0xFFFFFFFF);
	}

	@Override
	public void mouseClick(int mouseX, int mouseY, int mouseButton, int action) {
		if (isMouseOver(mouseX, mouseY) && action == 1 && mouseButton == 0) {
			pressed = !pressed;
			if (callback != null) {
				callback.onClick(pressed);
			}
		}
	}
}
