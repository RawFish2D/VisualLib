#version 330 core

in vec2 vTexCoord;
in vec4 vColor;

uniform float u_radius;
uniform vec2 u_dimensions;

out vec4 colorOut;

//void main(void) {
//    vec2 coords = vTexCoord * u_dimensions;
//    if (length(coords - vec2(0)) < u_radius ||
//    length(coords - vec2(0, u_dimensions.y)) < u_radius ||
//    length(coords - vec2(u_dimensions.x, 0)) < u_radius ||
//    length(coords - u_dimensions) < u_radius) {
//        discard;
//    }
//    colorOut = vColor;
//}

float calcDistance() {
	vec2 coords = abs(vTexCoord) * (u_dimensions + u_radius);
	vec2 delta = max(coords - u_dimensions, 0.0);
	return length(delta);
}

void main() {
	float dist = calcDistance();
	if (dist > u_radius) discard;
	colorOut = vColor;
}