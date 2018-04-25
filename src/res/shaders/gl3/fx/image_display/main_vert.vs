#version 330 core

uniform mat4 xform;

in vec2 position_quad;

out vec2 pass_quad_uv;
out vec2 pass_quad_uv_xform;

void main() {
	pass_quad_uv = position_quad * 0.5 + 0.5;
	pass_quad_uv_xform = (inverse(xform) * vec4(position_quad, 0.0, 1.0)).xy * 0.5 + 0.5;
	
	gl_Position = vec4(position_quad, 0.0, 1.0);
}
