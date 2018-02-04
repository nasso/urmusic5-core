package io.github.nasso.urmusic.model.project.control;

import java.util.ArrayList;
import java.util.List;

import io.github.nasso.urmusic.model.event.ControlParamListener;
import io.github.nasso.urmusic.utils.easing.EasingFunction;

public abstract class ControlParam<T> {
	private List<ControlParamListener<T>> listeners = new ArrayList<>();
	
	private List<KeyFrame<T>> keyFrames = new ArrayList<>();
	private String name;
	
	public ControlParam(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public KeyFrame<T> addKeyFrame(int frame) {
		return this.addKeyFrame(frame, this.getValue(frame));
	}
	
	public KeyFrame<T> addKeyFrame(int frame, T val) {
		return this.addKeyFrame(frame, val, EasingFunction.LINEAR);
	}
	
	public KeyFrame<T> addKeyFrame(int frame, T val, EasingFunction func) {
		int i;
		for(i = 0; i < this.keyFrames.size(); i++) {
			if(this.keyFrames.get(i).getFrame() > frame) break;
		}
		
		KeyFrame<T> kf = new KeyFrame<T>(frame, this.cloneValue(val), func);
		this.keyFrames.add(i, kf);
		
		this.notifyKeyFrameAdded(kf);
		
		return kf;
	}
	
	public KeyFrame<T> getKeyFrameAt(int frame) {
		for(KeyFrame<T> kf : this.keyFrames) {
			if(kf.getFrame() == frame) return kf;
		}
		
		return null;
	}
	
	public void removeKeyFrame(KeyFrame<T> kf) {
		this.keyFrames.remove(kf);
		this.notifyKeyFrameRemoved(kf);
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
			this.setStaticValue(val);
			this.notifyValueChanged(val);
			
			return;
		}
		
		this.addKeyFrame(frame, val);
	}
	
	public T getValue(int frame) {
		if(this.keyFrames.isEmpty()) {
			return this.getStaticValue();
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
	
	public void addControlParamListener(ControlParamListener<T> l) {
		this.listeners.add(l);
	}
	
	public void removeControlParamListener(ControlParamListener<T> l) {
		this.listeners.remove(l);
	}
	
	private void notifyKeyFrameAdded(KeyFrame<T> kf) {
		for(ControlParamListener<T> l : this.listeners)
			l.keyFrameAdded(this, kf);
	}
	
	private void notifyKeyFrameRemoved(KeyFrame<T> kf) {
		for(ControlParamListener<T> l : this.listeners)
			l.keyFrameRemoved(this, kf);
	}
	
	private void notifyValueChanged(T val) {
		for(ControlParamListener<T> l : this.listeners)
			l.valueChanged(this, val);
	}
	
	// -- Abstract
	protected abstract void setStaticValue(T val);
	protected abstract T getStaticValue();
	
	protected abstract T cloneValue(T val);
	
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
