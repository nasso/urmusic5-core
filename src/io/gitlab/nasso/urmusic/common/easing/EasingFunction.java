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
package io.gitlab.nasso.urmusic.common.easing;

import static io.gitlab.nasso.urmusic.common.MathUtils.*;

public interface EasingFunction {
	public static final EasingFunction EASE = new CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f);
	public static final EasingFunction EASE_IN = new CubicBezierEasing(0.42f, 0.0f, 1.0f, 1.0f);
	public static final EasingFunction EASE_OUT = new CubicBezierEasing(0.0f, 0.0f, 0.58f, 1.0f);
	public static final EasingFunction EASE_IN_OUT = new CubicBezierEasing(0.42f, 0.0f, 0.58f, 1.0f);
	public static final EasingFunction STEP_START = new StepEasing(1, StepEasing.Direction.START);
	public static final EasingFunction STEP_END = new StepEasing(1, StepEasing.Direction.END);
	
	// Easings adapted from penner.easing (see https://github.com/jesusgollonet/processing-penner-easing)
	public static final EasingFunction LINEAR = (t, b, c, d) -> (c*t/d + b);
	
	public static final EasingFunction EASE_IN_SINE = (t, b, c, d) -> (-c * cosf(t/d * (PI/2)) + c + b);
	public static final EasingFunction EASE_OUT_SINE = (t, b, c, d) -> (c * sinf(t/d * (PI/2)) + b);
	public static final EasingFunction EASE_IN_OUT_SINE = (t, b, c, d) -> (-c/2 * (cosf(PI*t/d) - 1) + b);
	
	public static final EasingFunction EASE_IN_QUAD = (t, b, c, d) -> (c*(t/=d)*t + b);
	public static final EasingFunction EASE_OUT_QUAD = (t, b, c, d) -> (-c *(t/=d)*(t-2) + b);
	public static final EasingFunction EASE_IN_OUT_QUAD = (t, b, c, d) -> (((t/=d/2) < 1) ? c/2*t*t + b : -c/2 * ((--t)*(t-2) - 1) + b);
	
	public static final EasingFunction EASE_IN_CUBIC = (t, b, c, d) -> (c*(t/=d)*t*t + b);
	public static final EasingFunction EASE_OUT_CUBIC = (t, b, c, d) -> (c*((t=t/d-1)*t*t + 1) + b);
	public static final EasingFunction EASE_IN_OUT_CUBIC = (t, b, c, d) -> (((t/=d/2) < 1) ? c/2*t*t*t + b : c/2*((t-=2)*t*t + 2) + b);
	
	public static final EasingFunction EASE_IN_QUART = (t, b, c, d) -> (c*(t/=d)*t*t*t + b);
	public static final EasingFunction EASE_OUT_QUART = (t, b, c, d) -> (-c * ((t=t/d-1)*t*t*t - 1) + b);
	public static final EasingFunction EASE_IN_OUT_QUART = (t, b, c, d) -> (((t/=d/2) < 1) ? c/2*t*t*t*t + b : -c/2 * ((t-=2)*t*t*t - 2) + b);
	
	public static final EasingFunction EASE_IN_QUINT = (t, b, c, d) -> (c*(t/=d)*t*t*t*t + b);
	public static final EasingFunction EASE_OUT_QUINT = (t, b, c, d) -> (c*((t=t/d-1)*t*t*t*t + 1) + b);
	public static final EasingFunction EASE_IN_OUT_QUINT = (t, b, c, d) -> ((((t/=d/2) < 1)) ? c/2*t*t*t*t*t + b : c/2*((t-=2)*t*t*t*t + 2) + b);
	
	public static final EasingFunction EASE_IN_EXPO = (t, b, c, d) -> ((t==0) ? b : c * powf(2, 10 * (t/d - 1)) + b);
	public static final EasingFunction EASE_OUT_EXPO = (t, b, c, d) -> ((t==d) ? b+c : c * (-powf(2, -10 * t/d) + 1) + b);
	public static final EasingFunction EASE_IN_OUT_EXPO = (t, b, c, d) -> ((t==0) ? b : (t==d) ? b+c : ((t/=d/2) < 1) ? c/2 * powf(2, 10 * (t - 1)) + b : c/2 * (-powf(2, -10 * --t) + 2) + b);
	
	public static final EasingFunction EASE_IN_CIRC = (t, b, c, d) -> (-c * (sqrtf(1 - (t/=d)*t) - 1) + b);
	public static final EasingFunction EASE_OUT_CIRC = (t, b, c, d) -> (c * sqrtf(1 - (t=t/d-1)*t) + b);
	public static final EasingFunction EASE_IN_OUT_CIRC = (t, b, c, d) -> (((t/=d/2) < 1) ? -c/2 * (sqrtf(1 - t*t) - 1) + b : c/2 * (sqrtf(1 - (t-=2)*t) + 1) + b);
	
	public static final EasingFunction EASE_IN_BACK = (t, b, c, d) -> (c*(t/=d)*t*(2.70158f*t - 1.70158f) + b);
	public static final EasingFunction EASE_OUT_BACK = (t, b, c, d) -> (c*((t=t/d-1)*t*(2.70158f*t + 1.70158f) + 1) + b);
	public static final EasingFunction EASE_IN_OUT_BACK = (t, b, c, d) -> (((t/=d/2) < 1) ? c/2*(t*t*(3.5949095f*t - 2.5949095f)) + b : c/2*((t-=2)*t*(3.5949095f*t + 2.5949095f) + 2) + b);
	
	public static final EasingFunction EASE_IN_ELASTIC = (t, b, c, d) -> ((t==0) ? b : ((t/=d)==1) ? b+c : -(c*powf(2,10*(t-=1)) * sinf(((t-.075f)*d)*(2*PI)/(d*.3f))) + b);
	public static final EasingFunction EASE_OUT_ELASTIC = (t, b, c, d) -> ((t==0) ? b : ((t/=d)==1) ? b+c : (c*powf(2,-10*t) * sinf(((t-.075f)*d)*(2*PI)/(d*.3f)) + c + b));
	public static final EasingFunction EASE_IN_OUT_ELASTIC = (t, b, c, d) -> ((t==0) ? b : ((t/=d/2)==2) ? b+c : (t < 1) ? -.5f*(c*powf(2,10*(t-=1)) * sinf( d*(t-.1125f)*(2*PI)/(d*.45f) )) + b : c*powf(2,-10*(t-=1)) * sinf( d*(t-.1125f)*(2*PI)/(d*.45f) )*.5f + c + b);
	
	public static final EasingFunction EASE_OUT_BOUNCE = (t, b, c, d) -> (((t/=d) < (1/2.75f)) ? c*(7.5625f*t*t) + b : (t < (2/2.75f)) ? c*(7.5625f*(t-=(1.5f/2.75f))*t + .75f) + b : (t < (2.5/2.75)) ? c*(7.5625f*(t-=(2.25f/2.75f))*t + .9375f) + b : c*(7.5625f*(t-=(2.625f/2.75f))*t + .984375f) + b);
	public static final EasingFunction EASE_IN_BOUNCE = (t, b, c, d) -> (c - EASE_OUT_BOUNCE.apply(d - t, 0, c, d) + b);
	public static final EasingFunction EASE_IN_OUT_BOUNCE = (t, b, c, d) -> ((t < d/2) ? EASE_IN_BOUNCE.apply(t*2, 0, c, d) * .5f + b : EASE_OUT_BOUNCE.apply(t*2-d, 0, c, d) * .5f + c*.5f + b);
	
	public float apply(float t, float b, float c, float d);
}
