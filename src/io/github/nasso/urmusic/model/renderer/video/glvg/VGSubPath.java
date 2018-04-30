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
package io.github.nasso.urmusic.model.renderer.video.glvg;

import java.util.ArrayList;
import java.util.List;

class VGSubPath implements Cloneable {
	List<VGPoint> points = new ArrayList<>();
	boolean closed = false;
	
	public VGPoint firstPoint() {
		return this.points.isEmpty() ? null : this.points.get(0);
	}
	
	public VGPoint lastPoint() {
		return this.points.isEmpty() ? null : this.points.get(this.points.size() - 1);
	}
	
	public VGSubPath clone() {
		VGSubPath copy = new VGSubPath();
		copy.points.addAll(this.points);
		copy.closed = this.closed;
		
		return copy;
	}
}
