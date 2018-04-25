#ifndef PD_BLEND_SRC_OVER

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

#endif

// 
// blends and compose the given colours using the given porter duff operator and normal alpha blending
// see https://www.w3.org/TR/compositing-1/
// 
// Arguments:
// - src = the source color
// - dst = the destination color
// - op = the Porter-Duff operator used. One of: PD_BLEND_SRC_OVER, PD_BLEND_DST_OVER
//                                               PD_BLEND_SRC_IN, PD_BLEND_DST_IN,
//                                               PD_BLEND_SRC_OUT, PD_BLEND_DST_OUT,
//                                               PD_BLEND_SRC_ATOP, PD_BLEND_DST_ATOP,
//                                               PD_BLEND_COPY, PD_BLEND_ADD or PD_BLEND_XOR.
// 
vec4 PD_compose(vec4 src, vec4 dst, int op) {
	vec3 cs = src.rgb;
	vec3 cb = dst.rgb;
	
	float as = src.a;
	float ab = dst.a;
	
	// COMPOSE
	float fa = 0.0, fb = 0.0;
	
	switch(op) {	
		case PD_BLEND_SRC_OVER:
			fa = 1.0;
			fb = 1.0 - as;
			break;
		case PD_BLEND_DST_OVER:
			fa = 1.0 - ab;
			fb = 1.0;
			break;
		case PD_BLEND_SRC_IN:
			fa = ab;
			fb = 0.0;
			break;
		case PD_BLEND_DST_IN:
			fa = 0.0;
			fb = as;
			break;
		case PD_BLEND_SRC_OUT:
			fa = 1.0 - ab;
			fb = 0.0;
			break;
		case PD_BLEND_DST_OUT:
			fa = 0.0;
			fb = 1.0 - as;
			break;
		case PD_BLEND_SRC_ATOP:
			fa = ab;
			fb = 1.0 - as;
			break;
		case PD_BLEND_DST_ATOP:
			fa = 1.0 - ab;
			fb = as;
			break;
		case PD_BLEND_COPY:
			fa = 1.0;
			fb = 0.0;
			break;
		case PD_BLEND_ADD:
			fa = 1.0;
			fb = 1.0;
			break;
		case PD_BLEND_XOR:
			fa = 1.0 - ab;
			fb = 1.0 - as;
			break;
	}
	
	// Divide by alpha because pre-multiplied is bad and we don't want that
	float ao = fa * as + fb * ab;
	vec3 co = (as * fa * cs + ab * fb * cb) / ao;
	
	return vec4(co, ao);
}
