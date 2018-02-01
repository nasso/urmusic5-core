package io.github.nasso.urmusic.model.project.control;

import java.util.ArrayList;
import java.util.List;

import io.github.nasso.urmusic.utils.easing.EasingFunction;

public abstract class ControlParam<T> {
	private List<KeyFrame<T>> keyFrames = new ArrayList<>();
	
	private T staticValue;
	
	public ControlParam() {
	}
	
	public KeyFrame<T> addKeyFrame(int frame, T val) {
		return this.addKeyFrame(frame, val, EasingFunction.LINEAR);
	}
	
	public KeyFrame<T> addKeyFrame(int frame, T val, EasingFunction func) {
		int i;
		for(i = 0; i < this.keyFrames.size(); i++) {
			if(this.keyFrames.get(i).getFrame() > frame) break;
		}
		
		KeyFrame<T> kf = new KeyFrame<T>(frame, val, func);
		this.keyFrames.add(i, kf);
		
		return kf;
	}
	
	/**
	 * Sets the value of the control at the specified frame.<br>
	 * If there's no key frames for this control, the frame index given won't matter.
	 * 
	 * @param val
	 * @param frame
	 */
	public void setValue(T val, int frame) {
		if(this.keyFrames.isEmpty()) {
			this.staticValue = val;
			
			return;
		}
		
		this.addKeyFrame(frame, val);
	}
	
	public T getValue(int frame) {
		if(this.keyFrames.isEmpty()) {
			return this.staticValue;
		}
		
		int i;
		for(i = 0; i < this.keyFrames.size(); i++) {
			if(this.keyFrames.get(i).getFrame() > frame) break;
		}
		
		if(i == 0) return this.keyFrames.get(i).getValue();
		if(i == this.keyFrames.size()) return this.keyFrames.get(this.keyFrames.size() - 1).getValue();
		
		KeyFrame<T> previous, next;
		previous = this.keyFrames.get(i - 1);
		next = this.keyFrames.get(i);
		return this.ramp(previous.getValue(), next.getValue(), next.getInterpolationMethod().apply(
			frame - previous.getFrame(),			// position
			0,										// beginning value
			1,										// change (aka: beginning value + change = end value)
			next.getFrame() - previous.getFrame()	// duration
		));
	}
	
	/**
	 * Linearly interpolates between <code>s</code> and <code>e</code>, <code>t</code> being the factor (usually in the range 0.0..1.0 but not limited to).<br>
	 * For <code>t == 0.0</code>, <code>s</code> should be returned, and for <code>t == 1.0</code>, <code>e</code> should be returned.
	 * 
	 * @param s
	 * @param e
	 * @param t
	 * @return
	 */
	public abstract T ramp(T s, T e, float t);
}
