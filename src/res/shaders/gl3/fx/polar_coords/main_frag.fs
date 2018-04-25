#version 330 core

#define M_PI 3.14159265359
#define M_PI_2 6.28318530718

uniform sampler2D inputTex;
uniform float aspectRatio;
uniform bool modePolarToRect;

in vec2 pass_quad_uv;

out vec4 out_color;

vec2 rectToPolar(vec2 v) {
	vec2 nv = v * 2.0 - 1.0;
	
	nv.x *= aspectRatio;
	
	return vec2((atan(nv.y, nv.x) + M_PI) / M_PI_2, length(nv));
}

vec2 polarToRect(vec2 v) {
	return vec2(cos(v.x * M_PI_2 - M_PI) * v.y / aspectRatio, sin(v.x * M_PI_2 - M_PI) * v.y) * 0.5 + 0.5;
}

void main() {
	out_color = texture(inputTex, modePolarToRect ? polarToRect(pass_quad_uv) : rectToPolar(pass_quad_uv));
}
