#version 330 core

uniform vec2 scale;

in vec2 position_quad;

out vec2 pass_quad_uv;

void main() {
	pass_quad_uv = position_quad * 0.5 + 0.5;
	
	gl_Position = vec4(position_quad * scale, 0.0, 1.0);
}
