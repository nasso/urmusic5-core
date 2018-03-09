#version 330 core

#define PD_BLEND_SRC_OVER 0
#define PD_BLEND_DST_OVER 1
#define PD_BLEND_SRC_IN 2
#define PD_BLEND_DST_IN 3
#define PD_BLEND_SRC_OUT 4
#define PD_BLEND_DST_OUT 5
#define PD_BLEND_SRC_ATOP 6
#define PD_BLEND_DST_ATOP 7
#define PD_BLEND_COPY 8
#define PD_BLEND_ADD 9
#define PD_BLEND_XOR 10

#include <porterDuff>

struct Parameters {
	vec4 color;
	vec4 originInOutRadius;
	vec2 inOutFade;
	
	int blending;
	
	bool invert;
};

#define u_color params.color
#define u_originPoint params.originInOutRadius.xy
#define u_innerRadius params.originInOutRadius.z
#define u_outerRadius params.originInOutRadius.w
#define u_innerFade params.inOutFade.x
#define u_outerFade params.inOutFade.y
#define u_blending params.blending
#define u_invert params.invert

uniform sampler2D inputTex;
uniform vec2 colorSize;
uniform Parameters params;

in vec2 pass_quad_uv;

out vec4 out_color;

float doMask() {
	vec2 pixelCoords = (pass_quad_uv * 1.0 - 0.5) * colorSize;
	
	float dist = distance(pixelCoords, u_originPoint);
	
	if(u_outerRadius == 0.0 && u_outerFade == 0.0) return 0.0;
	
	float outerMask = smoothstep(u_outerRadius + u_outerFade, u_outerRadius, dist);
	if(u_innerRadius == 0) return outerMask;
	
	float innerMask = smoothstep(u_innerRadius - u_innerFade, u_innerRadius, dist);
	
	return outerMask * innerMask;
}

void main() {
	float maskvalue = doMask();
	if(u_invert) maskvalue = 1.0 - maskvalue;
	
	vec4 a = u_color * vec4(1.0, 1.0, 1.0, maskvalue);
	vec4 b = texture(inputTex, pass_quad_uv);
	
	out_color = PD_compose(a, b, u_blending);
}
