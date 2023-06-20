package ua.rawfish2d.visuallib.utils;

import org.lwjgl.opengl.GL11;

public class GLSM {
	protected int boundTexture = 0;

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

	public int glGenTextures() {
		return GL11.glGenTextures();
	}

	public void glDeleteTextures(int id) {
		GL11.glDeleteTextures(id);
	}

	public void glBindTexture(int textureID) {
		if (textureID == boundTexture)
			return;

		boundTexture = textureID;
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
	}

	public void glBindTexture(int target, int textureID) {
		if (textureID == boundTexture)
			return;

		boundTexture = textureID;
		GL11.glBindTexture(target, textureID);
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

	@Deprecated
	public void glEnableScissor() {
	}

	@Deprecated
	public void glScissor(float x, float y, float sw, float sh) {
	}

	@Deprecated
	public void glDisableScissor() {
	}

	@Deprecated
	public void glColor(int c) {
	}

	@Deprecated
	public void glColor(float v, float v1, float v2, float v3) {
	}

	@Deprecated
	public void glAlphaFunc(int glNotequal, float v) {
	}

	@Deprecated
	public void glShadeModel(int glSmooth) {
	}

	@Deprecated
	public void glScale(float scale, float scale1, float v) {
	}
}