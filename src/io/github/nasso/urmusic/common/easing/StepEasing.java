package io.github.nasso.urmusic.common.easing;

public class StepEasing implements EasingFunction {
	public static enum Direction {
		START, END
	}
	
	public int number_of_steps;
	public Direction direction;
	
	public StepEasing(int number_of_steps) {
		this(number_of_steps, Direction.END);
	}
	
	public StepEasing(int number_of_steps, Direction dir) {
		this.number_of_steps = number_of_steps;
		this.direction = dir;
	}
	
	public float apply(float t, float b, float c, float d) {
		t = t / d;
		
		if(t == 0) return b;
		if(t == 1) return b + c;
		
		int step = (int) (t * this.number_of_steps);
		
		if(this.direction == Direction.START) step++;
		
		return b + (float) step / this.number_of_steps * c;
	}
}
