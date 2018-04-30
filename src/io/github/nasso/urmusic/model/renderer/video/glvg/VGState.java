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

import org.joml.Matrix3f;

import io.github.nasso.urmusic.common.MutableRGBA32;

class VGState implements Cloneable {
	Matrix3f transform;
	VGPathBuilder path;
	float lineWidth, miterLimit, globalAlpha;
	VGLineJoin lineJoins;
	VGLineCap lineCaps;
	MutableRGBA32 fillStyle, strokeStyle;
	
	private VGState(boolean noinit) {
		
	}
	
	public VGState() {
		 this.transform = new Matrix3f();
		 this.path = new VGPathBuilder();
		 
		 this.lineWidth = 1.0f;
		 this.miterLimit = 10.0f;
		 this.globalAlpha = 1.0f;
		 this.lineJoins = VGLineJoin.MITER;
		 this.lineCaps = VGLineCap.BUTT;
		 
		 this.fillStyle = new MutableRGBA32(0x000000FF);
		 this.strokeStyle = new MutableRGBA32(0x000000FF);
	}
	
	public VGState clone() {
		VGState copy = new VGState(true);
		
		copy.transform = new Matrix3f(this.transform);
		copy.path = this.path.clone();
		copy.lineWidth = this.lineWidth;
		copy.miterLimit = this.miterLimit;
		copy.globalAlpha = this.globalAlpha;
		copy.lineJoins = this.lineJoins;
		copy.lineCaps = this.lineCaps;
		copy.fillStyle = this.fillStyle.clone();
		copy.strokeStyle = this.strokeStyle.clone();
		
		return copy;
	}
}
