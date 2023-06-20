#version 330 core

in vec2 vTexCoord;
in vec2 vResolution;
uniform float u_time;
uniform float u_rainbowScale;
uniform float brightnessDecreaseFactor;

out vec4 colorOut;

void main()
{
	// Normalized pixel coordinates (from 0 to 1)
	vec2 uv = ((vTexCoord / vResolution) * 1000.0) * -u_rainbowScale;

	// Time varying pixel color
	vec3 col = 0.5 + 0.5 * cos(u_time + uv.xxx + vec3(0, 2, 4));
	//col = col * (1.0 - vTexCoord.y + brightnessDecreaseFactor);

	// Output to screen
	colorOut = vec4(col, 1.0);
}  
