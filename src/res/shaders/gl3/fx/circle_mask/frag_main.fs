#version 330 core

#define BLEND_SRC_OVER 0
#define BLEND_DST_OVER 1
#define BLEND_SRC_IN 2
#define BLEND_DST_IN 3
#define BLEND_SRC_OUT 4
#define BLEND_DST_OUT 5
#define BLEND_SRC_ATOP 6
#define BLEND_DST_ATOP 7
#define BLEND_COPY 8
#define BLEND_ADD 9
#define BLEND_XOR 10

// blends and compose the given colours using the given porter duff operator and normal alpha blending
// see https://www.w3.org/TR/compositing-1/
vec4 porterDuff(vec4 src, vec4 dst, int op) {
	// BLEND
	vec4 cs = src;
	vec4 cb = dst;
	
	float as = cs.a;
	float ab = cb.a;
	
	// COMPOSE
	float fa = 0.0, fb = 0.0;
	
	switch(op) {	
		case BLEND_SRC_OVER:
			fa = 1.0;
			fb = 1.0 - as;
			break;
		case BLEND_DST_OVER:
			fa = 1.0 - ab;
			fb = 1.0;
			break;
		case BLEND_SRC_IN:
			fa = ab;
			fb = 0.0;
			break;
		case BLEND_DST_IN:
			fa = 0.0;
			fb = as;
			break;
		case BLEND_SRC_OUT:
			fa = 1.0 - ab;
			fb = 0.0;
			break;
		case BLEND_DST_OUT:
			fa = 0.0;
			fb = 1.0 - as;
			break;
		case BLEND_SRC_ATOP:
			fa = ab;
			fb = 1.0 - as;
			break;
		case BLEND_DST_ATOP:
			fa = 1.0 - ab;
			fb = as;
			break;
		case BLEND_COPY:
			fa = 1.0;
			fb = 0.0;
			break;
		case BLEND_ADD:
			fa = 1.0;
			fb = 1.0;
			break;
		case BLEND_XOR:
			fa = 1.0 - ab;
			fb = 1.0 - as;
			break;
	}
	
	return as * fa * cs + ab * fb * cb;
}

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

float __smoothstep(float edge0, float edge1, float x) {
	float t = clamp((x - edge0) / (edge1 - edge0), 0.0, 1.0);
    return t * t * (3.0 - 2.0 * t);
}

float doMask() {
	vec2 pixelCoords = (pass_quad_uv * 1.0 - 0.5) * colorSize;
	
	float dist = distance(pixelCoords, u_originPoint);
	
	if(u_outerRadius == 0.0 && u_outerFade == 0.0) return 0.0;
	
	float outerMask = __smoothstep(u_outerRadius + u_outerFade, u_outerRadius, dist);
	if(u_innerRadius == 0) return outerMask;
	
	float innerMask = __smoothstep(u_innerRadius - u_innerFade, u_innerRadius, dist);
	
	return outerMask * innerMask;
}

void main() {
	float maskvalue = doMask();
	if(u_invert) maskvalue = 1.0 - maskvalue;
	
	vec4 a = u_color * vec4(1.0, 1.0, 1.0, maskvalue);
	vec4 b = texture(inputTex, pass_quad_uv);
	
	out_color = porterDuff(a, b, u_blending);
}
