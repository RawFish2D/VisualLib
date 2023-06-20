package ua.rawfish2d.visuallib.font;

import org.lwjgl.opengl.GL11;

public abstract class BaseFontRender {
	protected int textureWidth;
	protected int textureHeight;
	protected int fontTextureID;
	protected float scale = 1f;
	protected float shadowOffset = 1f;

	public void setScale(float scale) {
		this.scale = scale;
	}

	public void setShadowOffset(float shadowOffset) {
		this.shadowOffset = shadowOffset;
	}

	public int getFontTextureID() {
		return fontTextureID;
	}

	/**
	 * @param text - string of text
	 * @return string width in pixels
	 */
	public abstract int getStringWidth(String text);

	/**
	 * @param text - string of text
	 * @return string height in pixels
	 */
	public abstract int getStringHeight(String text);

	/**
	 * Delete all font data from memory
	 */
	public void free() {
		if (fontTextureID != 0) {
			GL11.glDeleteTextures(fontTextureID);
			fontTextureID = 0;
		}
	}

	/**
	 * @param text   - string of text
	 * @param x      - horizontal x coordinate
	 * @param y      - vertical y coordinate
	 * @param color  - color in ARGB format
	 * @param shadow - if true, darker color will be used
	 */
	public abstract void drawString(String text, float x, float y, int color, boolean shadow);

	/**
	 * @param text  - string of text
	 * @param x     - horizontal x coordinate
	 * @param y     - vertical y coordinate
	 * @param color - color in ARGB format
	 */
	public void drawString(String text, float x, float y, int color) {
		drawString(text, x, y, color, false);
	}

	/**
	 * @param text   - string of text
	 * @param x      - horizontal x coordinate
	 * @param y      - vertical y coordinate
	 * @param shadow - if true, darker color will be used
	 */
	public abstract void drawString(String text, float x, float y, boolean shadow);

	/**
	 * @param text - string of text
	 * @param x    - horizontal x coordinate
	 * @param y    - vertical y coordinate
	 */
	public void drawString(String text, float x, float y) {
		drawString(text, x, y, false);
	}

	/**
	 * @param text  - string of text
	 * @param x     - horizontal x coordinate
	 * @param y     - vertical y coordinate
	 * @param color - color in ARGB format
	 */
	public void drawStringWithShadow(String text, float x, float y, int color) {
		final int shadowColor = (color & 0xFCFCFC) >> 2 | color & 0xFF000000;
		drawString(text, x + shadowOffset, y + shadowOffset, shadowColor, true);
		drawString(text, x, y, color, false);
	}

	/**
	 * @param text         - string of text
	 * @param x            - horizontal x coordinate
	 * @param y            - vertical y coordinate
	 * @param color        - color in ARGB format
	 * @param shadowOffset - shadow distance from text in pixels
	 */
	public void drawStringWithShadow(String text, float x, float y, int color, float shadowOffset) {
		final int shadowColor = (color & 0xFCFCFC) >> 2 | color & 0xFF000000;
		drawString(text, x + shadowOffset, y + shadowOffset, shadowColor, true);
		drawString(text, x, y, color, false);
	}

	/**
	 * @param text - string of text
	 * @param x    - horizontal x coordinate
	 * @param y    - vertical y coordinate
	 */
	public void drawStringWithShadow(String text, float x, float y) {
		final int shadowColor = (0xFCFCFC) >> 2 | 0xFF000000;
		drawString(text, x + this.shadowOffset, y + this.shadowOffset, shadowColor, true);
		drawString(text, x, y, 0xFFFFFFFF, false);
	}

	/**
	 * @param text         - string of text
	 * @param x            - horizontal x coordinate
	 * @param y            - vertical y coordinate
	 * @param shadowOffset - shadow distance from text in pixels
	 */
	public void drawStringWithShadow(String text, float x, float y, float shadowOffset) {
		final int shadowColor = (0xFCFCFC) >> 2 | 0xFF000000;
		drawString(text, x + shadowOffset, y + shadowOffset, shadowColor, true);
		drawString(text, x, y, 0xFFFFFFFF, false);
	}
}
