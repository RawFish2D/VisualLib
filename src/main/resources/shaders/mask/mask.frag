#version 330 core

in vec2 vTexCoord;
uniform sampler2D texture;

out vec4 colorOut;

void main()
{
	float maskAlpha = texture2D(texture, vTexCoord).a;
	vec2 texCoord2 = vec2(vTexCoord.x, vTexCoord.y + 0.5);
	colorOut = vec4(texture2D(texture, texCoord2).rgb, maskAlpha);
}  
