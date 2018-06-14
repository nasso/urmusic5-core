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

interface VGPathMethods {
	public void moveTo(float x, float y);
	public void closePath();
	
	public void lineTo(float x, float y);
	
	public default void rect(float x, float y, float w, float h) {
		moveTo(x, y);
		lineTo(x + w, y);
		lineTo(x + w, y + h);
		lineTo(x, y + h);
		closePath();
	}
	
	public default void oval(float x, float y, float w, float h) {
		float rx = w / 2.0f;
		float ry = h / 2.0f;
		
		int steps = GLVG.getArcSteps(Math.max(rx, ry));
		for(int i = 0; i <= steps; i++) {
			float p = (float) i / steps;
			
			float cs = (float) Math.cos(p * Math.PI * 2);
			float sn = (float) Math.sin(p * Math.PI * 2);
			
			cs *= rx;
			sn *= ry;
			
			lineTo(x + cs, y + sn);
		}
	}
}
