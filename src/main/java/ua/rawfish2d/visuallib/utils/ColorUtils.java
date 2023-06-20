package ua.rawfish2d.visuallib.utils;


import org.joml.Math;
import org.joml.Vector3f;

public class ColorUtils {
	public static int getRGBA(float r, float g, float b, float a) {
		return ((int) (a * 255f) << 24) | ((int) (r * 255f) << 16) | ((int) (g * 255f) << 8) | ((int) (b * 255f) << 0);
	}

	public static int transparency(int color, float alpha) {
		float r = (float) (color >> 16 & 255) / 255.0f;
		float g = (float) (color >> 8 & 255) / 255.0f;
		float b = (float) (color & 255) / 255.0f;

		float r2 = 0.003921569f * r;
		float g2 = 0.003921569f * g;
		float b2 = 0.003921569f * b;
		return getRGBA(r2, g2, b2, alpha);
	}

	public static int changeAlpha(int color, float alpha) {
		float r = (color >> 16 & 255) / 255.0f;
		float g = (color >> 8 & 255) / 255.0f;
		float b = (color & 255) / 255.0f;
		return getRGBA(r, g, b, alpha);
	}

	public static int changeAlphaByFactor(int color, float factor) {
		float a = (color >> 24 & 255) / 255.0f;
		float r = (color >> 16 & 255) / 255.0f;
		float g = (color >> 8 & 255) / 255.0f;
		float b = (color & 255) / 255.0f;
		return getRGBA(r, g, b, a * factor);
	}

	public static float[] getRGBA(int color) {
		float a = (float) (color >> 24 & 255) / 255.0f;
		float r = (float) (color >> 16 & 255) / 255.0f;
		float g = (float) (color >> 8 & 255) / 255.0f;
		float b = (float) (color & 255) / 255.0f;
		return new float[]{r, g, b, a};
	}

	public static int darker(int color, float factor) {
		int r = (int) (((float) (color >> 16 & 255)) * factor);
		int g = (int) (((float) (color >> 8 & 255)) * factor);
		int b = (int) (((float) (color & 255)) * factor);
		int a = color >> 24 & 255;
		return (r & 255) << 16 | (g & 255) << 8 | b & 255 | (a & 255) << 24;
	}

	public static int lighter(int color, float fraction) {
		int r = (color >> 16 & 255);
		int g = (color >> 8 & 255);
		int b = (color & 255);
		int a = color >> 24 & 255;

		int red = MathUtils.clamp(Math.round(r * (1.0f + fraction)), 0, 255);
		int green = MathUtils.clamp(Math.round(g * (1.0f + fraction)), 0, 255);
		int blue = MathUtils.clamp(Math.round(b * (1.0f + fraction)), 0, 255);

		return (red & 255) << 16 | (green & 255) << 8 | blue & 255 | (a & 255) << 24;
	}

	public static double colorDistance(float r1, float g1, float b1, float r2, float g2, float b2) {
		float d1 = r2 - r1;
		float d2 = g2 - g1;
		float d3 = b2 - b1;
		return Math.sqrt(d1 * d1 + d2 * d2 + d3 * d3);
	}

	public static double colorDistance(float[] color1, float[] color2) {
		return ColorUtils.colorDistance(color1[0], color1[1], color1[2], color2[0], color2[1], color2[2]);
	}

	public static double colorDistance(int color1, int color2) {
		float r1 = (float) (color1 >> 16 & 255) / 255.0f;
		float g1 = (float) (color1 >> 8 & 255) / 255.0f;
		float b1 = (float) (color1 & 255) / 255.0f;

		float r2 = (float) (color2 >> 16 & 255) / 255.0f;
		float g2 = (float) (color2 >> 8 & 255) / 255.0f;
		float b2 = (float) (color2 & 255) / 255.0f;

		return ColorUtils.colorDistance(r1, g1, b1, r2, g2, b2);
	}

	public static boolean isDark(float r, float g, float b) {
		double dWhite = ColorUtils.colorDistance(r, g, b, 1f, 1f, 1f);
		double dBlack = ColorUtils.colorDistance(r, g, b, 0f, 0f, 0f);
		if (dBlack < dWhite) {
			return true;
		}
		return false;
	}

	public static float clamp(float v) {
		if (v < 0f)
			return 0f;
		if (v > 255f)
			return 255f;
		return v;
	}

