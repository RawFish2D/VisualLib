#version 330 core

in vec2 vTexCoord;
in vec2 vResolution;
uniform float u_time;
uniform float u_rainbowScale;

uniform float redCount;
uniform float greenCount;
uniform float blueCount;
uniform float brightnessDecreaseFactor;

out vec4 colorOut;

void main()
{
	// Normalized pixel coordinates (from 0 to 1)
	vec2 uv = ((vTexCoord / vResolution) * 1000.0) * u_rainbowScale;

	// Time varying pixel color
	vec3 col = 0.5 + 0.5 * cos(u_time + uv.xxx + vec3(0, 2, 4));
	col = col * (1.0 - vTexCoord.y + brightnessDecreaseFactor);

	// reduces colors to N per channel
	//    const float redCount = 3.0;
	//    const float greenCount = 3.0;
	//    const float blueCount = 3.0;
	float stepSizeR = 1.0 / redCount;
	float stepSizeG = 1.0 / greenCount;
	float stepSizeB = 1.0 / blueCount;
	col.r = floor(col.r / stepSizeR) * stepSizeR;
	col.g = floor(col.g / stepSizeG) * stepSizeG;
	col.b = floor(col.b / stepSizeB) * stepSizeB;

	// Output to screen
	colorOut = vec4(col, 1.0);

	// 1000 / 16 = 62.5 step size
	// 500 / 62.5 = 8 steps count
	// 520 / 62.5 = floor(8.32) = 8 steps count
	// 8 * 62.5 = 500
}  
