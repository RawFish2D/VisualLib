package ua.rawfish2d.visuallib.loader;

import de.matthiasmann.twl.utils.PNGDecoder;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TextureLoader {
	public static TextureData getImageData(String filename, PNGDecoder.Format textureFormat) {
		ByteBuffer buf = null;
		int textureWidth = 0;
		int textureHeight = 0;
		TextureData texData = null;

		try {
			InputStream in = Files.newInputStream(Paths.get(filename));
			PNGDecoder decoder = new PNGDecoder(in);

			textureWidth = decoder.getWidth();
			textureHeight = decoder.getHeight();

			final int components = textureFormat.getNumComponents();

			buf = ByteBuffer.allocateDirect(textureWidth * textureHeight * components);
			decoder.decode(buf, textureWidth * components, textureFormat);
			buf.flip();
			in.close();
			texData = new TextureData(filename, textureWidth, textureHeight, buf);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return texData;
	}
}
