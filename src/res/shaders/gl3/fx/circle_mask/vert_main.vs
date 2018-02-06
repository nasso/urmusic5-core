#version 330 core

in vec2 position_quad;

out vec2 pass_quad_uv;

void main() {
	pass_quad_uv = position_quad * 0.5 + 0.5;
	
	gl_Position = vec4(position_quad, 0.0, 1.0);
}
