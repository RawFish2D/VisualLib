package ua.rawfish2d.visuallib.font;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector4f;
import ua.rawfish2d.visuallib.utils.RenderBuffer;
import ua.rawfish2d.visuallib.utils.RenderContext;

import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

public class FontRenderer extends BaseSystemFontRender {
	private RenderContext renderContext;
	private final int[] colorCode = new int[32];
	private int currentColor = 0xFFFFFFFF;
	private boolean randomStyle = false;
	private boolean boldStyle = false;
	private boolean strikethroughStyle = false;
	private boolean underlineStyle = false;
	private boolean italicStyle = false;
	@Setter
	@Getter
	private Vector4f geomUV = new Vector4f();

	public FontRenderer(Font font, int imgWidth, int imgHeight, boolean antiAlias, boolean fractionalMetrics, int fontMarginX, int fontMarginY, int charactersCount) {
		super(font, imgWidth, imgHeight, antiAlias, fractionalMetrics, charactersCount, fontMarginX, fontMarginY, true);
		final float u0 = 0f;
		final float u1 = 0.0078125f;
		final float v0 = 0.99975586f;
		final float v1 = 1.0f;
		geomUV.set(u0, u1, v0, v1);
		initColorCodes();
	}

	public FontRenderer(Font font, int imgWidth, int imgHeight, boolean antiAlias, boolean fractionalMetrics, int fontMarginX, int fontMarginY, char[] chars) {
		super(font, imgWidth, imgHeight, antiAlias, fractionalMetrics, chars, fontMarginX, fontMarginY, true);
		final float u0 = 0f;
		final float u1 = 0.0078125f;
		final float v0 = 0.99975586f;
		final float v1 = 1.0f;
		geomUV.set(u0, u1, v0, v1);
		initColorCodes();
	}

	public FontRenderer(String imageFileName, String charsDataFileName) {
		super(imageFileName, charsDataFileName, true);
		final float u0 = 0f;
		final float u1 = 0.0078125f;
		final float v0 = 0.99975586f;
		final float v1 = 1.0f;
		geomUV.set(u0, u1, v0, v1);
		initColorCodes();
	}

	public void setRenderContext(RenderContext renderContext) {
		this.renderContext = renderContext;
	}

	private void setColor(int newColor) {
		this.currentColor = newColor;
	}

	public void drawString(String text, float x, float y, boolean shadow) {
		this.randomStyle = false;
		this.boldStyle = false;
		this.strikethroughStyle = false;
		this.underlineStyle = false;
		this.italicStyle = false;
		final int origColor = currentColor;

		final int size = text.length();
		final char[] chars = text.toCharArray();

		int index = 0;
		while (index < size) {
			float offset = 0;
			char character = chars[index];
			if (character < charsData.length) {
				if (character == 167 && index + 1 < chars.length) {
					char c = Character.toLowerCase(chars[index + 1]);
					int colorIndex = "0123456789abcdefklmnor".indexOf(c);

					if (colorIndex < 16) {
						this.randomStyle = false;
						this.boldStyle = false;
						this.strikethroughStyle = false;
						this.underlineStyle = false;
						this.italicStyle = false;

						if (colorIndex < 0) {
							colorIndex = 15;
						}

						if (shadow) {
							colorIndex += 16;
						}

						int newColor = this.colorCode[colorIndex];
						setColor(newColor | ((origColor & 255) << 24));
					} else if (colorIndex == 16) {
						this.randomStyle = true;
					} else if (colorIndex == 17) {
						this.boldStyle = true;
					} else if (colorIndex == 18) {
						this.strikethroughStyle = true;
					} else if (colorIndex == 19) {
						this.underlineStyle = true;
					} else if (colorIndex == 20) {
						this.italicStyle = true;
					} else {
						this.randomStyle = false;
						this.boldStyle = false;
						this.strikethroughStyle = false;
						this.underlineStyle = false;
						this.italicStyle = false;
						setColor(origColor);
					}

					++index;
				} else {
					if (this.randomStyle) {
						int charIndex = 0;
						int charWidth = this.charsData[character].width;

						while (charWidth != this.charsData[charIndex].width) {
							charIndex = ThreadLocalRandom.current().nextInt(charsData.length);
						}

						character = (char) (charIndex);
					}
					this.drawChar(character, x, y);

					if (this.boldStyle) {
						offset += scale;
						this.drawChar(character, x + offset, y);
					}
					offset += charsData[character].charWidth * scale;

					x += offset;
				}
			}

			drawGeometry(x, y, offset);
			++index;
		}
	}

