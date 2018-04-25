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
