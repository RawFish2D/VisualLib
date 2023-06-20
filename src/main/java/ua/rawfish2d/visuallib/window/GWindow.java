package ua.rawfish2d.visuallib.window;

import lombok.Getter;
import lombok.Setter;
import org.lwjgl.Version;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAddress;

public class GWindow {
	@Getter
	private int displayWidth;
	@Getter
	private int displayHeight;
	public int displayWidthBase;
	public int displayHeightBase;
	@Getter
	private boolean vsync;
	@Getter
	private boolean fullScreen = false;
	public boolean created = false;

	public GLFWErrorCallback errorCallback;
	public GLFWKeyCallback keyCallback;
	private GLFWMouseButtonCallback mouseButtonCallback;
	private GLFWCursorPosCallback cursorPosCallback;
	public GLFWFramebufferSizeCallback fsCallback;
	public GLFWScrollCallback scrollCallback;
	public Callback debugProc;

	public long hwnd = NULL;
	@Setter
	private boolean transparentFramebuffer = false;
	private boolean msaa = false;
	private int msaaLevel = 4;
	private final boolean oldOpengl;
	@Setter
	private boolean alwaysOnTop;

	public GWindow(int screenWidth, int screenHeight, boolean oldOpengl) {
		this.oldOpengl = oldOpengl;
		this.displayWidth = screenWidth;
		this.displayHeight = screenHeight;
		this.displayWidthBase = screenWidth;
		this.displayHeightBase = screenHeight;

		System.out.println("LWJGL " + Version.getVersion());
		System.out.println("GLFW " + glfwGetVersionString());
		glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
	}

	public void create(String windowTitle) {
		if (!glfwInit()) {
			throw new IllegalStateException("Unable to initialize GLFW");
		}

		// cringe
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		long temp = glfwCreateWindow(640, 480, "OpenGL Window", NULL, NULL);
		glfwMakeContextCurrent(temp);
		GL.createCapabilities();
		System.out.println("Max supported OpenGL ver: " + glGetLatestSupportedVersion());
		glfwDestroyWindow(temp);

		createWindow();
		glfwSetWindowTitle(hwnd, windowTitle);

		initCamera();
		setFullscreen(false, true);

		pollEvents();
		swapBuffers();
	}

	public void setKeyCallback(GLFWKeyCallback callback) {
		this.keyCallback = callback;
		glfwSetKeyCallback(hwnd, keyCallback);
	}

	public void setMouseButtonCallback(GLFWMouseButtonCallback callback) {
		this.mouseButtonCallback = callback;
		glfwSetMouseButtonCallback(hwnd, mouseButtonCallback);
	}

	public void setCursorPosCallback(GLFWCursorPosCallback callback) {
		this.cursorPosCallback = callback;
		glfwSetCursorPosCallback(hwnd, cursorPosCallback);
	}

