#version 330 core

in vec2 vTexCoord;
in vec4 vColor;

uniform sampler2D u_texture;

out vec4 outColor;

void main() {
	outColor = texture(u_texture, vTexCoord) * vColor;
}