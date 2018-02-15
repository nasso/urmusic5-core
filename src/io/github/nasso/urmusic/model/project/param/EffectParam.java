package io.github.nasso.urmusic.model.project.param;

import java.util.ArrayList;
import java.util.List;

import io.github.nasso.urmusic.common.easing.EasingFunction;
import io.github.nasso.urmusic.common.event.EffectParamListener;
import io.github.nasso.urmusic.common.event.KeyFrameListener;

public abstract class EffectParam<T> implements KeyFrameListener<T> {
	private List<EffectParamListener<T>> listeners = new ArrayList<>();
	
	private List<KeyFrame<T>> keyFrames = new ArrayList<>();
	private String name;
	
	private final boolean canAnimate;

	public EffectParam(String name) {
		this(name, true);
	}
	
	public EffectParam(String name, boolean canAnimate) {
		this.name = name;
		this.canAnimate = canAnimate;
		
		if(this.canAnimate) {
			this.addEffectParamListener(new EffectParamListener<T>() {
				public void valueChanged(EffectParam<T> source, T newVal) {
				}
	
				public void keyFrameAdded(EffectParam<T> source, KeyFrame<T> kf) {
					kf.addKeyFrameListener(EffectParam.this);
				}
	
				public void keyFrameRemoved(EffectParam<T> source, KeyFrame<T> kf) {
					kf.removeKeyFrameListener(EffectParam.this);
				}
			});
		}
	}
	
	public String getName() {
		return this.name;
	}
	
	public KeyFrame<T> addKeyFrame(int frame) {
		if(!this.canAnimate) return null;
		
		return this.addKeyFrame(frame, this.getValue(frame));
	}
	
	public KeyFrame<T> addKeyFrame(int frame, T val) {
		if(!this.canAnimate) return null;
		
		return this.addKeyFrame(frame, val, EasingFunction.EASE);
	}
	
	public KeyFrame<T> addKeyFrame(int frame, T val, EasingFunction func) {
		if(!this.canAnimate) return null;
		
		T valClone = this.cloneValue(val);
		
		int i;
		for(i = 0; i < this.keyFrames.size(); i++) {
			KeyFrame<T> kf = this.keyFrames.get(i);
			
			if(kf.getFrame() == frame) {
				kf.setValue(valClone);
				kf.setInterpolationMethod(func);
				return kf;
			} else if(kf.getFrame() > frame) break;
		}
		
		KeyFrame<T> kf = new KeyFrame<T>(frame, valClone, func);
		this.keyFrames.add(i, kf);
		
		this.notifyKeyFrameAdded(kf);
		
		return kf;
	}
	
	public KeyFrame<T> getKeyFrameAt(int frame) {
		if(!this.canAnimate) return null;
		
		for(KeyFrame<T> kf : this.keyFrames) {
			if(kf.getFrame() == frame) return kf;
		}
		
		return null;
	}

	public KeyFrame<T> getKeyFrameBefore(int frame) {
		if(!this.canAnimate) return null;
		
		int i;
		for(i = 0; i < this.keyFrames.size(); i++) {
			if(this.keyFrames.get(i).getFrame() >= frame) break;
		}
		
		return i == 0 ? null : this.keyFrames.get(i - 1);
	}
	
	public KeyFrame<T> getKeyFrameAfter(int frame) {
		if(!this.canAnimate) return null;
		
		int i;
		for(i = 0; i < this.keyFrames.size(); i++) {
			if(this.keyFrames.get(i).getFrame() > frame) break;
		}
		
		return i == this.keyFrames.size() ? null : this.keyFrames.get(i);
	}
	
	public int getKeyFrameCount() {
		return this.keyFrames.size();
	}
	
	public KeyFrame<T> getKeyFrame(int index) {
		if(!this.canAnimate) return null;
		
		return this.keyFrames.get(index);
	}
	
