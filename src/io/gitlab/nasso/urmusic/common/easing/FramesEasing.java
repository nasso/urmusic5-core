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

public class FramesEasing implements EasingFunction {
	public int number_of_frames;
	
	public FramesEasing(int number_of_frames) {
		this.number_of_frames = number_of_frames;
	}
	
	public float apply(float t, float b, float c, float d) {
		t = t / d;
		
		if(t == 0) return b;
		if(t == 1) return b + c;
		
		int step = (int) (t * this.number_of_frames);
		
		return b + (float) step / (this.number_of_frames - 1) * c;
	}
}