	public void drawString(String text, float x, float y, int color, boolean shadow) {
		setColor(color);
		drawString(text, x, y, shadow);
	}

	private void drawGeometry(float posX, float posY, float offset) {
		float lineHeight = 1f;
		if (strikethroughStyle) {
			float strikeY = this.fontHeight / 2f;
			drawGeometry(posX - offset, posY + strikeY, posX, posY + strikeY + lineHeight, scale, currentColor);
		}
		if (underlineStyle) {
			float strikeY = 8f;
			drawGeometry(posX - offset / 2f, posY + strikeY, posX, posY + strikeY + lineHeight, scale, currentColor);
		}
	}

	public void drawGeometry(float posX, float posY, float posX2, float posY2, float scale, int color) {
		final float w = (posX2 - posX) * scale;
		final float h = (posY2 - posY) * scale;

		final RenderBuffer renderBuffer = renderContext.getRenderBuffer();
		renderBuffer.addVertex(posX, posY, color, geomUV.x, geomUV.z);
		renderBuffer.addVertex(posX, posY + h, color, geomUV.x, geomUV.w);
		renderBuffer.addVertex(posX + w, posY + h, color, geomUV.y, geomUV.w);
		renderBuffer.addVertex(posX + w, posY, color, geomUV.y, geomUV.z);
		renderBuffer.addIndexCount(6);
	}

	@Override
	protected void drawChar(char character, float x, float y) {
		final CharData charData = charsData[character];

		final float width = charData.width * this.scale;
		final float height = charData.height * this.scale;
		final float u0 = charData.u0;
		final float v0 = charData.v0;
		final float u1 = charData.u1;
		final float v1 = charData.v1;
		final float italicOffset = this.italicStyle ? 2f : 0f;

		final RenderBuffer renderBuffer = renderContext.getRenderBuffer();
		renderBuffer.addVertex(x + width - italicOffset, y + height, currentColor, u1, v1);
		renderBuffer.addVertex(x + width + italicOffset, y, currentColor, u1, v0);
		renderBuffer.addVertex(x + italicOffset, y, currentColor, u0, v0);
		renderBuffer.addVertex(x - italicOffset, y + height, currentColor, u0, v1);
		renderBuffer.addIndexCount(6);
	}

	@Override
	public int getStringWidth(String text) {
		final int size = text.length();
		final char[] chars = text.toCharArray();
		float width = 0;
		int index = 0;

		char character = ' ';
		while (index < size) {
			float offset = 0;
			character = chars[index];
			if (character < charsData.length) {
				if (character == 167 && index + 1 < chars.length) {
					char c = Character.toLowerCase(chars[index + 1]);
					int colorIndex = "0123456789abcdefklmnor".indexOf(c);

					if (colorIndex < 16) {
						this.boldStyle = false;
					} else if (colorIndex == 17) {
						this.boldStyle = true;
					} else if (colorIndex == 21) {
						this.boldStyle = false;
					}
					++index;
				} else {
					if (this.boldStyle) {
						offset += scale;
					}
					offset += charsData[character].charWidth * scale;
					width += offset * scale;
				}
			}
			++index;
		}
		width += (charsData[character].charWidth * scale) / 1.5f;
		return (int) Math.ceil(width);
	}

	@Override
	public int getStringHeight(String text) {
		float height = 0;
		final int size = text.length();
		final char[] chars = text.toCharArray();
		int index = 0;

		while (index < size) {
			char character = chars[index];
			if (character < charsData.length) {
				if (character == 167 && index + 1 < chars.length) {
					++index;
				} else {
					if (charsData[character].height > height) {
						height = charsData[character].height;
					}
				}
			}
			++index;
		}
		return (int) Math.ceil(height);
	}

	private void initColorCodes() {
		for (int i = 0; i < colorCode.length; ++i) {
			int j = (i >> 3 & 1) * 85;
			int r = (i >> 2 & 1) * 170 + j;
			int g = (i >> 1 & 1) * 170 + j;
			int b = (i & 1) * 170 + j;

			if (i == 6) {
				r += 85;
			}

			if (i >= 16) {
				r /= 4;
				g /= 4;
				b /= 4;
			}

			this.colorCode[i] = (r & 255) << 16 | (g & 255) << 8 | b & 255;
		}
	}
}
