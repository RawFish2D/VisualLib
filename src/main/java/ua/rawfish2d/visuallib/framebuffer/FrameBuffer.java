package ua.rawfish2d.visuallib.framebuffer;

import lombok.Getter;
import org.lwjgl.opengl.GL;
import ua.rawfish2d.visuallib.texture.TextureUtils;
import ua.rawfish2d.visuallib.utils.RenderContext;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class FrameBuffer {
	@Getter
	protected final int width;
	@Getter
	protected final int height;
	@Getter
	protected int frameBufferID = 0;
	@Getter
	protected int textureID = 0;
	@Getter
	protected final boolean hasDepthBuffer;
	protected int renderBufferID = 0;
	protected boolean complete = false;
	protected final RenderContext.Clearcolor clearcolor = new RenderContext.Clearcolor();
	protected final RenderContext.Viewport viewport = new RenderContext.Viewport();
	@Getter
	protected final int layerCount;

	public FrameBuffer(int width, int height, int textureID, int layerCount, boolean hasDepthBuffer) {
		this.width = width;
		this.height = height;
		this.layerCount = layerCount;
		this.hasDepthBuffer = hasDepthBuffer;
		this.viewport.setViewport(0, 0, width, height);
		this.clearcolor.setClearColor(0xFF000000);

		// check if GL_EXT_framebuffer_object can be used on this system
		if (!GL.getCapabilities().GL_EXT_framebuffer_object) {
			System.out.println("GL_EXT_framebuffer_object is not supported!");
		} else {
			delete();

			frameBufferID = glGenFramebuffers();
			glBindFramebuffer(GL_FRAMEBUFFER, frameBufferID);

			if (this.hasDepthBuffer) {
				renderBufferID = glGenRenderbuffers();
			}

			this.textureID = textureID;
			if (layerCount == 1) {
				// attach texture to the fbo
				glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureID, 0);
			} else {
				// attach texture to the fbo
				int[] drawBuffers = new int[layerCount];
				for (int a = 0; a < layerCount; ++a) {
					int attachmentID = GL_COLOR_ATTACHMENT0 + a;
					drawBuffers[a] = attachmentID;
					glFramebufferTexture3D(GL_FRAMEBUFFER, attachmentID, GL_TEXTURE_2D_ARRAY, textureID, 0, a);
				}
				glDrawBuffers(drawBuffers);
			}

			if (hasDepthBuffer) {
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

	public FrameBuffer(int width, int height, int wrapS, int wrapT, int filteringMin, int filteringMax, boolean hasDepthBuffer) {
		this(width, height, wrapS, wrapT, filteringMin, filteringMax, hasDepthBuffer, 1);
	}

	public FrameBuffer(int width, int height, int wrapS, int wrapT, int filteringMin, int filteringMax, boolean hasDepthBuffer, int layerCount) {
		this(width, height,
				layerCount == 1 ?
						TextureUtils.createTexture(width, height, wrapS, wrapT, filteringMin, filteringMax) :
						TextureUtils.createTextureArray(width, height, layerCount, wrapS, wrapT, filteringMin, filteringMax),
				layerCount, hasDepthBuffer);
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

		if (this.hasDepthBuffer) {
			glClearDepth(1f);
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		} else {
			glClear(GL_COLOR_BUFFER_BIT);
		}
		otherClearcolor.setClearcolor();
	}

	public void clearFramebuffer(int otherClearcolor) {
		this.clearcolor.setClearcolor();

		if (this.hasDepthBuffer) {
			glClearDepth(1f);
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		} else {
			glClear(GL_COLOR_BUFFER_BIT);
		}
		float r = (float) (otherClearcolor >> 16 & 255) / 255.0f;
		float g = (float) (otherClearcolor >> 8 & 255) / 255.0f;
		float b = (float) (otherClearcolor & 255) / 255.0f;
		float a = (float) (otherClearcolor >> 24 & 255) / 255.0f;
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
}
