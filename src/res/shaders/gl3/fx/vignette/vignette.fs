#version 330 core

uniform sampler2D inputTex;
uniform vec2 colorSize;
uniform vec4 outerColor;
uniform vec4 innerColor;
uniform vec4 parameters; // x,y : origin | z : dist | w : penumbra

#define u_originPoint parameters.xy
#define u_dist parameters.z
#define u_penumbra parameters.w

in vec2 pass_quad_uv;

out vec4 out_color;

void main() {
	vec2 pixelCoords = pass_quad_uv * colorSize;
	
	float minDist = u_dist - u_penumbra * 0.5;
	float maxDist = u_dist + u_penumbra * 0.5;
	
	float dist = distance(pixelCoords, u_originPoint);
	
	vec4 interColor = mix(innerColor, outerColor, smoothstep(minDist, maxDist, dist));
	
	out_color = mix(
		texture(inputTex, pass_quad_uv).xyzw,
		vec4(interColor.xyz, 1.0),
		interColor.a
	);
}
