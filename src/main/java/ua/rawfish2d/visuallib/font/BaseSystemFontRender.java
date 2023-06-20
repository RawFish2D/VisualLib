package ua.rawfish2d.visuallib.font;

import de.matthiasmann.twl.utils.PNGDecoder;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import ua.rawfish2d.visuallib.loader.TextureData;
import ua.rawfish2d.visuallib.loader.TextureLoader;
import ua.rawfish2d.visuallib.utils.GLSM;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public abstract class BaseSystemFontRender extends BaseFontRender {
	protected CharData[] charsData;
	protected Font font;
	protected boolean antiAlias;
	protected boolean fractionalMetrics;
	protected int fontHeight = -1;
	protected int fontMarginX = 2;
	protected int fontMarginY = 3;
	protected boolean useOneColor = true;

	public BaseSystemFontRender(Font font, int textureWidth, int textureHeight, boolean antiAlias, boolean fractionalMetrics, int charactersCount, int fontMarginX, int fontMarginY, boolean useOneColor) {
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
		this.font = font;
		this.antiAlias = antiAlias;
		this.fractionalMetrics = fractionalMetrics;
		this.charsData = new CharData[charactersCount];
		this.fontMarginX = fontMarginX;
		this.fontMarginY = fontMarginY;
		this.useOneColor = useOneColor;
		uploadBufferedImageToGPU(generateFontImage(), true);
	}

	public BaseSystemFontRender(Font font, String imageFileName, String charsDataFileName, boolean useOneColor) {
		this.useOneColor = useOneColor;
		this.font = font;
		loadTexture(imageFileName, true);
		loadCharactersData(charsDataFileName);
	}

	private void loadTexture(String imageFileName, boolean pixelated) {
		PNGDecoder.Format format = PNGDecoder.Format.RGBA;
		TextureData texture = TextureLoader.getImageData(imageFileName, format);

		this.textureWidth = (int) texture.width;
		this.textureHeight = (int) texture.height;
		this.fontTextureID = GLSM.instance.glGenTextures();
		GLSM.instance.glBindTexture(fontTextureID);

		if (pixelated) {
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		} else {
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		}

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL13.GL_CLAMP_TO_BORDER);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL13.GL_CLAMP_TO_BORDER);

		ByteBuffer buffer = ByteBuffer.allocateDirect(textureWidth * textureHeight);
		for (int index = 0; index < texture.data.capacity(); ++index) {
			byte b = texture.data.get();
			if (index % 4 == 0) {
				buffer.put(b);
			}
		}
		buffer.flip();

		// FontTestOld.print("texture size: " + OtherUtils.humanReadableByteCountBin(textureWidth * textureHeight * format.getNumComponents()));
		if (useOneColor) {
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RED, textureWidth, textureHeight, 0, GL11.GL_RED, GL11.GL_UNSIGNED_BYTE, buffer);
			GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
			GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
		} else {
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, textureWidth, textureHeight, 0, GL11.GL_RGBA8, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, texture.data);
		}
	}

	protected BufferedImage generateFontImage() {
		BufferedImage bufferedImage = new BufferedImage(textureWidth, textureHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) bufferedImage.getGraphics();
		g.setFont(font);
		g.setColor(new Color(255, 255, 255, 0));
		g.fillRect(0, 0, textureWidth, textureHeight);
		g.setColor(Color.WHITE);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, fractionalMetrics ? RenderingHints.VALUE_FRACTIONALMETRICS_ON : RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, antiAlias ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAlias ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
		FontMetrics fontMetrics = g.getFontMetrics();
		int charHeight = 0;
		int positionX = 0;
		int positionY = 1;

		for (int i = 0; i < charsData.length; ++i) {
			char ch = (char) i;
			CharData data = new CharData();
			charsData[i] = data;
			if (!font.canDisplay(ch)) {
				continue;
			}

			// new
			// int fontMarginX = 2;
			// int fontMarginY = 3;

			Rectangle2D dimensions = fontMetrics.getStringBounds(String.valueOf(ch), g);
			data.charWidth = dimensions.getBounds().width;
			data.width = (int) (data.charWidth + fontMarginX);
			data.height = dimensions.getBounds().height;

			if (data.height > charHeight) {
				charHeight = data.height + fontMarginY;
				this.fontHeight = charHeight;
			}

			if (positionX + data.width >= textureWidth) {
				positionX = 0;
				positionY += charHeight;
				charHeight = 0;
			}

			float renderX = ((float) positionX) / textureWidth;
			float renderY = ((float) positionY) / textureHeight;
			float renderWidth = ((float) data.width) / textureWidth;
			float renderHeight = ((float) data.height) / textureHeight;
			data.u0 = renderX;
			data.v0 = renderY;
			data.u1 = renderX + renderWidth;
			data.v1 = renderY + renderHeight;

			g.drawString(String.valueOf(ch), positionX + 2, positionY + fontMetrics.getAscent());
			positionX += data.width;
		}

		g.fillRect(0, textureHeight - 2, textureWidth, textureHeight - 1);
		// g.dispose(); // corrupts image

		return bufferedImage;
	}

	public void saveCharactersData(String fileName) {
		final File file = new File(fileName);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8))) {
			out.write(String.format(Locale.US, "%d\r\n", charsData.length));
			for (final CharData charData : charsData) {
				final String text = String.format(Locale.US, "%f %f %f %f %f %f %f\r\n", (float) charData.width, (float) charData.height, charData.charWidth, charData.u0, charData.v0, charData.u1, charData.v1);
				out.write(text);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void loadCharactersData(String fileName) {
		int index = 0;
		String currentLine;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(fileName)), StandardCharsets.UTF_8))) {
			final int length = Integer.parseInt(reader.readLine());
			charsData = new CharData[length];
			while ((currentLine = reader.readLine()) != null) {
				final String[] split = currentLine.split(" ");
				if (split.length == 7) {
					final CharData data = new CharData();
					data.width = (int) Float.parseFloat(split[0]);
					data.height = (int) Float.parseFloat(split[1]);
					data.charWidth = Float.parseFloat(split[2]);
					data.u0 = Float.parseFloat(split[3]);
					data.v0 = Float.parseFloat(split[4]);
					data.u1 = Float.parseFloat(split[5]);
					data.v1 = Float.parseFloat(split[6]);
					charsData[index++] = data;

					if (data.height > fontHeight) {
						fontHeight = data.height;
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	protected void drawChar(char c, float x, float y) {
		CharData charData = charsData[c];
		final float width = charData.width;
		final float height = charData.height;
		final float u0 = charData.u0;
		final float v0 = charData.v0;
		final float u1 = charData.u1;
		final float v1 = charData.v1;

		GL11.glTexCoord2f(u1, v1);
		GL11.glVertex2f(x + width, y + height);
		GL11.glTexCoord2f(u1, v0);
		GL11.glVertex2f(x + width, y);
		GL11.glTexCoord2f(u0, v0);
		GL11.glVertex2f(x, y);
		GL11.glTexCoord2f(u0, v1);
		GL11.glVertex2f(x, y + height);
	}

	@Override
	public void free() {
		GLSM.instance.glDeleteTextures(fontTextureID);
	}

	public void saveTexture(String fileName) {
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		final GLSM glsm = GLSM.instance;
		final int fboWidth = textureWidth;
		final int fboHeight = textureHeight;

		final IntBuffer oldViewport = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
		glGetIntegerv(GL_VIEWPORT, oldViewport);
		final int viewPortX = oldViewport.get(0);
		final int viewPortY = oldViewport.get(1);
		final int viewPortW = oldViewport.get(2);
		final int viewPortH = oldViewport.get(3);
		glViewport(0, 0, fboWidth, fboHeight);

		ByteBuffer buffer;
		BufferedImage image;

		final int bufferSize = (fboWidth * fboHeight * Integer.BYTES);
		buffer = ByteBuffer.allocateDirect(bufferSize);
		int fbo = glGenFramebuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, fbo);
		glBindTexture(GL_TEXTURE_2D, fontTextureID);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, fontTextureID, 0);
		glBindFramebuffer(GL_FRAMEBUFFER, 0);

		glBindFramebuffer(GL_READ_FRAMEBUFFER, fbo);
		glReadBuffer(GL_COLOR_ATTACHMENT0);
		glReadPixels(0, 0, fboWidth, fboHeight, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
		glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
		glDeleteFramebuffers(fbo);

		image = new BufferedImage(fboWidth, fboHeight, BufferedImage.TYPE_INT_ARGB);

		for (int y = 0; y < fboHeight; ++y) {
			for (int x = 0; x < fboWidth; ++x) {
				int blue = (((int) buffer.get()) & 255) << 16;
				int green = (((int) buffer.get()) & 255) << 8;
				int red = (((int) buffer.get()) & 255) << 0;
				int alpha = (((int) buffer.get()) & 255) << 24;

				int newPixel = blue | green | red | alpha;
				image.setRGB(x, y, newPixel);
			}
		}

		//ExecutorService es = Executors.newSingleThreadExecutor();
		//es.execute(() -> {
		File outputfile = new File(fileName);
		try {
			ImageIO.write(image, "png", outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//es.shutdown();
		//});
		buffer.clear();
		glsm.glBindTexture(0);

		// setting old viewport
		glViewport(viewPortX, viewPortY, viewPortW, viewPortH);
	}

	private void uploadBufferedImageToGPU(BufferedImage bufferedImage, boolean pixelated) {
		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();
		this.fontTextureID = GLSM.instance.glGenTextures();
		GLSM.instance.glBindTexture(fontTextureID);

		if (pixelated) {
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		} else {
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		}

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL13.GL_CLAMP_TO_BORDER);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL13.GL_CLAMP_TO_BORDER);

		if (useOneColor) {
			int bufferSize = width * height;

			ByteBuffer imageData = null;
			if (imageData == null || imageData.capacity() < bufferSize) {
				bufferSize = ceilPowerOfTwo(bufferSize);
				imageData = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
			}
			imageData.clear();

			for (int y = 0; y < height; ++y) {
				for (int x = 0; x < width; ++x) {
					int rgb = bufferedImage.getRGB(x, y);
					imageData.put((byte) ((rgb >> 24) & 255));
				}
			}
			imageData.position(0).limit(bufferSize);
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RED, width, height, 0, GL11.GL_RED, GL11.GL_UNSIGNED_BYTE, imageData);
			GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
			GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
			imageData.clear();
		} else {
			int bufferSize = width * height * Integer.BYTES;

			IntBuffer imageData = null;
			bufferSize = ceilPowerOfTwo(bufferSize);
			imageData = ByteBuffer.allocateDirect(bufferSize * Integer.BYTES).order(ByteOrder.nativeOrder()).asIntBuffer();
			imageData.clear();

			for (int y = 0; y < height; ++y) {
				for (int x = 0; x < width; ++x) {
					int rgb = bufferedImage.getRGB(x, y);
					imageData.put(rgb);
				}
			}
			imageData.position(0).limit(bufferSize);
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA8, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, imageData);
			imageData.clear();
		}

		bufferedImage.getGraphics().dispose();
	}

	// https://stackoverflow.com/questions/466204/rounding-up-to-next-power-of-2
	public int ceilPowerOfTwo(int value) {
		value--;
		value |= value >> 1;
		value |= value >> 2;
		value |= value >> 4;
		value |= value >> 8;
		value |= value >> 16;
		value++;

		return value;
	}

	protected static class CharData {
		public int width = 0;
		public int height = 0;
		public float charWidth = 0;
		public float u0 = 0;
		public float v0 = 0;
		public float u1 = 0;
		public float v1 = 0;
	}
}