#version 330 core

in vec2 vTexCoord;
in vec4 vColor;
uniform sampler2D texture;

out vec4 colorOut;

void main()
{
	colorOut = texture2D(texture, vTexCoord) * vColor;
}  
