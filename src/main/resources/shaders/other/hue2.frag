#version 330 core

precision highp float;
in vec2 uv;
const float pi = 3.14159265359;
const float pi2 = pi * 2.0;

vec3 hsb_to_rgb(float h, float s, float l)
{
	float c = l * s;
	h = mod((h * 6.0), 6.0);
	float x = c * (1.0 - abs(mod(h, 2.0) - 1.0));
	vec3 result;

	if (0.0 <= h && h < 1.0) {
		result = vec3(c, x, 0.0);
	} else if (1.0 <= h && h < 2.0) {
		result = vec3(x, c, 0.0);
	} else if (2.0 <= h && h < 3.0) {
		result = vec3(0.0, c, x);
	} else if (3.0 <= h && h < 4.0) {
		result = vec3(0.0, x, c);
	} else if (4.0 <= h && h < 5.0) {
		result = vec3(x, 0.0, c);
	} else if (5.0 <= h && h < 6.0) {
		result = vec3(c, 0.0, x);
	} else {
		result = vec3(0.0, 0.0, 0.0);
	}

	result.rgb += l - c;
	return result;
}

void main()
{
	const float x0 = 0.5;
	const float y0 = 0.5;
	const float r0 = 0.35;
	const float r1 = 0.5;
	float dd = (uv.x - x0) * (uv.x - x0) + (uv.y - y0) * (uv.y - y0);
	//if ((dd < r0 * r0) || (dd > r1 * r1)) {
	if ((dd > r1 * r1)) {
		gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
	}
	else {
		float ang = atan(uv.y - y0, uv.x - x0) + pi; // < -pi, +pi >
		ang /= pi2;
		gl_FragColor = vec4(hsb_to_rgb(ang, 1.0, 1.0), 1.0); // wheel
		//gl_FragColor = vec4(hsb_to_rgb(uv.x, 1.0, 1.0), 1.0); // quad
	}
}