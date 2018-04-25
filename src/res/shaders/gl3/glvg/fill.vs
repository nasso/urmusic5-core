#version 330 core

uniform vec4 rectXYWH;

#define u_pos rectXYWH.xy
#define u_size rectXYWH.zw

in vec2 position_quad;

void main() {
	gl_Position = vec4(position_quad * u_size + u_pos, 0.0, 1.0);
}
