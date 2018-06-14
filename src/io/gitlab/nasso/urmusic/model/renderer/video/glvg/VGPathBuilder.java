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
package io.gitlab.nasso.urmusic.model.renderer.video.glvg;

class VGPathBuilder implements Cloneable, VGPathMethods {
	private VGPath currentPath = new VGPath();
	
	public VGPathBuilder clone() {
		VGPathBuilder copy = new VGPathBuilder();
		
		copy.currentPath = this.currentPath.clone();
		
		return copy;
	}
	
	public void clear() {
		this.currentPath = new VGPath();
	}
	
	public VGPath getPath() {
		return this.currentPath;
	}
	
	public void moveTo(float x, float y) {
		VGSubPath subPath = new VGSubPath();
		subPath.points.add(new VGPoint(x, y));
		
		this.currentPath.subPaths.add(subPath);
	}
	
	public void closePath() {
		if(!this.currentPath.subPaths.isEmpty()) {
			this.currentPath.currentSubPath().closed = true;
			
			VGSubPath sub = new VGSubPath();
			sub.points.add(this.currentPath.currentSubPath().firstPoint());
			
			this.currentPath.subPaths.add(sub);
		}
	}
	
	public void lineTo(float x, float y) {
		this.ensureSubPath(x, y);
		
		this.currentPath.currentSubPath().points.add(new VGPoint(x, y));
	}
	
	private void ensureSubPath(float x, float y) {
		if(this.currentPath.subPaths.isEmpty())
			this.moveTo(x, y);
	}
}
