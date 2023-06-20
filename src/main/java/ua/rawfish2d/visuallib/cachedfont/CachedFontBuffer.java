package ua.rawfish2d.visuallib.cachedfont;

import lombok.Getter;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import ua.rawfish2d.visuallib.font.FontRenderer;
import ua.rawfish2d.visuallib.framebuffer.FrameBuffer;
import ua.rawfish2d.visuallib.utils.GLSM;
import ua.rawfish2d.visuallib.utils.RenderContext;
import ua.rawfish2d.visuallib.vertexbuffer.ShaderProgram;
import ua.rawfish2d.visuallib.vertexbuffer.VertexBuffer;
import ua.rawfish2d.visuallib.window.GWindow;

import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.LinkedHashSet;

import static org.lwjgl.opengl.GL11.GL_FLOAT;

public class CachedFontBuffer {
	@Getter
	private final FrameBuffer fbo;
	private final ShaderProgram shaderProgram;
	@Getter
	private final VertexBuffer vbo;
	@Getter
	private final LinkedHashSet<CachedFont> cachedList = new LinkedHashSet<>();
	private final GLSM glsm = GLSM.instance;
	private final GWindow window;
	@Getter
	private final FontRenderer fontRenderer;

	public CachedFontBuffer(GWindow window, FontRenderer fontRenderer) {
		this.window = window;
		this.fontRenderer = fontRenderer;
		this.fbo = new FrameBuffer(window.getDisplayWidth(), window.getDisplayHeight(), true, false);

		shaderProgram = new ShaderProgram();
		InputStream vertStream = getInputStream("shaders/font/cachedFont.vert");
		InputStream fragStream = getInputStream("shaders/font/cachedFont.frag");
		shaderProgram.loadShaders(vertStream, fragStream);

		vbo = new VertexBuffer();
		vbo.setShader(shaderProgram);
		vbo.setDrawType(GL11.GL_QUADS);
		final int maxObjects = 4096;
		vbo.setMaxObjectCount(maxObjects);

		final VertexBuffer.VertexAttribute posAttrib = vbo.addVertexAttribute("aPos", 2, Float.BYTES, false, GL_FLOAT);
		final VertexBuffer.VertexAttribute texCoordAttrib = vbo.addVertexAttribute("aTexCoord", 2, Float.BYTES, false, GL_FLOAT);
		vbo.addBuffer(posAttrib);
		vbo.addBuffer(texCoordAttrib);
		vbo.initBuffers();
		vbo.clearBuffers();

		final IntBuffer indexBuffer = vbo.getIndexBuffer();
		for (int a = 0; a < maxObjects * vbo.verticesPerObject; a += vbo.verticesPerObject) {
			indexBuffer.put(a + 0);
			indexBuffer.put(a + 1);
			indexBuffer.put(a + 2);

			indexBuffer.put(a + 2);
			indexBuffer.put(a + 3);
			indexBuffer.put(a + 0);
		}
		indexBuffer.flip();
		vbo.uploadBuffers();
		vbo.uploadIndexBuffer();
	}

	public void addText(String text, float x, float y) {
		cachedList.add(new CachedFont(fontRenderer, text, x, y));
	}

	public void bake(RenderContext renderContext) {
		glsm.glEnableTexture2D();
		glsm.glEnableBlend();
		glsm.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		glsm.glEnableAlpha();

		fontRenderer.setScale(1f);
		fontRenderer.setShadowOffset(1f);
		fontRenderer.setRenderContext(renderContext);
		fbo.bindFramebuffer();
		fbo.clearFramebuffer(renderContext.getClearcolor());

		final VertexBuffer vbo = renderContext.getRenderBuffer().getVbo();
		final ShaderProgram shader = renderContext.getShaderProgram();
		vbo.clearBuffers();

		float x = 2f;
		float y = 2f;
		for (CachedFont cachedFont : cachedList) {
			cachedFont.renderAt(fontRenderer, x, y);
			y += cachedFont.getStringHeight();
		}
		vbo.canUpload();
		vbo.uploadBuffers();

		GL20.glUseProgram(shader.getProgram());
		glsm.glBindTexture(fontRenderer.getFontTextureID());
		shader.setUniform1f("u_scale", 1f);
		shader.setUniform2f("u_resolution", window.getDisplayWidth(), window.getDisplayHeight());
		vbo.draw();
		GL20.glUseProgram(0);

		fbo.unbindFramebuffer(renderContext.getViewport());
	}

	public void clearBuffer() {
		vbo.clearBuffers();
	}

	public void finishBuffer() {
		vbo.canUpload();
		vbo.uploadBuffers();
	}

	public void render() {
		final int shaderID = shaderProgram.getProgram();
		GL20.glUseProgram(shaderID);
		glsm.glBindTexture(fbo.getTextureID());
		shaderProgram.setUniform1f("u_scale", 1f);
		shaderProgram.setUniform2f("u_resolution", window.getDisplayWidth(), window.getDisplayHeight());
		vbo.draw();
		GL20.glUseProgram(0);
	}

	public void onRender() {
		glsm.glEnableTexture2D();
		glsm.glEnableBlend();
		glsm.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		glsm.glEnableAlpha();
		glsm.glBindTexture(fbo.getTextureID());

		cachedList.forEach(cache -> cache.onRender(this));

		glsm.glBindTexture(0);
		glsm.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
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
}
