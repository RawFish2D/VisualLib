package ua.rawfish2d.visuallib.cachedfont;

import lombok.Getter;
import ua.rawfish2d.visuallib.font.FontRenderer;

import java.nio.ByteBuffer;

public class CachedFont {
	private String text;
	private float x;
	private float y;
	private int stringWidth;
	@Getter
	private int stringHeight;
	private float cachedX;
	private float cachedY;
	private final FontRenderer fontRenderer;
	private boolean updated = false;

	public CachedFont(FontRenderer fontRenderer, String string, float xpos, float ypos) {
		this.fontRenderer = fontRenderer;
		update(string, xpos, ypos);
	}

	public void update(String string) {
		this.text = string;
		this.stringWidth = fontRenderer.getStringWidth(string);
		this.stringHeight = fontRenderer.getStringHeight(string);
		this.updated = true;
	}

	public void update(float xpos, float ypos) {
		this.x = xpos;
		this.y = ypos;
		this.updated = true;
	}

	public void update(String string, float xpos, float ypos) {
		this.x = xpos;
		this.y = ypos;
		this.text = string;
		this.stringWidth = fontRenderer.getStringWidth(string);
		this.stringHeight = fontRenderer.getStringHeight(string);
		this.updated = true;
	}

	public void onRender(CachedFontBuffer buffer) {
		boolean debug = false;
		float bufferW = buffer.getFbo().getWidth();
		float bufferH = buffer.getFbo().getHeight();

		if (!debug) {
			final float w = stringWidth;
			final float h = stringHeight;
			ByteBuffer vertex = buffer.getVbo().getTemporaryBuffer(0); // vertex
			vertex.putFloat(x).putFloat(y);
			vertex.putFloat(x).putFloat(y + h);
			vertex.putFloat(x + w).putFloat(y + h);
			vertex.putFloat(x + w).putFloat(y);

			final float stepX = 1f / bufferW;
			final float stepY = 1f / bufferH;
			float u0 = cachedX * stepX;
			float u1 = (cachedX + w) * stepX;
			float v0 = (bufferH - cachedY - h) * stepY;
			float v1 = (bufferH - cachedY) * stepY;

			ByteBuffer tex = buffer.getVbo().getTemporaryBuffer(1); // tex
			tex.putFloat(u0).putFloat(v1);
			tex.putFloat(u0).putFloat(v0);
			tex.putFloat(u1).putFloat(v0);
			tex.putFloat(u1).putFloat(v1);
			buffer.getVbo().setIndexCount(buffer.getVbo().getIndexCount() + 6);
		} else {
			ByteBuffer vertex = buffer.getVbo().getTemporaryBuffer(0); // vertex
			vertex.putFloat(0).putFloat(0);
			vertex.putFloat(0).putFloat(bufferH);
			vertex.putFloat(bufferW).putFloat(bufferH);
			vertex.putFloat(bufferW).putFloat(0);

			ByteBuffer tex = buffer.getVbo().getTemporaryBuffer(1); // tex
			tex.putFloat(0f).putFloat(1f);
			tex.putFloat(0f).putFloat(0f);
			tex.putFloat(1f).putFloat(0f);
			tex.putFloat(1f).putFloat(1f);
			buffer.getVbo().setIndexCount(buffer.getVbo().getIndexCount() + 6);
		}
	}

	public void renderAt(FontRenderer fontRenderer, float xpos, float ypos) {
		this.cachedX = xpos;
		this.cachedY = ypos;
		fontRenderer.drawStringWithShadow(text, xpos, ypos);
	}
}
