package io.github.nasso.urmusic.utils.easing;

public class FramesEasing implements EasingFunction {
	public final int number_of_frames;
	
	public FramesEasing(int number_of_frames) {
		this.number_of_frames = number_of_frames;
	}
	
	public float apply(float t, float b, float c, float d) {
		t = t / d;
		
		if(t == 0) return b;
		if(t == 1) return b + c;
		
		int step = (int) (t * this.number_of_frames);
		
		return b + (float) step / (this.number_of_frames - 1) * c;
	}
}
