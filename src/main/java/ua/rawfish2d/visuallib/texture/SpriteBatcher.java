package ua.rawfish2d.visuallib.texture;

import de.matthiasmann.twl.utils.PNGDecoder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.joml.Vector2i;
import ua.rawfish2d.visuallib.framebuffer.FrameBuffer;
import ua.rawfish2d.visuallib.loader.TextureData;
import ua.rawfish2d.visuallib.loader.TextureLoader;
import ua.rawfish2d.visuallib.utils.GLSM;
import ua.rawfish2d.visuallib.utils.RenderBuffer;
import ua.rawfish2d.visuallib.utils.RenderContext;
import ua.rawfish2d.visuallib.vertexbuffer.ShaderProgram;
import ua.rawfish2d.visuallib.vertexbuffer.VertexBuffer;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.LinkedList;

import static org.lwjgl.opengl.GL11.GL_MAX_TEXTURE_SIZE;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_REPEAT;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glGetInteger;
import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL11C.GL_QUADS;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11C.GL_VIEWPORT;
import static org.lwjgl.opengl.GL11C.glGetIntegerv;
import static org.lwjgl.opengl.GL20C.glUseProgram;

public class SpriteBatcher {
	@Getter
	private final int textureID;
	private final LinkedList<LinkedList<Sprite>> spriteGroups = new LinkedList<>();
	private final LinkedList<Sprite> allSprites = new LinkedList<>();
	private final FrameBuffer frameBuffer;
	private final RenderContext renderContext;
	private final int maxSpriteSize;

	public SpriteBatcher(int width, int height, int maxSpriteSize) {
		this(width, height, maxSpriteSize, GL_REPEAT, GL_REPEAT, GL_NEAREST, GL_NEAREST);
	}

	public SpriteBatcher(int width, int height, int maxSpriteSize, int filteringMin, int filteringMax) {
		this(width, height, maxSpriteSize, GL_REPEAT, GL_REPEAT, filteringMin, filteringMax);
	}

	public SpriteBatcher(int width, int height, int maxSpriteSize, int wrapS, int wrapT, int filteringMin, int filteringMax) {
		this.maxSpriteSize = maxSpriteSize;
		int maxSize = glGetInteger(GL_MAX_TEXTURE_SIZE);
		System.out.println("max texture size:" + maxSize);

		this.frameBuffer = new FrameBuffer(width, height, wrapS, wrapT, filteringMin, filteringMax, false);
		frameBuffer.setClearColor(0xFF000000);
		frameBuffer.setViewport(0, 0, width, height);
		this.textureID = frameBuffer.getTextureID();

		renderContext = setupTextureContext(1);
	}

	public void setClearColor(int clearColor) {
		frameBuffer.setClearColor(clearColor);
	}

	public void addSprite(BufferedImage image, int duration, LinkedList<Sprite> spriteGroup) {
		final int textureImage = TextureUtils.createTexture(image);
		addSprite(textureImage, image.getWidth(), image.getHeight(), duration, spriteGroup);
	}

