package io.github.nasso.urmusic.model.renderer.video.glvg;

class VGPoint {
	final float x;
	final float y;
	final boolean ghost; // True when no cap/join should be computed for this point
	
	public VGPoint(float x, float y) {
		this(x, y, false);
	}
	
	public VGPoint(float x, float y, boolean ghost) {
		this.x = x;
		this.y = y;
		this.ghost = ghost;
	}
}
