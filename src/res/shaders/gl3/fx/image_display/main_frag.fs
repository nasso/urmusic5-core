/*******************************************************************************
 * urmusic - The Free and Open Source Music Visualizer Tool
 * Copyright (C) 2018  nasso (https://github.com/nasso)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * Contact "nasso": nassomails -at- gmail dot com
 ******************************************************************************/
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
	if(pass_quad_uv_xform.x > 0.0 && pass_quad_uv_xform.y > 0.0 && pass_quad_uv_xform.x < 1.0 && pass_quad_uv_xform.y < 1.0) {
		out_color = PD_compose(texture(imageTex, pass_quad_uv_xform) * vec4(1.0, 1.0, 1.0, opacity), texture(inputTex, pass_quad_uv), blendingMode);
	} else {
		out_color = texture(inputTex, pass_quad_uv);
	}
}
