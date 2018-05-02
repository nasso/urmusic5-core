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

uniform sampler2D inputTex;
uniform vec4 edge;

in vec2 pass_quad_uv;

out vec4 out_color;

void main() {
	float A = edge.w - edge.y;
	float B = edge.x - edge.z;
	float C = -A * edge.x - B * edge.y;
	
	float M = sqrt(A * A + B * B);
	float Ap = A / M;
	float Bp = B / M;
	float Cp = C / M;
	
	float D = Ap * pass_quad_uv.x + Bp * pass_quad_uv.y + Cp;
	
	if(D > 0) {
		out_color = texture(inputTex, vec2(pass_quad_uv.x - 2 * Ap * D, pass_quad_uv.y - 2 * Bp * D));
	} else {
		out_color = texture(inputTex, pass_quad_uv);
	}
}
