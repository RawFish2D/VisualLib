package ua.rawfish2d.visuallib.cachedgui.utils;

import lombok.Setter;
import org.joml.Vector4f;
import ua.rawfish2d.visuallib.utils.RenderBuffer;
import ua.rawfish2d.visuallib.utils.RenderContext;

public class RenderUtils {
	@Setter
	private static int vertexBufferIndex = 0;
	@Setter
	private static int colorBufferIndex = 1;
	@Setter
	private static int textureBufferIndex = 2;
	@Setter
	private static Vector4f geomUV = new Vector4f(0f, 1f, 0f, 1f);

	public static void drawQuad(RenderContext renderContext, float x, float y, float x2, float y2, int color) {
		RenderBuffer renderBuffer = renderContext.getRenderBuffer();
		renderBuffer.addVertex(x, y, color, geomUV.x, geomUV.z);
		renderBuffer.addVertex(x, y2, color, geomUV.x, geomUV.w);
		renderBuffer.addVertex(x2, y2, color, geomUV.y, geomUV.w);
		renderBuffer.addVertex(x2, y, color, geomUV.y, geomUV.z);
		renderBuffer.addIndexCount(6);
	}

	public static void drawBorder(RenderContext renderContext, float x, float y, float x2, float y2, float borderWidth, int color) {
		drawQuad(renderContext, x, y, x2, y + borderWidth, color); // top
		drawQuad(renderContext, x, y2 - borderWidth, x2, y2, color); // bottom

		drawQuad(renderContext, x, y, x + borderWidth, y2, color); // left
		drawQuad(renderContext, x2 - borderWidth, y, x2, y2, color); // right
	}

	public static void drawBorderedQuad(RenderContext renderContext, float x, float y, float x2, float y2, float borderWidth, int color, int borderColor) {
		drawQuad(renderContext, x, y, x2, y2, color);
		drawBorder(renderContext, x, y, x2, y2, borderWidth, borderColor);
	}
}
