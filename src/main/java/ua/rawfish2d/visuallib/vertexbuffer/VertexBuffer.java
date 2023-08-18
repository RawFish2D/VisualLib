package ua.rawfish2d.visuallib.vertexbuffer;

import lombok.Getter;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL20;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.LinkedList;

import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.glBufferData;
import static org.lwjgl.opengl.GL30.*;

public class VertexBuffer {
	private final LinkedList<VertexAttribute> vertexAttributes = new LinkedList<>();
	private final LinkedList<ByteBuffer> buffers = new LinkedList<>();
	private final LinkedList<ByteBuffer> temporaryBuffers = new LinkedList<>();
	@Getter
	private IntBuffer indexBuffer;

	@Getter
	private int totalBufferSize = 0;
	@Getter
	private int indexBufferSize = 0;

	@Getter
	private int vaoID = 0;
	@Getter
	private int vboID = 0;
	@Getter
	private int eboID = 0;

	private int indicesCount = 0;
	private int indicesCountDrawing = 0;
	private int drawType = GL_TRIANGLES;

	@Getter
	private ShaderProgram shader;

	private int maxObjects = 2048;
	public int indicesPerObject = 3;
	public int verticesPerObject = 3;

	private boolean buffersChanged = true;

	/**
	 * Creates and binds VAO, VBO and EBO.
	 */
	public VertexBuffer() {
		this.vaoID = glGenVertexArrays();
		glBindVertexArray(vaoID);
		this.vboID = glGenBuffers();
		this.eboID = glGenBuffers();
		glBindVertexArray(0);
	}

	public void setMaxObjectCount(int objectCount) {
		this.maxObjects = objectCount;
	}

	/**
	 * Adds vertex attribute. If no shader was set before this call, program will crash.
	 *
	 * @param name             Attribute name in shader.
	 * @param elementCount     Values count, must be 1, 2, 3 or 4.
	 * @param elementByteCount One value byte size.
	 * @param normalized       Specifies whether fixed-point data values should be normalized to (0.0 - 1.0).
	 * @param type             Specifies the data type of each component in the array.
	 *                         The symbolic constants GL_BYTE, GL_UNSIGNED_BYTE, GL_SHORT, GL_UNSIGNED_SHORT, GL_INT, GL_UNSIGNED_INT,
	 *                         GL_HALF_FLOAT, GL_FLOAT, GL_DOUBLE, GL_FIXED, GL_INT_2_10_10_10_REV, GL_UNSIGNED_INT_2_10_10_10_REV and GL_UNSIGNED_INT_10F_11F_11F_REV
	 * @return VertexAttribute object that can be used in addBuffer() method
	 */
	public VertexAttribute addVertexAttribute(String name, int elementCount, int elementByteCount, boolean normalized, int type) {
		final int shaderID = shader.getProgram();
		final int location = GL20.glGetAttribLocation(shaderID, name);
		final VertexAttribute attribute = new VertexAttribute(location, elementCount, elementByteCount, normalized, type);
		vertexAttributes.add(attribute);
		return attribute;
	}

	/**
	 * Adds single temporary and single regular buffer for list of Vertex Attributes.
	 *
	 * @param list List of VertexAttribute objects.
	 */
	public void addBuffer(Collection<VertexAttribute> list) {
		int bufferSize = 0;
		for (VertexAttribute attrib : list) {
			bufferSize += attrib.getSize();
		}
		bufferSize *= verticesPerObject * maxObjects;
		totalBufferSize += bufferSize;
		final ByteBuffer buffer = BufferUtils.createByteBuffer(bufferSize);
		buffers.add(buffer);
		final ByteBuffer temporaryBuffer = BufferUtils.createByteBuffer(bufferSize);
		temporaryBuffers.add(temporaryBuffer);
	}

	/**
	 * Adds single temporary and single regular buffer for that vertex attribute.
	 *
	 * @param attribute vertex attribute object.
	 */
	public void addBuffer(VertexAttribute attribute) {
		final int bufferSize = attribute.getSize() * verticesPerObject * maxObjects;
		totalBufferSize += bufferSize;
		final ByteBuffer buffer = BufferUtils.createByteBuffer(bufferSize);
		buffers.add(buffer);
		final ByteBuffer temporaryBuffer = BufferUtils.createByteBuffer(bufferSize);
		temporaryBuffers.add(temporaryBuffer);
	}

