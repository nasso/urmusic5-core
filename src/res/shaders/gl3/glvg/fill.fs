#version 330 core

uniform vec4 fillRGBA;

out vec4 out_color;

void main() {
	out_color = fillRGBA;
}
