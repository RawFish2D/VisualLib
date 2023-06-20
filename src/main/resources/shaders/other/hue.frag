precision highp float;
in vec2 uv;
uniform float hue;

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
	gl_FragColor = vec4(hsb_to_rgb(hue, uv.x, uv.y), 1.0);
}