	/**
	 * Sets draw type for VertexBuffer.
	 *
	 * @param drawType Currently can be:
	 *                 GL_QUADS or GL_TRIANGLES.
	 *                 Note: though GL_QUADS is not supported by OpenGL Core Profile, when used,
	 *                 will be set as GL_TRIANGLES with vertices count of 4 and indices count of 6.
	 */
	public void setDrawType(int drawType) {
		this.drawType = drawType;
		if (drawType == GL_QUADS) {
			verticesPerObject = 4;
			indicesPerObject = 6;
			this.drawType = GL_TRIANGLES; // because you can't draw GL_QUADS
		} else if (drawType == GL_TRIANGLES) {
			verticesPerObject = 3;
			indicesPerObject = 3;
		}
	}

	/**
	 * @param index index of that buffer.
	 * @return regular buffer.
	 */
	private ByteBuffer getBuffer(int index) {
		return buffers.get(index);
	}

	/**
	 * @param index index of that buffer.
	 * @return temporary buffer.
	 */
	public ByteBuffer getTemporaryBuffer(int index) {
		return temporaryBuffers.get(index);
	}

	/**
	 * Binds EBO, uploads buffer via glBufferData, then unbinds EBO.
	 */
	public void uploadIndexBuffer() {
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STREAM_DRAW);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}


	/**
	 * Binds VBO and uploads buffer via glBufferSubData, then unbinds VBO.
	 *
	 * @param index index of buffer that will be uploaded.
	 */
	public void uploadBuffer(int index) {
		final ByteBuffer buffer = getBuffer(index);
		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		int offset = 0;
		if (index > 0 && vertexAttributes.size() > 1) {
			for (int a = 0; a < index; ++a) {
				final VertexAttribute attribute = this.vertexAttributes.get(a);
				offset += attribute.getSize() * verticesPerObject * maxObjects;
			}
		}
		glBufferSubData(GL_ARRAY_BUFFER, offset, buffer);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}

	/**
	 * Binds VBO and uploads all buffers via glBufferSubData, then unbinds VBO.
	 * Will do nothing if canUpload() is not called before this method.
	 */
	public void uploadBuffers() {
		if (buffersChanged) {
			glBindBuffer(GL_ARRAY_BUFFER, vboID);
			for (int index = 0; index < buffers.size(); ++index) {
				final ByteBuffer buffer = getBuffer(index);
				int offset = 0;
				if (index > 0 && vertexAttributes.size() > 1) {
					for (int a = 0; a < index; ++a) {
						final VertexAttribute attribute = this.vertexAttributes.get(a);
						offset += attribute.getSize() * verticesPerObject * maxObjects;
					}
				}
				glBufferSubData(GL_ARRAY_BUFFER, offset, buffer);
			}
			glBindBuffer(GL_ARRAY_BUFFER, 0);

			buffersChanged = false;
		}
	}

	/**
	 * Kinda hard to explain... Just use this right before uploadBuffers()
	 */
	public void canUpload() {
		for (int index = 0; index < temporaryBuffers.size(); ++index) {
			final ByteBuffer tempBuffer = temporaryBuffers.get(index);
			final ByteBuffer buffer = buffers.get(index);
			tempBuffer.flip();
			buffer.clear();
			buffer.put(tempBuffer);
			buffer.flip();
		}

		indicesCountDrawing = indicesCount;
		buffersChanged = true;
	}

	public void setShader(ShaderProgram program) {
		this.shader = program;
	}

	public void setIndexCount(int index) {
		indicesCount = index;
	}

	public int getIndexCount() {
		return indicesCount;
	}

	/**
	 * Initializes vertex attributes and creates index buffer.
	 * After that you can use this for rendering.
	 */
	public void initBuffers() {
		this.indexBufferSize = maxObjects * indicesPerObject;
		this.indexBuffer = BufferUtils.createIntBuffer(indexBufferSize);

		glBindVertexArray(vaoID);
		glBindBuffer(GL_ARRAY_BUFFER, eboID);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, 0, GL_STREAM_DRAW);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBufferSize, GL_STREAM_DRAW);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		glBufferData(GL_ARRAY_BUFFER, 0, GL_STREAM_DRAW);
		glBufferData(GL_ARRAY_BUFFER, totalBufferSize, GL_STREAM_DRAW);
		initAttribArray();
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		glBindVertexArray(0);
	}

	/**
	 * Resizes vertex and index buffers
	 *
	 * @param newMaxObjects - new max object count (object - triangle or quad)
	 */
	public void resizeBuffer(int newMaxObjects) {
		this.maxObjects = newMaxObjects;

		this.indexBufferSize = maxObjects * indicesPerObject;
		this.indexBuffer = BufferUtils.createIntBuffer(indexBufferSize);

		// TODO remake this for other type of buffer layouts
		buffers.clear();
		temporaryBuffers.clear();
		totalBufferSize = 0;
		for (VertexAttribute attribute : vertexAttributes) {
			final int bufferSize = attribute.getSize() * verticesPerObject * maxObjects;
			totalBufferSize += bufferSize;
			final ByteBuffer buffer = BufferUtils.createByteBuffer(bufferSize);
			buffers.add(buffer);
			final ByteBuffer temporaryBuffer = BufferUtils.createByteBuffer(bufferSize);
			temporaryBuffers.add(temporaryBuffer);
		}

		glBindVertexArray(vaoID);
		glBindBuffer(GL_ARRAY_BUFFER, eboID);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, 0, GL_STREAM_DRAW);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBufferSize, GL_STREAM_DRAW);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		glBufferData(GL_ARRAY_BUFFER, 0, GL_STREAM_DRAW);
		glBufferData(GL_ARRAY_BUFFER, totalBufferSize, GL_STREAM_DRAW);
		initAttribArray();
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);

		indicesCount = 0;
		indicesCountDrawing = 0;
		buffersChanged = true;
	}

	// TODO make this less ugly
	private void initAttribArray() {
		if (buffers.size() == 1) {
			int stride = 0;
			for (VertexAttribute attrib : vertexAttributes) {
				stride += attrib.getSize();
			}

			int offset = 0;
			for (VertexAttribute attrib : vertexAttributes) {
				glEnableVertexAttribArray(attrib.vertexLocation);
				glVertexAttribPointer(attrib.vertexLocation, attrib.vertexElementCount, attrib.type, attrib.normalized, stride, offset);

				offset += attrib.getSize();
			}
		} else {
			int offset = 0;
			for (VertexAttribute attrib : vertexAttributes) {
				int stride = attrib.getSize();
				glEnableVertexAttribArray(attrib.vertexLocation);
				glVertexAttribPointer(attrib.vertexLocation, attrib.vertexElementCount, attrib.type, attrib.normalized, stride, offset);

				offset += attrib.getSize() * verticesPerObject * maxObjects;
			}
		}
	}

	/**
	 * Binds VAO and EBO and draws, specified by method setIndexCount(), indices.
	 */
	public void draw() {
		glBindVertexArray(vaoID);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
		glDrawElements(drawType, indicesCountDrawing, GL_UNSIGNED_INT, 0L);
	}

	public void draw(int[] counts, PointerBuffer pointerBuffer) {
		glBindVertexArray(vaoID);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
		glMultiDrawElements(drawType, counts, GL_UNSIGNED_INT, pointerBuffer);
	}

	/**
	 * Clears temporary buffers
	 */
	public void clearBuffers() {
		indicesCount = 0;
		for (ByteBuffer temporaryBuffer : temporaryBuffers) {
			temporaryBuffer.clear();
		}
	}

	/**
	 * Kinda speaks for itself.
	 */
	public void deleteBuffers() {
		if (vaoID != 0) {
			glDeleteVertexArrays(vaoID);
			vaoID = 0;
		}
		if (vboID != 0) {
			glDeleteBuffers(vboID);
			vboID = 0;
		}
		if (eboID != 0) {
			glDeleteBuffers(eboID);
			eboID = 0;
		}
	}

	public static class VertexAttribute {
		public final int vertexLocation;
		public final int vertexElementCount;
		public final int vertexElementByteCount;
		public final boolean normalized;
		public final int type;

		public VertexAttribute(int vertexLocation, int vertexElementCount, int vertexElementByteCount, boolean normalized, int type) {
			this.vertexLocation = vertexLocation;
			this.vertexElementCount = vertexElementCount;
			this.vertexElementByteCount = vertexElementByteCount;
			this.normalized = normalized;
			this.type = type;
		}

		public int getSize() {
			return vertexElementCount * vertexElementByteCount;
		}
	}
}
