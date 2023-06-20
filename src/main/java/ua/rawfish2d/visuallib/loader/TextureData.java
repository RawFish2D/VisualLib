package ua.rawfish2d.visuallib.loader;

import java.nio.ByteBuffer;

public class TextureData {
	public final String location;
	public final int width;
	public final int height;
	public final ByteBuffer data;

	public TextureData(String location, int width, int height, ByteBuffer data) {
		this.location = location;
		this.width = width;
		this.height = height;
		this.data = data;
	}
}