	public static int TransformH(final int RGBIn, float fHue) {
		final float cosA = MathUtils.cos(fHue * 3.14159265f / 180); // convert degrees to radians
		final float sinA = MathUtils.sin(fHue * 3.14159265f / 180); // convert degrees to radians
		// calculate the rotation matrix, only depends on Hue
		final float[][] matrix = new float[][]{ //
				{cosA + (1.0f - cosA) / 3.0f, 1.0f / 3.0f * (1.0f - cosA) - MathUtils.sqrt(1.0f / 3.0f) * sinA, 1.0f / 3.0f * (1.0f - cosA) + MathUtils.sqrt(1.0f / 3.0f) * sinA}, //
				{1.0f / 3.0f * (1.0f - cosA) + MathUtils.sqrt(1.0f / 3.0f) * sinA, cosA + 1.0f / 3.0f * (1.0f - cosA), 1.0f / 3.0f * (1.0f - cosA) - MathUtils.sqrt(1.0f / 3.0f) * sinA}, //
				{1.0f / 3.0f * (1.0f - cosA) - MathUtils.sqrt(1.0f / 3.0f) * sinA, 1.0f / 3.0f * (1.0f - cosA) + MathUtils.sqrt(1.0f / 3.0f) * sinA, cosA + 1.0f / 3.0f * (1.0f - cosA)}}; //
		// Use the rotation matrix to convert the RGB directly

		float r = ((RGBIn & 0xFF0000) >> 16) / 255F;
		float g = ((RGBIn & 0x00FF00) >> 8) / 255F;
		float b = ((RGBIn & 0x0000FF)) / 255F;

		float r2 = clamp(r * matrix[0][0] + g * matrix[0][1] + b * matrix[0][2]);
		float g2 = clamp(r * matrix[1][0] + g * matrix[1][1] + b * matrix[1][2]);
		float b2 = clamp(r * matrix[2][0] + g * matrix[2][1] + b * matrix[2][2]);
		/*
		out.r = clamp(in.r * matrix[0][0] + in.g * matrix[0][1] + in.b * matrix[0][2]);
		out.g = clamp(in.r * matrix[1][0] + in.g * matrix[1][1] + in.b * matrix[1][2]);
		out.b = clamp(in.r * matrix[2][0] + in.g * matrix[2][1] + in.b * matrix[2][2]);
		*/
		return 0xFF000000 | ((int) (r2 * 255f) << 16) | ((int) (g2 * 255f) << 8) | ((int) (b2 * 255f) << 0);
	}

	public static float dot(float x, float y, float z, float x2, float y2, float z2) {
		return Math.fma(x2, x, Math.fma(y2, y, z2 * z));
	}

	// from https://gist.github.com/mairod/a75e7b44f68110e1576d77419d608786?permalink_comment_id=3180018#gistcomment-3180018
	/*
	float3 ApplyHue(float3 col, float hueAdjust)
	{
		const float3 k = float3(0.57735, 0.57735, 0.57735);
		half cosAngle = cos(hueAdjust);
		return col * cosAngle + cross(k, col) * sin(hueAdjust) + k * dot(k, col) * (1.0 - cosAngle);
	}
	*/
	public static Vector3f hueShift(float colorR, float colorG, float colorB, float hue) {
		float k = 0.57735f; // 1 / sqrt(3)
		float cosAngle = MathUtils.cos(hue);
		//
		float v1x = colorR * cosAngle;
		float v1y = colorG * cosAngle;
		float v1z = colorB * cosAngle;
		//
		float v2x = Math.fma(k, colorB, -k * colorG);
		float v2y = Math.fma(k, colorR, -k * colorB);
		float v2z = Math.fma(k, colorG, -k * colorR);
		//
		float f1 = MathUtils.sin(hue);
		//
		float dot = dot(k, k, k, colorR, colorG, colorB);
		float v3x = k * dot;
		float v3y = k * dot;
		float v3z = k * dot;
		// *
		float f2 = (1.0f - cosAngle);
		//
		float mul1x = v2x * f1;
		float mul1y = v2y * f1;
		float mul1z = v2z * f1;
		float mul2x = v3x * f2;
		float mul2y = v3y * f2;
		float mul2z = v3z * f2;
		//
		return new Vector3f(mul1x + mul2x + v1x,
				mul1y + mul2y + v1y,
				mul1z + mul2z + v1z);
	}

	public static int getRainbow(float seed, float speed, float brightness) {
		float factor = seed * speed;
		Vector3f vec = hueShift(brightness, 0f, 0f, factor);
		return rgb2int(vec.x, vec.y, vec.z, 1f);
	}

	public static float clampFloat(float value) {
		if (value > 1f)
			value = 1f;

		if (value < 0f)
			value = 0f;

		return value;
	}

	public static int rgb2int(float r, float g, float b, float a) {
		int i_color = 0;
		i_color |= ((int) (ColorUtils.clampFloat(r) * 255) << 16); // red
		i_color |= ((int) (ColorUtils.clampFloat(g) * 255) << 8); // green
		i_color |= ((int) (ColorUtils.clampFloat(b) * 255)); // blue
		i_color |= ((int) (ColorUtils.clampFloat(a) * 255) << 24); // alpha

		return i_color;
	}

	public static int hvs2rgb(float hue, float saturation, float brightness) {
		int h = (int) (hue * 6);
		float f = hue * 6 - h;
		float p = brightness * (1 - saturation);
		float q = brightness * (1 - f * saturation);
		float t = brightness * (1 - (1 - f) * saturation);

		switch (h) {
			case 0:
				return rgb2int(brightness, t, p, 1f);
			case 1:
				return rgb2int(q, brightness, p, 1f);
			case 2:
				return rgb2int(p, brightness, t, 1f);
			case 3:
				return rgb2int(p, q, brightness, 1f);
			case 4:
				return rgb2int(t, p, brightness, 1f);
			case 5:
				return rgb2int(brightness, p, q, 1f);
			default:
				throw new RuntimeException("Something went wrong when converting from HSV to RGB. Input was " + hue + ", " + saturation + ", " + brightness);
		}
	}
}
