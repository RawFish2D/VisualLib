package ua.rawfish2d.visuallib.utils;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.lwjgl.opengl.GL11;
import ua.rawfish2d.visuallib.font.FontRenderer;
import ua.rawfish2d.visuallib.vertexbuffer.ShaderProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

@NoArgsConstructor
public class RenderContext {
	@Getter
	@Setter
	private RenderBuffer renderBuffer;
	@Getter
	@Setter
	private FontRenderer fontRenderer;
	@Getter
	@Setter
	private ShaderProgram shaderProgram;
	@Getter
	private int screenWidth;
	@Getter
	private int screenHeight;
	@Getter
	private final Viewport viewport = new Viewport();
	@Getter
	private final Clearcolor clearcolor = new Clearcolor();

	public void setViewport(int x, int y, int width, int height) {
		this.viewport.setViewport(x, y, width, height);
	}

	public void setViewport() {
		this.viewport.setViewport();
	}

	public void setScreenSize(int screenWidth, int screenHeight) {
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
	}

	public void setClearcolor(int color) {
		clearcolor.setClearColor(color);
	}

	public void setClearcolor() {
		clearcolor.setClearcolor();
	}

	@NoArgsConstructor
	public static class Viewport {
		int x;
		int y;
		int width;
		int height;

		public void setViewport(int x, int y, int width, int height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}

		public void setViewport() {
			GL11.glViewport(x, y, width, height);
		}

		public void getAndSetFromCurrent() {
			final IntBuffer oldViewport = ByteBuffer.allocateDirect(16 * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
			GL11.glGetIntegerv(GL11.GL_VIEWPORT, oldViewport);
			setViewport(oldViewport.get(0), oldViewport.get(1), oldViewport.get(2), oldViewport.get(3));
		}

		public void setViewport(Viewport viewport) {
			this.x = viewport.x;
			this.y = viewport.y;
			this.width = viewport.width;
			this.height = viewport.height;
		}
	}

	@Data
	@NoArgsConstructor
	public static class Clearcolor {
		protected float r;
		protected float g;
		protected float b;
		protected float a;

		public void setClearColor(int color) {
			r = ((color >> 16) & 0xff) / 255.0f;
			g = ((color >> 8) & 0xff) / 255.0f;
			b = ((color) & 0xff) / 255.0f;
			a = ((color >> 24) & 0xff) / 255.0f;
		}

		public void setClearcolor() {
			GL11.glClearColor(r, g, b, a);
		}
	}
}
