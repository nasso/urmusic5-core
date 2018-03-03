#version 330 core

in vec2 position_quad;

void main() {
	gl_Position = vec4(position_quad, 0.0, 1.0);
}
