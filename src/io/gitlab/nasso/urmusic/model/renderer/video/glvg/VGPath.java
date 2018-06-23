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

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2f;

import io.gitlab.nasso.urmusic.common.MathUtils;

class VGPath implements Cloneable {
	List<VGSubPath> subPaths = new ArrayList<>();
	
	public VGPath clone() {
		VGPath copy = new VGPath();
		
		for(VGSubPath sub : this.subPaths) {
			copy.subPaths.add(sub.clone());
		}
		
		return copy;
	}
	
	public VGSubPath currentSubPath() {
		return this.subPaths.isEmpty() ? null : this.subPaths.get(this.subPaths.size() - 1);
	}
	
	/**
	 * Returns a new path representing the stroke of this path
	 */
	public VGPath trace(float lineWidth, VGLineCap caps, VGLineJoin joins) {
		float lineWidthHalf = lineWidth * 0.5f;
		
		float fax = 0;
		float fay = 0;
		float fbx = 0;
		float fby = 0;
		
		VGPathBuilder p = new VGPathBuilder();
		
		VGSubPath sub = null;
		VGPoint currPt = null;
		VGPoint nextPt = null;
		VGPoint nextNextPt = null;
		Vector2f extend = new Vector2f();
		
		for(int i = 0; i < this.subPaths.size(); i++) {
			sub = this.subPaths.get(i);
			
			// Remove the useless stuff
			if(sub.points.size() < 2) continue;
			
			currPt = !sub.closed ? null : sub.points.get(sub.points.size() - 2);
			nextPt = null;
			nextNextPt = null;
			if(sub.closed) extend.set(sub.lastPoint().y - currPt.y, currPt.x - sub.lastPoint().x).normalize().mul(lineWidthHalf);
			else extend.zero();
			
			pointsLoop: for(int j = 0, next = 0, nextnext = 0; j < sub.points.size(); j = next) {
				currPt = sub.points.get(j);
				
				// Merge every consecutive and aligned lines
				do {
					next++;
					nextnext = next + 1;
					
					if(next >= sub.points.size() && !sub.closed) nextPt = null;
					else nextPt = sub.points.get(next % sub.points.size());

					if(nextnext >= sub.points.size() && !sub.closed) nextNextPt = null;
					else nextNextPt = sub.points.get(nextnext % sub.points.size());
					
					// Since there can only be at least 2 points, nextPt will only be null
					// on the last point of a non closed path.
					// So we break when the next next point is null, and quit the main loop when the next is null
					if(nextPt == null) break pointsLoop;
					if(nextNextPt == null) break;
				} while(MathUtils.alignedInOrder(currPt.x, currPt.y, nextPt.x, nextPt.y, nextNextPt.x, nextNextPt.y));
				
				// Calc extend vector
				extend.set(nextPt.y - currPt.y, currPt.x - nextPt.x).normalize().mul(lineWidthHalf);
				
				// Draw line like that
				/*
				  B----------------------------------D  
				  |"""""-----_____                   |  
				--o---------------====---------------o--
				  |                   """""-----_____|  
				  A----------------------------------C  
				*/
				
				// Calc point coords for a rectangle line (basic)
				float ax, ay, bx, by, cx, cy, dx, dy;
				ax = currPt.x - extend.x;
				ay = currPt.y - extend.y;
				bx = currPt.x + extend.x;
				by = currPt.y + extend.y;
				cx = nextPt.x - extend.x;
				cy = nextPt.y - extend.y;
				dx = nextPt.x + extend.x;
				dy = nextPt.y + extend.y;
				
				if(j == 0) {
					fax = ax;
					fay = ay;
					fbx = bx;
					fby = by;
				}
				
				// Caps and joins first
				if(j == 0 && !sub.closed) {
					switch(caps) {
						case BUTT:
							p.moveTo(ax, ay);
							p.lineTo(bx, by);
							break;
						case ROUND:
							// Draw arc
							float startAngle = (float) Math.atan2(-extend.x, extend.y);
							float angOffset, cs, sn;
							
							int steps = GLVG.getArcSteps(lineWidthHalf) / 2; // Half the step cuz half the circle
							
							for(int a = 0; a < steps; a++) {
								angOffset = (float) a / steps * MathUtils.HALF_PI;
								
								cs = (float) Math.cos(startAngle - angOffset) * lineWidthHalf;
								sn = (float) Math.sin(startAngle - angOffset) * lineWidthHalf;
								
								if(a == 0) p.moveTo(currPt.x + cs, currPt.y + sn);
								else p.lineTo(currPt.x + cs, currPt.y + sn);
								
								cs = (float) Math.cos(startAngle + angOffset) * lineWidthHalf;
								sn = (float) Math.sin(startAngle + angOffset) * lineWidthHalf;
								
								p.lineTo(currPt.x + cs, currPt.y + sn);
							}
							
							p.lineTo(ax, ay);
							p.lineTo(bx, by);
							break;
						case SQUARE:
							p.moveTo(ax + extend.y, ay - extend.x);
							p.lineTo(bx + extend.y, by - extend.x);
							break;
					}
				} else {
					// TODO joins
					switch(joins) {
						case BEVEL:
							break;
						case MITER:
							break;
						case ROUND:
							break;
					}
					
					// Trace join
					if(j == 0) p.moveTo(ax, ay);
					else p.lineTo(ax, ay);
					p.lineTo(bx, by);
				}
				
				// Trace the rest of the line
				p.lineTo(cx, cy);
				p.lineTo(dx, dy);
				
				// End cap if the end has been reached
				if(next == sub.points.size() - 1) {
					if(!sub.closed) {
						switch(caps) {
							case BUTT:
								p.moveTo(ax, ay);
								p.lineTo(bx, by);
								break;
							case ROUND:
								// Draw arc
								float startAngle = (float) Math.atan2(extend.x, -extend.y);
								float angOffset, cs, sn;
								
								int steps = GLVG.getArcSteps(lineWidthHalf) / 2; // Half the step cuz half the circle
								
								for(int a = 0; a <= steps; a++) {
									angOffset = (1.0f - (float) a / steps) * MathUtils.HALF_PI;
									
									cs = (float) Math.cos(startAngle - angOffset) * lineWidthHalf;
									sn = (float) Math.sin(startAngle - angOffset) * lineWidthHalf;
									
									if(a == 0) p.moveTo(nextPt.x + cs, nextPt.y + sn);
									else p.lineTo(nextPt.x + cs, nextPt.y + sn);
									
									cs = (float) Math.cos(startAngle + angOffset) * lineWidthHalf;
									sn = (float) Math.sin(startAngle + angOffset) * lineWidthHalf;
									
									p.lineTo(nextPt.x + cs, nextPt.y + sn);
								}
								break;
							case SQUARE:
								p.moveTo(ax + extend.y, ay - extend.x);
								p.lineTo(bx + extend.y, by - extend.x);
								break;
						}
					}
				}
			}
			
			// We must join to the first point if the path is closed
			if(sub.closed) {
				p.lineTo(fax, fay);
				p.lineTo(fbx, fby);
			}
		}
		
		return p.getPath();
	}
}
