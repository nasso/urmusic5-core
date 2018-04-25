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

uniform sampler2D inputTex;
uniform sampler2D imageTex;
uniform int blendingMode;
uniform float opacity;

in vec2 pass_quad_uv;
in vec2 pass_quad_uv_xform;

out vec4 out_color;

void main() {
	if(pass_quad_uv_xform.x > 0.0 && pass_quad_uv_xform.y > 0.0 && pass_quad_uv_xform.x < 1.0 && pass_quad_uv_xform.y < 1.0)
		out_color = PD_compose(texture(imageTex, pass_quad_uv_xform), texture(inputTex, pass_quad_uv), blendingMode);
	else 
		out_color = texture(inputTex, pass_quad_uv);
}
