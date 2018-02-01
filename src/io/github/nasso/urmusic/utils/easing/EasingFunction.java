package io.github.nasso.urmusic.utils.easing;

import io.github.nasso.urmusic.utils.easing.penner.easing.Linear;

public interface EasingFunction {
	public static final EasingFunction LINEAR = Linear::easeNone;
	public static final EasingFunction EASE = new CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f);
	public static final EasingFunction EASE_IN = new CubicBezierEasing(0.42f, 0.0f, 1.0f, 1.0f);
	public static final EasingFunction EASE_OUT = new CubicBezierEasing(0.0f, 0.0f, 0.58f, 1.0f);
	public static final EasingFunction EASE_IN_OUT = new CubicBezierEasing(0.42f, 0.0f, 0.58f, 1.0f);
	public static final EasingFunction STEP_START = new StepEasing(1, StepEasing.Direction.START);
	public static final EasingFunction STEP_END = new StepEasing(1, StepEasing.Direction.END);
	
	public float apply(float t, float b, float c, float d);
}
