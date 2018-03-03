package io.github.nasso.urmusic.model.renderer.video.glvg;

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
