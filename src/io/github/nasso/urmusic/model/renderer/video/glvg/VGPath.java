package io.github.nasso.urmusic.model.renderer.video.glvg;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2f;

import io.github.nasso.urmusic.common.MathUtils;

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
		
		VGPathBuilder p = new VGPathBuilder();
		
		VGSubPath sub = null;
		VGPoint prevPt = null;
		VGPoint currPt = null;
		VGPoint nextPt = null;
		VGPoint nextNextPt = null;
		Vector2f prevExtend = new Vector2f();
		Vector2f extend = new Vector2f();
		Vector2f nextExtend = new Vector2f();
		Vector2f turnPt = new Vector2f();
		
		for(int i = 0; i < this.subPaths.size(); i++) {
			sub = this.subPaths.get(i);
			
			// Remove the useless stuff
			if(sub.points.size() < 2) continue;
			
			pointsLoop: for(int j = 0, next = 0, nextnext = 0; j < sub.points.size(); j = next) {
				prevPt = currPt;
				currPt = sub.points.get(j);
				
				// Merge every consecutive and aligned lines
				do {
					next++;
					nextnext = next + 1;
					
					if(next > sub.points.size()) nextPt = null;
					else if(next == sub.points.size()) nextPt = sub.closed ? sub.firstPoint() : null;
					else nextPt = sub.points.get(next);

					if(nextnext > sub.points.size()) nextNextPt = null;
					else if(nextnext == sub.points.size()) nextNextPt = sub.closed ? sub.firstPoint() : null;
					else nextNextPt = sub.points.get(nextnext);
					
					// Since there can only be at least 2 points, nextPt will only be null
					// on the last point of a non closed path.
					// So we break when the next next point is null, and quit the main loop when the next is null
					if(nextPt == null) break pointsLoop;
					if(nextNextPt == null) break;
				} while(MathUtils.aligned(currPt.x, currPt.y, nextPt.x, nextPt.y, nextNextPt.x, nextNextPt.y));
				
				// Calc extend vector
				prevExtend.set(extend);
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
				
				// Handle intersections of the edges with the previous line
				if(prevPt != null) {
					float pax, pay, pbx, pby, pcx, pcy, pdx, pdy;
					pax = prevPt.x - prevExtend.x;
					pay = prevPt.y - prevExtend.y;
					pbx = prevPt.x + prevExtend.x;
					pby = prevPt.y + prevExtend.y;
					pcx = currPt.x - prevExtend.x;
					pcy = currPt.y - prevExtend.y;
					pdx = currPt.x + prevExtend.x;
					pdy = currPt.y + prevExtend.y;
					
					if(
						MathUtils.intersection( // Intersection with AC
								pax, pay,
								pcx, pcy,
								ax, ay,
								cx, cy,
								turnPt)) {
						// When found, change A
						ax = turnPt.x;
						ay = turnPt.y;
					} else if(
						MathUtils.intersection( // Intersection with BD
							pbx, pby,
							pdx, pdy,
							bx, by,
							dx, dy,
							turnPt)) {
						// When found, change B
						bx = turnPt.x;
						by = turnPt.y;
					}
				}
				
				// Handle intersections of the edges with the next line
				if(nextNextPt != null) {
					nextExtend.set(nextNextPt.y - nextPt.y, nextPt.x - nextNextPt.x).normalize().mul(lineWidthHalf);
					
					float nax, nay, nbx, nby, ncx, ncy, ndx, ndy;
					nax = nextPt.x - nextExtend.x;
					nay = nextPt.y - nextExtend.y;
					nbx = nextPt.x + nextExtend.x;
					nby = nextPt.y + nextExtend.y;
					ncx = nextNextPt.x - nextExtend.x;
					ncy = nextNextPt.y - nextExtend.y;
					ndx = nextNextPt.x + nextExtend.x;
					ndy = nextNextPt.y + nextExtend.y;
					
					if(
						MathUtils.intersection( // Intersection with AC
								ax, ay,
								cx, cy,
								nax, nay,
								ncx, ncy,
								turnPt)) {
						// When found, change C
						cx = turnPt.x;
						cy = turnPt.y;
					} else if(
						MathUtils.intersection( // Intersection with BD
							bx, by,
							dx, dy,
							nbx, nby,
							ndx, ndy,
							turnPt)) {
						// When found, change D
						dx = turnPt.x;
						dy = turnPt.y;
					}
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
					switch(joins) {
						case BEVEL:
							break;
						case MITER:
							break;
						case ROUND:
							break;
					}
					
					// Trace join
					p.lineTo(ax, ay);
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
				VGPoint first = sub.firstPoint();
				VGPoint last = sub.lastPoint();
				
				float ax, ay, bx, by, cx, cy, dx, dy;
				ax = last.x - extend.x;
				ay = last.y - extend.y;
				bx = last.x + extend.x;
				by = last.y + extend.y;
				cx = first.x - extend.x;
				cy = first.y - extend.y;
				dx = first.x + extend.x;
				dy = first.y + extend.y;
				
				
			}
		}
		
		return p.getPath();
	}
}
