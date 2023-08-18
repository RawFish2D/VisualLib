package ua.rawfish2d.visuallib.utils;

import org.lwjgl.opengl.GL11;

public class GLSM {

	public static GLSM instance = new GLSM();

	private GLSM() {
	}

	public void glEnableDepthTest() {
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}

	public void glDisableDepthTest() {
		GL11.glDisable(GL11.GL_DEPTH_TEST);
	}

	public void glEnableTexture2D() {
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	public void glDisableTexture2D() {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
	}

	public void glEnableBlend() {
		GL11.glEnable(GL11.GL_BLEND);
	}

	public void glDisableBlend() {
		GL11.glDisable(GL11.GL_BLEND);
	}

	public void glBlendFunc(int sfactor, int dfactor) {
		GL11.glBlendFunc(sfactor, dfactor);
	}

	public void glDisableAlpha() {
		GL11.glDisable(GL11.GL_ALPHA_TEST);
	}

	public void glEnableAlpha() {
		GL11.glEnable(GL11.GL_ALPHA_TEST);
	}

	public void glDepthFunc(int glfunc) {
		GL11.glDepthFunc(glfunc);
	}

	public void glDepthMask(boolean value) {
		GL11.glDepthMask(value);
	}

	public void glEnableLineSmooth() {
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
	}

	public void glDisableLineSmooth() {
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
	}

	public void glPolygonMode(int face, int mode) {
		GL11.glPolygonMode(face, mode);
	}

	public void glLineWidth(float width) {
		GL11.glLineWidth(width);
	}

	public void glEnableStencil() {
		GL11.glEnable(GL11.GL_STENCIL);
	}

	public void glDisableStencil() {
		GL11.glDisable(GL11.GL_STENCIL);
	}

	public void glEnableCullFace() {
		GL11.glEnable(GL11.GL_CULL_FACE);
	}

	public void glDisableCullFace() {
		GL11.glDisable(GL11.GL_CULL_FACE);
	}

	public void glCullFace(int mode) {
		GL11.glCullFace(mode);
	}
}