	public void addSprite(int textureID, int width, int height, int duration, LinkedList<Sprite> spriteGroup) {
		final ShaderProgram shader = renderContext.getShaderProgram();
		final RenderBuffer renderBuffer = renderContext.getRenderBuffer();
		final VertexBuffer vbo = renderBuffer.getVbo();

		final GLSM glsm = GLSM.instance;
		glsm.glDisableDepthTest();
		glsm.glEnableAlpha();
		glsm.glEnableBlend();
		glsm.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glsm.glEnableCullFace();

		final IntBuffer oldViewport = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
		glGetIntegerv(GL_VIEWPORT, oldViewport);
		final int viewPortX = oldViewport.get(0);
		final int viewPortY = oldViewport.get(1);
		final int viewPortW = oldViewport.get(2);
		final int viewPortH = oldViewport.get(3);

		frameBuffer.bindFramebuffer();
		renderBuffer.clear();

		final Vector2i size = scaleSpriteSize(width, height);
		final int spriteWidth = size.x;
		final int spriteHeight = size.y;

		final Vector2i spritePos = findNextSpritePosFast(spriteWidth, spriteHeight);
		final int x = spritePos.x;
		final int y = spritePos.y;

		final Sprite sprite = new Sprite(x, y, spriteWidth, spriteHeight, frameBuffer.getWidth(), frameBuffer.getHeight(), duration, 0);
		spriteGroup.add(sprite);
		allSprites.add(sprite);

		final int color = 0xFFFFFFFF;
		final float u0 = 0f;
		final float u1 = 1f;
		final float v0 = 0f;
		final float v1 = 1f;

		renderBuffer.addVertex(x, y, color, u0, v0);
		renderBuffer.addVertex(x, y + spriteHeight, color, u0, v1);
		renderBuffer.addVertex(x + spriteWidth, y + spriteHeight, color, u1, v1);
		renderBuffer.addVertex(x + spriteWidth, y, color, u1, v0);
		renderBuffer.addIndexCount(6);

		renderBuffer.canUpload();
		renderBuffer.uploadBuffers();

		glUseProgram(shader.getProgram());
		shader.setUniform1f("u_scale", 1f);
		shader.setUniform2f("u_resolution", frameBuffer.getWidth(), frameBuffer.getHeight());
		shader.setUniform2f("u_translate", 0f, 0f);
		glBindTexture(GL_TEXTURE_2D, textureID);
		vbo.draw();
		glUseProgram(0);

		frameBuffer.unbindFramebuffer(viewPortX, viewPortY, viewPortW, viewPortH);
		oldViewport.clear();
		glDeleteTextures(textureID);
		glBindTexture(GL_TEXTURE_2D, 0);
	}

	private Vector2i findNextSpritePosFast(int spriteWidth, int spriteHeight) {
		final Vector2i pos = new Vector2i(0, 0);
		if (allSprites.isEmpty()) {
			System.out.printf("EMPTY OK x%d y%d\n", pos.x, pos.y);
			return pos;
		} else {
			for (Sprite refSprite : allSprites) {
				int x = 0;
				int y = 0;
				boolean overlaping = false;

				int otherMinX = refSprite.getPosX() + refSprite.getWidth();
				int otherMinY = refSprite.getPosY();
				int otherMaxX = otherMinX + spriteWidth;
				int otherMaxY = otherMinY + spriteHeight;
				for (Sprite sprite : allSprites) {
					// right check
					int thisMinX = sprite.getPosX();
					int thisMinY = sprite.getPosY();
					int thisMaxX = thisMinX + sprite.getWidth();
					int thisMaxY = thisMinY + sprite.getHeight();

					if (otherMaxX > frameBuffer.getWidth()) {
						overlaping = true;
						break;
					}

					// check
					overlaping = thisMinX < otherMaxX && thisMaxX > otherMinX && thisMinY < otherMaxY && thisMaxY > otherMinY;
					if (overlaping) {
						break;
					}
					x = otherMinX;
					y = otherMinY;
				}
				if (!overlaping) {
					pos.set(x, y);
					return pos;
				}
			}
			for (Sprite refSprite : allSprites) {
				int x = 0;
				int y = 0;
				boolean overlaping = false;

				int otherMinX = refSprite.getPosX();
				int otherMinY = refSprite.getPosY() + refSprite.getHeight();
				int otherMaxX = otherMinX + spriteWidth;
				int otherMaxY = otherMinY + spriteHeight;
				for (Sprite sprite : allSprites) {
					// bottom check
					int thisMinX = sprite.getPosX();
					int thisMinY = sprite.getPosY();
					int thisMaxX = thisMinX + sprite.getWidth();
					int thisMaxY = thisMinY + sprite.getHeight();

					if (otherMaxY > frameBuffer.getHeight()) {
						overlaping = true;
						break;
					}

					// check
					overlaping = thisMinX < otherMaxX && thisMaxX > otherMinX && thisMinY < otherMaxY && thisMaxY > otherMinY;
					if (overlaping) {
						break;
					}
					x = otherMinX;
					y = otherMinY;
				}
				if (!overlaping) {
					pos.set(x, y);
					return pos;
				}
			}
		}
		System.out.printf("VERY BAD x%d y%d\n", pos.x, pos.y);
		return pos;
	}