	public void removeKeyFrame(KeyFrame<T> kf) {
		if(!this.canAnimate) return;
		
		this.keyFrames.remove(kf);
		this.notifyKeyFrameRemoved(kf);
	}
	
	public void frameChanged(KeyFrame<T> source, int frame) {
		this.keyFrames.remove(source);

		int i;
		for(i = 0; i < this.keyFrames.size(); i++) {
			KeyFrame<T> kf = this.keyFrames.get(i);
			
			if(kf.getFrame() == frame) {
				this.removeKeyFrame(kf);
				break;
			}
			
			if(kf.getFrame() > frame) {
				i--;
				break;
			}
		}
		
		this.keyFrames.add(i, source);
	}
	
	public void valueChanged(KeyFrame<T> source, T newValue) {
		// No need to reorder
	}
	
	public void interpChanged(KeyFrame<T> source, EasingFunction newInterp) {
		// No need to reorder
	}
	
	/**
	 * Sets the value of the parameter at the specified frame.<br>
	 * If there's no key frames for this parameter, the frame index given won't matter.
	 * 
	 * @param val
	 * @param frame
	 */
	public void setValue(T val, int frame) {
		if(!this.canAnimate || this.keyFrames.isEmpty()) {
			this.setStaticValue(val);
			this.notifyValueChanged(val);
			
			return;
		}
		
		this.addKeyFrame(frame, val);
	}
	
	public T getValue(int frame) {
		if(!this.canAnimate || this.keyFrames.isEmpty()) {
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
	
	public void addEffectParamListener(EffectParamListener<T> l) {
		this.listeners.add(l);
	}
	
	public void removeEffectParamListener(EffectParamListener<T> l) {
		this.listeners.remove(l);
	}
	
	private void notifyKeyFrameAdded(KeyFrame<T> kf) {
		for(EffectParamListener<T> l : this.listeners)
			l.keyFrameAdded(this, kf);
	}
	
	private void notifyKeyFrameRemoved(KeyFrame<T> kf) {
		for(EffectParamListener<T> l : this.listeners)
			l.keyFrameRemoved(this, kf);
	}
	
	private void notifyValueChanged(T val) {
		for(EffectParamListener<T> l : this.listeners)
			l.valueChanged(this, val);
	}
	
	// -- Abstract
	/**
	 * Sets the static value for this parameter.<br>
	 * Only called when there's no key frames for this parameter ({@link EffectParam#getKeyFrameCount()} returns <code>0</code>)
	 * @param val
	 */
	protected abstract void setStaticValue(T val);
	
	/**
	 * Gets the static value for this parameter.<br>
	 * Only called when there's no key frames for this parameter ({@link EffectParam#getKeyFrameCount()} returns <code>0</code>)
	 * @return
	 */
	protected abstract T getStaticValue();
	
	/**
	 * Returns a copy of the given value. Used when adding key frames to prevent unexpected modifications to the value.<br>
	 * So if <code>val</code> is already immutable (e.g. {@link Float}), this method should be able to just return it with no problem.<br>
	 * 
	 * No need to implement if this parameter isn't animatable, so you can just return <code>null</code> in this case, since this will never be called.
	 * @param val
	 * @return
	 */
	protected abstract T cloneValue(T val);
	
	/**
	 * Linearly interpolates between <code>s</code> and <code>e</code>, <code>t</code> being the factor (usually in the range 0.0..1.0 but not limited to).<br>
	 * For <code>t == 0.0</code>, <code>s</code> should be returned, and for <code>t == 1.0</code>, <code>e</code> should be returned.<br>
	 * The implementation can make modification to some local mutable object instance and return it to use less memory.<br>
	 * 
	 * No need to implement if this parameter isn't animatable, so you can just return <code>null</code> in this case, since this will never be called.
	 * 
	 * @param s Begin value
	 * @param e End value
	 * @param t Lerp factor
	 * @return
	 */
	public abstract T ramp(T s, T e, float t);
}
