package io.github.nasso.urmusic.model.project.param;

import java.util.ArrayList;
import java.util.List;

import io.github.nasso.urmusic.common.easing.EasingFunction;
import io.github.nasso.urmusic.common.event.KeyFrameListener;

public class KeyFrame<T> {
	private List<KeyFrameListener<T>> listeners = new ArrayList<>();
	
	private T value = null;
	private float time = 0;
	private EasingFunction interp = EasingFunction.LINEAR;
	
	public KeyFrame() {
	}
	
	public KeyFrame(float position, T value, EasingFunction interp) {
		this.setPosition(position);
		this.setValue(value);
		this.setInterpolationMethod(interp);
	}

	public T getValue() {
		return this.value;
	}

	public void setValue(T value) {
		if(this.value == value) return;
		this.value = value;
		this.notifyValueChanged(this.value);
	}

	public float getPosition() {
		return this.time;
	}

	public void setPosition(float time) {
		if(this.time == time) return;
		this.time = time;
		this.notifyPositionChanged(this.time);
	}

	public EasingFunction getInterpolationMethod() {
		return this.interp;
	}

	public void setInterpolationMethod(EasingFunction interp) {
		if(this.interp == interp) return;
		this.interp = interp;
		this.notifyInterpChanged(this.interp);
	}
	
	public void addKeyFrameListener(KeyFrameListener<T> l) {
		this.listeners.add(l);
	}
	
	public void removeKeyFrameListener(KeyFrameListener<T> l) {
		this.listeners.remove(l);
	}
	
	private void notifyValueChanged(T newValue) {
		for(KeyFrameListener<T> l : this.listeners)
			l.valueChanged(this, newValue);
	}
	
	private void notifyPositionChanged(float newValue) {
		for(KeyFrameListener<T> l : this.listeners)
			l.positionChanged(this, newValue);
	}
	
	private void notifyInterpChanged(EasingFunction newValue) {
		for(KeyFrameListener<T> l : this.listeners)
			l.interpChanged(this, newValue);
	}
}