	private Vector2i scaleSpriteSize(int width, int height) {
		Vector2i size = new Vector2i(width, height);
		int max = Math.max(size.x, size.y);
		if (max > maxSpriteSize) {
			float scaleW = (float) maxSpriteSize / max;
			float scaleH = (float) maxSpriteSize / max;
			size.x = (int) (size.x * scaleW);
			size.y = (int) (size.y * scaleH);
		}
		return size;
	}

	public void loadFromFile(String filePath) {
		final LinkedList<Sprite> spriteGroup = new LinkedList<>();
		final TextureData textureData = TextureLoader.getImageData(filePath, PNGDecoder.Format.RGBA);
		final int texture = TextureUtils.createTexture(textureData);
		addSprite(texture, textureData.width, textureData.height, 0, spriteGroup);
		System.out.printf("Sprite loaded %s\n", filePath);
		spriteGroups.add(spriteGroup);
	}

	public void saveDebugFramebuffer(String filename) {
		TextureUtils.saveFrameBuffer(filename, frameBuffer.getFrameBufferID(), frameBuffer.getWidth(), frameBuffer.getHeight());
	}

	private RenderContext setupTextureContext(int objectCount) {
		final ShaderProgram shaderProgram = new ShaderProgram();
		final InputStream vertStream = getInputStream("shaders/basic/texture2d.vert");
		final InputStream fragStream = getInputStream("shaders/basic/texture2d.frag");
		shaderProgram.loadShaders(vertStream, fragStream);

		final VertexBuffer vbo = new VertexBuffer();
		vbo.setShader(shaderProgram);
		vbo.setDrawType(GL_QUADS);
		vbo.setMaxObjectCount(objectCount);

		final VertexBuffer.VertexAttribute posAttrib = vbo.addVertexAttribute("aPos", 2, Float.BYTES, false, GL_FLOAT);
		final VertexBuffer.VertexAttribute colorAttrib = vbo.addVertexAttribute("aColor", 4, 1, true, GL_UNSIGNED_BYTE);
		final VertexBuffer.VertexAttribute texCoordAttrib = vbo.addVertexAttribute("aTexCoord", 2, Float.BYTES, false, GL_FLOAT);
		vbo.addBuffer(posAttrib);
		vbo.addBuffer(colorAttrib);
		vbo.addBuffer(texCoordAttrib);
		vbo.initBuffers();
		vbo.clearBuffers();

		final IntBuffer indexBuffer = vbo.getIndexBuffer();
		// this loop is kinda confusing I know, but I promise it works as intended
		for (int a = 0; a < objectCount * vbo.verticesPerObject; a += vbo.verticesPerObject) {
			indexBuffer.put(a);
			indexBuffer.put(a + 1);
			indexBuffer.put(a + 2);
			indexBuffer.put(a + 2);
			indexBuffer.put(a + 3);
			indexBuffer.put(a);
		}
		indexBuffer.flip();
		vbo.uploadBuffers();
		vbo.uploadIndexBuffer();
		vbo.clearBuffers();

		final RenderBuffer renderBuffer = new RenderBuffer();
		renderBuffer.setBuffers(vbo, 0, 1, 2);
		final RenderContext renderContext = new RenderContext();
		renderContext.setRenderBuffer(renderBuffer);
		renderContext.setShaderProgram(shaderProgram);

		return renderContext;
	}

	public void delete() {
		renderContext.getRenderBuffer().deleteBuffers();
		frameBuffer.delete();
	}

	public void cleanup() {
		renderContext.getRenderBuffer().deleteBuffers();
		frameBuffer.deleteFrameBuffer();
	}

	public LinkedList<Sprite> getSpriteGroup(int index) {
		return spriteGroups.get(index);
	}

	public int spriteGroupCount() {
		return spriteGroups.size();
	}

	private InputStream getInputStream(String fileName) {
		ClassLoader classLoader = GLSM.class.getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(fileName);

		if (inputStream == null) {
			throw new IllegalArgumentException("File not found: " + fileName);
		} else {
			return inputStream;
		}
	}

	@Data
	@AllArgsConstructor
	protected class Gif implements Comparable<Gif> {
		public String name;
		public int width;
		public int height;

		@Override
		public int compareTo(Gif o) {
			return (width * height) - (o.width * o.height);
		}
	}
}
