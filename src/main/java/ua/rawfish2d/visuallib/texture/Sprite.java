package ua.rawfish2d.visuallib.texture;

import lombok.Getter;

public class Sprite {
	@Getter
	private final float u0;
	@Getter
	private final float u1;
	@Getter
	private final float v0;
	@Getter
	private final float v1;
	@Getter
	private final int duration;
	@Getter
	private final int posX;
	@Getter
	private final int posY;
	@Getter
	private final int width;
	@Getter
	private final int height;
	@Getter
	private final int textureIndex;

	public Sprite(int posX, int posY, int width, int height, int textureWidth, int textureHeight, int duration, int textureIndex) {
		this.posX = posX;
		this.posY = posY;
		int fakeY = textureHeight - posY - height;
		final float stepX = 1f / (float) textureWidth;
		final float stepY = 1f / (float) textureHeight;
		this.u0 = stepX * (float) posX;
		this.u1 = stepX * (float) (posX + width);
		this.v0 = stepY * (float) fakeY;
		this.v1 = stepY * (float) (fakeY + height);
		this.duration = duration;
		this.width = width;
		this.height = height;
		this.textureIndex = textureIndex;
	}
}