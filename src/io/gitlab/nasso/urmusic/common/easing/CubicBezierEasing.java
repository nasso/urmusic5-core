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
package io.gitlab.nasso.urmusic.common.easing;

import static io.gitlab.nasso.urmusic.common.MathUtils.*;

public class CubicBezierEasing implements EasingFunction {
	public static final int PRECISION = 30;
	
	public float x1, y1, x2, y2;
	
	public CubicBezierEasing(float x1, float y1, float x2, float y2) {
		this.x1 = clamp(x1, 0, 1);
		this.y1 = y1;
		this.x2 = clamp(x2, 0, 1);
		this.y2 = y2;
	}
			
	private float xForT(float t) {
		return cubicBezier(0, this.x1, this.x2, 1, t);
	}
	
	private float yForT(float t) {
		return cubicBezier(0, this.y1, this.y2, 1, t);
	}
	
	private float tForX(float x) {
		float mint = 0, maxt = 1;
		
		// Binary search
		for(int i = 0; i < PRECISION; i++) {
			float guesst = (mint + maxt) / 2.0f;
			float guessx = this.xForT(guesst);
			
			if(x < guessx) maxt = guesst;
			else if(x > guessx) mint = guesst;
			else return guesst;
		}
		
		return (mint + maxt) / 2.0f;
	}
	
	public float apply(float t, float b, float c, float d) {
		t = t / d;
		if(t == 0) return b;
		if(t == 1) return b + c;
		
		return b + this.yForT(this.tForX(t)) * c;
	}
}
