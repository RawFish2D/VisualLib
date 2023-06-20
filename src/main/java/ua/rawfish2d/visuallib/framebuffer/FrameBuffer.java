package ua.rawfish2d.visuallib.framebuffer;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;
import ua.rawfish2d.visuallib.utils.GLSM;
import ua.rawfish2d.visuallib.utils.RenderContext;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class FrameBuffer {
	protected final int width;
	protected final int height;
	protected int frameBufferID = 0;
	protected int textureID = 0;
	protected boolean useDepth = false;
	protected int renderBufferID = 0;
	protected boolean complete = false;
	protected final RenderContext.Clearcolor clearcolor = new RenderContext.Clearcolor();
	protected final RenderContext.Viewport viewport = new RenderContext.Viewport();

	public FrameBuffer(int width, int height, boolean pixelated, boolean useDepth) {
		this.useDepth = useDepth;
		this.width = width;
		this.height = height;

		// check if GL_EXT_framebuffer_object can be used on this system
		if (!GL.getCapabilities().GL_EXT_framebuffer_object) {
			System.out.println("GL_EXT_framebuffer_object is not supported!");
			return;
		} else {
			delete();

			frameBufferID = glGenFramebuffers();
			glBindFramebuffer(GL_FRAMEBUFFER, frameBufferID);

			if (this.useDepth) {
				renderBufferID = GL30.glGenRenderbuffers();
			}

			// create texture to render to
			textureID = GLSM.instance.glGenTextures();
			GLSM.instance.glBindTexture(textureID);

			glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
			glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

			if (pixelated) {
				// pixelated
				glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
				glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
			} else {
				// blured
				glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
				glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			}

			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, this.width, this.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);
			GLSM.instance.glBindTexture(0);

			// attach texture to the fbo
			glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureID, 0);

			if (useDepth) {
				// create and attach depth buffer to fbo
				glBindRenderbuffer(GL_RENDERBUFFER, renderBufferID);
				glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT32F, width, height);// 33190 GL_DEPTH_COMPONENT24
				glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, renderBufferID);
			}

			int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
			if (status == GL_FRAMEBUFFER_COMPLETE) {
				complete = true;
				//System.out.println("FrameBuffer created successfully.");
			} else {
				System.out.println("An error occurred while creating the FrameBuffer: " + status);
			}

			clearFramebuffer(clearcolor);
			glBindFramebuffer(GL_FRAMEBUFFER, 0);
		}
	}

	public void setClearColor(int color) {
		clearcolor.setClearColor(color);
	}

	public void setViewport(int x, int y, int width, int height) {
		viewport.setViewport(x, y, width, height);
	}

	public void setViewport(RenderContext.Viewport otherViewport) {
		this.viewport.setViewport(otherViewport);
	}

	public boolean isComplete() {
		return complete;
	}

	public void clearFramebuffer(RenderContext.Clearcolor otherClearcolor) {
		this.clearcolor.setClearcolor();

		if (this.useDepth) {
			glClearDepth(1f);
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		} else {
			glClear(GL_COLOR_BUFFER_BIT);
		}
		otherClearcolor.setClearcolor();
	}

	public void clearFramebuffer(int clearColorValue) {
		this.clearcolor.setClearcolor();

		if (this.useDepth) {
			glClearDepth(1f);
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		} else {
			glClear(GL_COLOR_BUFFER_BIT);
		}
		float r = (float) (clearColorValue >> 16 & 255) / 255.0f;
		float g = (float) (clearColorValue >> 8 & 255) / 255.0f;
		float b = (float) (clearColorValue & 255) / 255.0f;
		float a = (float) (clearColorValue >> 24 & 255) / 255.0f;
		glClearColor(r, g, b, a);
	}

	public void bindFramebuffer() {
		glBindFramebuffer(GL_FRAMEBUFFER, frameBufferID);
		viewport.setViewport();
	}

	public void unbindFramebuffer(RenderContext.Viewport otherViewport) {
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		otherViewport.setViewport();
	}

	public void unbindFramebuffer(int viewPortX, int viewPortY, int viewPortW, int viewPortH) {
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		glViewport(viewPortX, viewPortY, viewPortW, viewPortH);
	}

	public void delete() {
		deleteTexture();
		deleteFrameBuffer();
	}

	public void deleteTexture() {
		if (textureID != 0) {
			glDeleteTextures(textureID);
			textureID = 0;
		}
	}

	public void deleteFrameBuffer() {
		complete = false;
		if (frameBufferID != 0) {
			glDeleteFramebuffers(frameBufferID);
			frameBufferID = 0;
		}
		if (renderBufferID != 0) {
			glDeleteRenderbuffers(renderBufferID);
			renderBufferID = 0;
		}
	}

	public int getTextureID() {
		return textureID;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getFramebufferID() {
		return frameBufferID;
	}
}
