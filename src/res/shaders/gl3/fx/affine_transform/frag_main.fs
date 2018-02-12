#version 330 core

uniform sampler2D inputTex;
uniform float opacity;

in vec2 pass_quad_uv;

out vec4 out_color;

void main() {
	out_color = texture(inputTex, pass_quad_uv) * vec4(1.0, 1.0, 1.0, opacity);
}
