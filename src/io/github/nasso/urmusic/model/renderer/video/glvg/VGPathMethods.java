package io.github.nasso.urmusic.model.renderer.video.glvg;

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
}
