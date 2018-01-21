#version 330 core

uniform sampler2D color;

in vec2 pass_quad_uv;

out vec4 out_color;

void main() {
	out_color = texture(color, pass_quad_uv);
}
