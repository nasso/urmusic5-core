#version 330 core

uniform vec2 surfaceSize;

in vec2 position;

void main() {
	gl_Position = vec4(position / surfaceSize * 2.0, 0.0, 1.0);
}
