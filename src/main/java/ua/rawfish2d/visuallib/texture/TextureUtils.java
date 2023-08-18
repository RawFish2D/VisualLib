package ua.rawfish2d.visuallib.texture;

import de.matthiasmann.twl.utils.PNGDecoder;
import ua.rawfish2d.visuallib.loader.TextureData;
import ua.rawfish2d.visuallib.loader.TextureLoader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class TextureUtils {

	public static int loadTexture(String path) {
		final TextureData textureData = TextureLoader.getImageData(path, PNGDecoder.Format.RGBA);
		return createTexture(textureData);
	}

	public static int loadTexture(String path, int filteringMin, int filteringMax) {
		final TextureData textureData = TextureLoader.getImageData(path, PNGDecoder.Format.RGBA);
		return createTexture(textureData, filteringMin, filteringMax);
	}

	public static int loadTexture(String path, int wrapS, int wrapT, int filteringMin, int filteringMax) {
		final TextureData textureData = TextureLoader.getImageData(path, PNGDecoder.Format.RGBA);
		return createTexture(textureData, wrapS, wrapT, filteringMin, filteringMax);
	}

	public static int loadTexture(String path, int wrapS, int wrapT, int filteringMin, int filteringMax, PNGDecoder.Format format) {
		final TextureData textureData = TextureLoader.getImageData(path, format);
		return createTexture(textureData, wrapS, wrapT, filteringMin, filteringMax);
	}

	public static int createTexture(final TextureData textureData) {
		return createTexture(textureData.width, textureData.height, GL_REPEAT, GL_REPEAT, GL_NEAREST, GL_NEAREST, textureData.data);
	}

	public static int createTexture(final TextureData textureData, int filteringMin, int filteringMax) {
		return createTexture(textureData.width, textureData.height, GL_REPEAT, GL_REPEAT, filteringMin, filteringMax, textureData.data);
	}

	public static int createTexture(final TextureData textureData, int wrapS, int wrapT, int filteringMin, int filteringMax) {
		return createTexture(textureData.width, textureData.height, wrapS, wrapT, filteringMin, filteringMax, textureData.data);
	}

	public static int createTexture(BufferedImage bufferedImage) {
		return createTexture(bufferedImage, GL_REPEAT, GL_REPEAT, GL_NEAREST, GL_NEAREST);
	}

	public static int createTexture(BufferedImage bufferedImage, int filteringMin, int filteringMax) {
		return createTexture(bufferedImage, GL_REPEAT, GL_REPEAT, filteringMin, filteringMax);
	}

	public static int createTexture(BufferedImage bufferedImage, int wrapS, int wrapT, int filteringMin, int filteringMax) {
		final ByteBuffer buffer = ByteBuffer.allocateDirect(bufferedImage.getWidth() * bufferedImage.getHeight() * 4);

		for (int y = 0; y < bufferedImage.getHeight(); ++y) {
			for (int x = 0; x < bufferedImage.getWidth(); ++x) {
				int pixel = bufferedImage.getRGB(x, y);

				buffer.put((byte) ((pixel >> 16) & 0xFF));
				buffer.put((byte) ((pixel >> 8) & 0xFF));
				buffer.put((byte) (pixel & 0xFF));
				buffer.put((byte) ((pixel >> 24) & 0xFF));
			}
		}
		buffer.flip();
		int textureID = createTexture(bufferedImage.getWidth(), bufferedImage.getHeight(), wrapS, wrapT, filteringMin, filteringMax, buffer);
		buffer.clear();

		return textureID;
	}

	public static int createTexture(int width, int height) {
		return createTexture(width, height, GL_REPEAT, GL_REPEAT, GL_NEAREST, GL_NEAREST);
	}

	public static int createTexture(int width, int height, int filteringMin, int filteringMax) {
		return createTexture(width, height, GL_REPEAT, GL_REPEAT, filteringMin, filteringMax, null);
	}

	public static int createTexture(int width, int height, int wrapS, int wrapT, int filteringMin, int filteringMax) {
		return createTexture(width, height, wrapS, wrapT, filteringMin, filteringMax, null);
	}

	public static int createTexture(int width, int height, ByteBuffer data) {
		return createTexture(width, height, GL_REPEAT, GL_REPEAT, GL_NEAREST, GL_NEAREST, data);
	}

	public static int createTexture(int width, int height, int filteringMin, int filteringMax, ByteBuffer data) {
		return createTexture(width, height, GL_REPEAT, GL_REPEAT, filteringMin, filteringMax, data);
	}

	public static int createTexture(int width, int height, int wrapS, int wrapT, int filteringMin, int filteringMax, ByteBuffer data) {
		final int textureID = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, textureID);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapS);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filteringMin);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filteringMax);

		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, data);

		glBindTexture(GL_TEXTURE_2D, 0);
		return textureID;
	}

	public static void saveFrameBuffer(String fileName, int fboID, int fboWidth, int fboHeight) {
		saveFrameBuffer(fileName, fboID, fboWidth, fboHeight, GL_COLOR_ATTACHMENT0);
	}

	public static void saveFrameBuffer(String fileName, int fboID, int fboWidth, int fboHeight, int colorAttachment) {
		final IntBuffer oldViewport = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
		glGetIntegerv(GL_VIEWPORT, oldViewport);
		final int viewPortX = oldViewport.get(0);
		final int viewPortY = oldViewport.get(1);
		final int viewPortW = oldViewport.get(2);
		final int viewPortH = oldViewport.get(3);
		glViewport(0, 0, fboWidth, fboHeight);

		System.out.println("Creating buffer...");
		final int bufferSize = (fboWidth * fboHeight * 4);
		ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);
		glBindFramebuffer(GL_READ_FRAMEBUFFER, fboID);
		glReadBuffer(colorAttachment);
		System.out.println("Reading pixels...");
		glReadPixels(0, 0, fboWidth, fboHeight, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
		glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);

		final ExecutorService es = Executors.newSingleThreadExecutor();
		es.execute(() -> {
			System.out.println("Creating buffered image...");
			BufferedImage image = new BufferedImage(fboWidth, fboHeight, BufferedImage.TYPE_INT_ARGB);
			System.out.println("Setting RGB pixels...");
			for (int y = 0; y < fboHeight; ++y) {
				for (int x = 0; x < fboWidth; ++x) {
					int pixel = buffer.getInt();
					int blue = (pixel & 0xFF000000) >> 8;
					int green = (pixel & 0x00FF0000) >> 8;
					int red = (pixel & 0x0000FF00) >> 8;
					int alpha = (pixel & 0x000000FF) << 24;
					int newPixel = blue | green | red | alpha;
					image.setRGB(x, fboHeight - y - 1, newPixel);
				}
			}
			buffer.clear();

			File outputfile = new File(fileName);
			try {
				System.out.println("Saving file " + fileName);
				ImageIO.write(image, "png", outputfile);
				image = null;
				System.out.println("File saved! " + fileName);
			} catch (IOException e) {
				e.printStackTrace();
			}
			es.shutdown();
		});
		glBindTexture(GL_TEXTURE_2D, 0);

		// setting old viewport
		glViewport(viewPortX, viewPortY, viewPortW, viewPortH);
	}

	public static int createTextureArray(int width, int height, int layerCount) {
		return createTextureArray(width, height, layerCount, GL_REPEAT, GL_REPEAT, GL_NEAREST, GL_NEAREST);
	}

	public static int createTextureArray(int width, int height, int layerCount, int filteringMin, int filteringMax) {
		return createTextureArray(width, height, layerCount, GL_REPEAT, GL_REPEAT, filteringMin, filteringMax);
	}

	public static int createTextureArray(int width, int height, int layerCount, int wrapS, int wrapT, int filteringMin, int filteringMax) {
		final int textureID = glGenTextures();
		glBindTexture(GL_TEXTURE_2D_ARRAY, textureID);

		glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, wrapS);
		glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, wrapT);
		glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, filteringMin);
		glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, filteringMax);

		glTexImage3D(GL_TEXTURE_2D_ARRAY, 0, GL_RGBA, width, height, layerCount, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
		glBindTexture(GL_TEXTURE_2D_ARRAY, 0);
		// final int mipmapLevels = 1;
		// glTexStorage3D(GL_TEXTURE_2D_ARRAY, mipmapLevels, GL_RGBA8, width, height, layerCount);
		return textureID;
	}
}
