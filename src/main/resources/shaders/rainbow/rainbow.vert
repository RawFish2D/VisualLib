#version 330 core

in vec2 aPos;
in vec2 aTexCoord;

out vec2 vTexCoord;
out vec2 vResolution;

uniform vec2 u_resolution;
uniform float u_scale;

void main()
{
	float x = (aPos.x * u_scale * 2.0 / u_resolution.x) - 1.0;
	float y = (aPos.y * u_scale * 2.0 / u_resolution.y) - 1.0;
	gl_Position = vec4(x, -y, 1.0, 1.0);

	vTexCoord = aTexCoord;
	vResolution = u_resolution;
}
