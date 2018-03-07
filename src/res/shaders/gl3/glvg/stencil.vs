#version 330 core

uniform mat3 xform;
uniform vec2 surfaceSize;

in vec2 position;

void main() {
	vec3 xformed = xform * vec3(position, 1.0);
	xformed /= xformed.z;
	
	gl_Position = vec4(xformed.xy / surfaceSize * 2.0, 0.0, 1.0);
}
