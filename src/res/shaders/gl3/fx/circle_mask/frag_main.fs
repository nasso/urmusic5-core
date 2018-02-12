#version 330 core

struct Parameters {
	vec4 color;
	vec4 originInOutRadius;
	vec2 inOutFade;
	
	bool invert;
};

#define u_color params.color
#define u_originPoint params.originInOutRadius.xy
#define u_innerRadius params.originInOutRadius.z
#define u_outerRadius params.originInOutRadius.w
#define u_innerFade params.inOutFade.x
#define u_outerFade params.inOutFade.y
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
	maskvalue *= u_color.a;
	
	if(maskvalue >= 1.0) out_color = u_color;
	else {
		vec4 texValue = texture(inputTex, pass_quad_uv);
		out_color = vec4(mix(texValue.xyz, u_color.xyz, maskvalue), clamp(texValue.w + maskvalue, 0.0, 1.0));
	}
}
