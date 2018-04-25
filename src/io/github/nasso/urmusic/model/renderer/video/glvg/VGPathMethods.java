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