	private void createWindow() {
		glfwDefaultWindowHints();

		if (!oldOpengl) {
			glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
			glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
			glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
			glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
			glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_API);
		}

		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);

		// MSAA
		if (msaa) {
			glfwWindowHint(GLFW_STENCIL_BITS, msaaLevel);
			glfwWindowHint(GLFW_SAMPLES, msaaLevel);
		}

		if (transparentFramebuffer) {
			glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, GLFW_TRUE);
		}

		if (alwaysOnTop) {
			glfwWindowHint(GLFW_FLOATING, GLFW_TRUE);
		}

		hwnd = glfwCreateWindow(displayWidth, displayHeight, "OpenGL Window", NULL, NULL);
		if (hwnd == NULL) {
			throw new RuntimeException("Failed to create the GLFW window");
		}
		glfwMakeContextCurrent(hwnd);

		GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		glfwSetWindowPos(hwnd, (vidmode.width() - displayWidth) / 2, (vidmode.height() - displayHeight) / 2);
		try (MemoryStack frame = MemoryStack.stackPush()) {
			IntBuffer framebufferSize = frame.mallocInt(2);
			nglfwGetFramebufferSize(hwnd, memAddress(framebufferSize), memAddress(framebufferSize) + 4);
			displayWidth = framebufferSize.get(0);
			displayHeight = framebufferSize.get(1);
		}

		// setWindowIcon("icons//32.png");
		glfwSetInputMode(hwnd, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
		glfwSetWindowAttrib(hwnd, GLFW_RESIZABLE, GLFW_FALSE);
		if (transparentFramebuffer) {
			glfwSetWindowAttrib(hwnd, GLFW_DECORATED, GLFW_FALSE);
		}
	}

	public String glGetLatestSupportedVersion() {
		String ver = "none";
		if (GL.getCapabilities().OpenGL11) {
			ver = "1.1";
		}
		if (GL.getCapabilities().OpenGL12) {
			ver = "1.2";
		}
		if (GL.getCapabilities().OpenGL13) {
			ver = "1.3";
		}
		if (GL.getCapabilities().OpenGL14) {
			ver = "1.4";
		}
		if (GL.getCapabilities().OpenGL15) {
			ver = "1.5";
		}

		if (GL.getCapabilities().OpenGL20) {
			ver = "2.0";
		}
		if (GL.getCapabilities().OpenGL21) {
			ver = "2.1";
		}

		if (GL.getCapabilities().OpenGL30) {
			ver = "3.0";
		}
		if (GL.getCapabilities().OpenGL31) {
			ver = "3.1";
		}
		if (GL.getCapabilities().OpenGL32) {
			ver = "3.2";
		}
		if (GL.getCapabilities().OpenGL33) {
			ver = "3.3";
		}

		if (GL.getCapabilities().OpenGL40) {
			ver = "4.0";
		}
		if (GL.getCapabilities().OpenGL41) {
			ver = "4.1";
		}
		if (GL.getCapabilities().OpenGL42) {
			ver = "4.2";
		}
		if (GL.getCapabilities().OpenGL43) {
			ver = "4.3";
		}
		if (GL.getCapabilities().OpenGL44) {
			ver = "4.4";
		}
		if (GL.getCapabilities().OpenGL45) {
			ver = "4.5";
		}
		if (GL.getCapabilities().OpenGL46) {
			ver = "4.6";
		}

		return ver;
	}

	private void initCamera() {
		created = true;
		setVsync(true);

		glClear(GL_COLOR_BUFFER_BIT);
		if (oldOpengl) {
			glMatrixMode(GL_PROJECTION);
			glEnable(GL_COLOR_MATERIAL);
			glLoadIdentity();
			glOrtho(0.0D, displayWidth, displayHeight, 0.0D, -1000.0D, 3000.0D);
			glMatrixMode(GL_MODELVIEW);
			glLoadIdentity();
			glTranslatef(0.0F, 0.0F, -2000.0F);
		}
		glViewport(0, 0, displayWidth, displayHeight);
		glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
	}

	public void showWindow() {
		glfwShowWindow(hwnd);
	}

	public void hideWindow() {
		glfwHideWindow(hwnd);
	}

	public void pollEvents() {
		glfwPollEvents();
	}

	public void swapBuffers() {
		glfwSwapBuffers(hwnd);
	}

	public boolean shouldClose() {
		return glfwWindowShouldClose(hwnd);
	}

	public void destroyWindow() {
		glfwDestroyWindow(hwnd);
	}

	public void enableMSAA(boolean msaa) {
		this.msaa = msaa;
	}

	public void setMSAALevel(int msaaLevel) {
		this.msaaLevel = msaaLevel;
	}

	public void cleanUp() {
		if (debugProc != null) {
			debugProc.free();
		}
		if (keyCallback != null) {
			keyCallback.free();
		}
		if (fsCallback != null) {
			fsCallback.free();
		}
		if (scrollCallback != null) {
			scrollCallback.free();
		}
		if (mouseButtonCallback != null) {
			mouseButtonCallback.free();
		}
		if (cursorPosCallback != null) {
			cursorPosCallback.free();
		}
	}

	public void terminate() {
		closeWindow();
		glfwTerminate();
	}

	public void closeWindow() {
		glfwFreeCallbacks(hwnd);
		glfwDestroyWindow(hwnd);
	}

	public void setPos(int x, int y) {
		glfwSetWindowPos(hwnd, x, y);
	}

	public void resize(int w, int h) {
		glfwSetWindowSize(hwnd, w, h);
		this.displayWidth = w;
		this.displayHeight = h;
		this.displayWidthBase = w;
		this.displayHeightBase = h;
		if (oldOpengl) {
			glMatrixMode(GL_PROJECTION);
			glEnable(GL_COLOR_MATERIAL);
			glLoadIdentity();
			glOrtho(0.0D, displayWidth, displayHeight, 0.0D, -1000.0D, 3000.0D);
			glMatrixMode(GL_MODELVIEW);
			glLoadIdentity();
			glTranslatef(0.0F, 0.0F, -2000.0F);
		}
		glViewport(0, 0, w, h);
	}

	public void setFullscreen(boolean fullscreen, boolean windowed) {
		if (fullscreen == this.fullScreen) {
			return;
		}

		this.fullScreen = fullscreen;
		long monitor = glfwGetPrimaryMonitor();
		GLFWVidMode mode = glfwGetVideoMode(monitor);

		if (fullscreen) {
			if (windowed) {
				glfwSetWindowAttrib(hwnd, GLFW_DECORATED, GLFW_FALSE);
				glfwSetWindowMonitor(hwnd, NULL, 0, 0, mode.width(), mode.height(), mode.refreshRate());
				resize(mode.width(), mode.height());
			} else {
				glfwSetWindowMonitor(hwnd, monitor, 0, 0, displayWidthBase, displayHeightBase, mode.refreshRate());
			}
		} else {
			glfwSetWindowAttrib(hwnd, GLFW_DECORATED, GLFW_TRUE);
			glfwSetWindowMonitor(hwnd, NULL, 0, 0, displayWidth, displayHeight, mode.refreshRate());
			resize(displayWidthBase, displayHeightBase);
			glfwSetWindowPos(hwnd, 50, 50);
		}

		setVsync(vsync);
	}

	public void setVsync(boolean b) {
		this.vsync = b;
		if (vsync) {
			glfwSwapInterval(1);
		} else {
			glfwSwapInterval(0);
		}
	}
}