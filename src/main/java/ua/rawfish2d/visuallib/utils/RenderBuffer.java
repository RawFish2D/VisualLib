package ua.rawfish2d.visuallib.utils;

import lombok.Getter;
import org.lwjgl.PointerBuffer;
import ua.rawfish2d.visuallib.vertexbuffer.VertexBuffer;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class RenderBuffer {
	@Getter
	protected VertexBuffer vbo;
	protected ByteBuffer vertexBuffer = null;
	protected ByteBuffer colorBuffer = null;
	protected ByteBuffer texCoordBuffer = null;
	@Getter
	protected IntBuffer indexBuffer = null;

	public void setBuffers(VertexBuffer vbo, int vertexBufferIndex, int colorBufferIndex, int texCoordBufferIndex) {
		this.vbo = vbo;
		if (vertexBufferIndex != -1) {
			this.vertexBuffer = vbo.getTemporaryBuffer(vertexBufferIndex);
		}
		if (colorBufferIndex != -1) {
			this.colorBuffer = vbo.getTemporaryBuffer(colorBufferIndex);
		}
		if (texCoordBufferIndex != -1) {
			this.texCoordBuffer = vbo.getTemporaryBuffer(texCoordBufferIndex);
		}
		this.indexBuffer = vbo.getIndexBuffer();
	}

	public void addVertex(float x, float y, int color, float u, float v) {
		writeVertex(x, y);
		writeColor(color);
		writeTexCoord(u, v);
	}

	public void addVertex(float x, float y, float z, int color, float u, float v) {
		writeVertex(x, y, z);
		writeColor(color);
		writeTexCoord(u, v);
	}

	public void addVertex(float x, float y, float z, float u, float v) {
		writeVertex(x, y, z);
		writeTexCoord(u, v);
	}

	public void addVertex(float x, float y, float u, float v) {
		writeVertex(x, y);
		writeTexCoord(u, v);
	}

	public void addVertex(float x, float y, int color) {
		writeVertex(x, y);
		writeColor(color);
	}

	public void clear() {
		vbo.clearBuffers();
	}

	public void canUpload() {
		vbo.canUpload();
	}

	public void uploadBuffers() {
		vbo.uploadBuffers();
	}

	public void uploadIndexBuffer() {
		vbo.uploadIndexBuffer();
	}

	public void draw() {
		vbo.draw();
	}

	public void draw(int[] counts, PointerBuffer pointers) {
		vbo.draw(counts, pointers);
	}

	public void deleteBuffers() {
		vbo.deleteBuffers();
	}

	protected void writeVertex(float x, float y, float z) {
		vertexBuffer.putFloat(x).putFloat(y).putFloat(z);
	}

	protected void writeVertex(float x, float y) {
		vertexBuffer.putFloat(x).putFloat(y);
	}

	protected void writeColor(int color) {
		colorBuffer.putInt(color);
	}

	protected void writeTexCoord(float u, float v) {
		texCoordBuffer.putFloat(u).putFloat(v);
	}

	public void addIndexCount(int amount) {
		vbo.setIndexCount(vbo.getIndexCount() + amount);
	}
}
