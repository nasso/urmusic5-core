#version 330 core

#include <porterDuff>

uniform sampler2D trackInput;
uniform sampler2D compOutput;

in vec2 pass_quad_uv;

out vec4 out_color;

void main() {
	out_color = PD_compose(texture(trackInput, pass_quad_uv), texture(compOutput, pass_quad_uv), PD_BLEND_SRC_OVER);
}
