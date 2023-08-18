package ua.rawfish2d.visuallib.vertexbuffer;

import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL41.*;

public class ShaderProgram {
	@Getter
	private int program = 0;
	private int vertID = 0;
	private int fragID = 0;

	public void loadShaders(InputStream vertexShaderStream, InputStream fragmentShaderStream) throws RuntimeException {
		final String vertexShaderCode = readInputStreamToString(vertexShaderStream);
		final String fragmentShaderCode = readInputStreamToString(fragmentShaderStream);
		loadShaders(vertexShaderCode, fragmentShaderCode);
	}

	public void loadShaders(String vertexShaderCode, String fragmentShaderCode) throws RuntimeException {
		if (program != 0) {
			glDeleteProgram(program);
		}

		try {
			this.vertID = createShader(vertexShaderCode, GL_VERTEX_SHADER);
		} catch (RuntimeException e) {
			this.vertID = 0;
			System.out.println("Error while creating vertex shader: " + vertexShaderCode);
			this.deleteShader();
			throw e;
		}

		try {
			this.fragID = createShader(fragmentShaderCode, GL_FRAGMENT_SHADER);
		} catch (RuntimeException e) {
			this.fragID = 0;
			System.out.println("Error while creating fragment shader: " + fragmentShaderCode);
			this.deleteShader();
			throw e;
		}

		program = glCreateProgram();
		glAttachShader(program, vertID);
		glAttachShader(program, fragID);
		glLinkProgram(program);

		if (glGetProgrami(program, GL_LINK_STATUS) == GL11.GL_FALSE) {
			System.out.println(glGetProgramInfoLog(program, glGetProgrami(program, GL_INFO_LOG_LENGTH)));
			this.deleteShader();
			throw new RuntimeException("Program failed to link.");
		}

		glValidateProgram(program);
	}

	public String getProgramLog(int maxLength) {
		return glGetProgramInfoLog(program, maxLength);
	}

	private int createShader(String shaderCode, int shaderType) throws RuntimeException {
		int shaderID = 0;
		try {
			shaderID = glCreateShader(shaderType);

			glShaderSource(shaderID, shaderCode);
			glCompileShader(shaderID);

			if (glGetShaderi(shaderID, GL_COMPILE_STATUS) == GL11.GL_FALSE) {
				System.out.println(glGetShaderInfoLog(shaderID, 2048));
				throw new RuntimeException("Failed to compile shader: " + shaderCode);
			}

			return shaderID;
		} catch (RuntimeException exc) {
			glDeleteShader(shaderID);
			throw exc;
		}
	}

	public void deleteShader() {
		if (program != 0) {
			glDetachShader(program, vertID);
			glDetachShader(program, fragID);
			glDeleteShader(vertID);
			glDeleteShader(fragID);
			glDeleteProgram(program);
			vertID = 0;
			fragID = 0;
			program = 0;
		}
	}

	public void setUniformMatrix4f(String uniform, Matrix4f matrix) {
		int loc = glGetUniformLocation(program, uniform);
		try (MemoryStack stack = MemoryStack.stackPush()) {
			glUniformMatrix4fv(loc, false, matrix.get(stack.mallocFloat(16)));
		}
	}

	public void setUniform1i(String uniform, int i1) {
		int loc = glGetUniformLocation(program, uniform);
		glUniform1i(loc, i1);
	}

	public void setUniform1f(String uniform, float fl) {
		int loc = glGetUniformLocation(program, uniform);
		glUniform1f(loc, fl);
	}

	public void setUniform2f(String uniform, float f1, float f2) {
		int loc = glGetUniformLocation(program, uniform);
		glUniform2f(loc, f1, f2);
	}

	public void setUniform3f(String uniform, float f1, float f2, float f3) {
		int loc = glGetUniformLocation(program, uniform);
		glUniform3f(loc, f1, f2, f3);
	}

	public void setUniform3f(String uniform, Vector3f vec) {
		int loc = glGetUniformLocation(program, uniform);
		glUniform3f(loc, vec.x, vec.y, vec.z);
	}

	public void setUniform4f(String uniform, float f1, float f2, float f3, float f4) {
		int loc = glGetUniformLocation(program, uniform);
		glUniform4f(loc, f1, f2, f3, f4);
	}

	private String loadFileToString(String fileName) throws IOException {
		return new String(Files.readAllBytes(Paths.get(fileName)));
	}

	/**
	 * Requires at least OpenGL 4.1
	 */
	public boolean loadShaderBinary(ByteBuffer shaderBinary, int binaryFormat) throws IOException {
		this.program = glCreateProgram();
		glProgramBinary(program, binaryFormat, shaderBinary);
		int status = glGetProgrami(program, GL_LINK_STATUS);
		if (GL_FALSE == status) {
			System.out.println("FAILED");
			glDeleteProgram(program);
			return false;
		}
		System.out.println("OK");
		return true;
	}

	/**
	 * Requires at least OpenGL 4.1
	 */
	public Binary getShaderBinary() {
		int formats = glGetInteger(GL_NUM_PROGRAM_BINARY_FORMATS);
		if (formats < 1) {
			System.err.println("Driver doesnt support any binary formats");
			return null;
		}
		final int length = glGetProgrami(program, GL_PROGRAM_BINARY_LENGTH);
		final ByteBuffer buffer = BufferUtils.createByteBuffer(length);
		final IntBuffer binaryFormat = BufferUtils.createIntBuffer(1);
		final IntBuffer lengthBuffer = BufferUtils.createIntBuffer(1);
		lengthBuffer.put(length);
		lengthBuffer.flip();
		glGetProgramBinary(program, lengthBuffer, binaryFormat, buffer);
		return new Binary(buffer, binaryFormat, lengthBuffer);
	}

	private String readInputStreamToString(InputStream stream) {
		StringBuilder stringBuilder = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
			String strCurrentLine;
			while ((strCurrentLine = reader.readLine()) != null) {
				stringBuilder.append(strCurrentLine);
				stringBuilder.append("\r\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stringBuilder.toString();
	}

	public static class Binary {
		@Getter
		private final ByteBuffer data;
		@Getter
		private final int shaderFormat;
		@Getter
		private final int length;

		public Binary(ByteBuffer data, IntBuffer binaryFormat, IntBuffer length) {
			this.data = data;
			this.shaderFormat = binaryFormat.get();
			this.length = length.get();
		}
	}
}