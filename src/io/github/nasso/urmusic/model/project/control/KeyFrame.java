package io.github.nasso.urmusic.model.project.control;

import java.util.ArrayList;
import java.util.List;

import io.github.nasso.urmusic.common.easing.EasingFunction;
import io.github.nasso.urmusic.common.event.KeyFrameListener;

public class KeyFrame<T> {
	private List<KeyFrameListener<T>> listeners = new ArrayList<>();
	
	private T value = null;
	private int frame = 0;
	private EasingFunction interp = EasingFunction.LINEAR;
	
	public KeyFrame() {
	}
	
	public KeyFrame(int frame, T value, EasingFunction interp) {
		this.setFrame(frame);
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

	public int getFrame() {
		return this.frame;
	}

	public void setFrame(int frame) {
		if(this.frame == frame) return;
		this.frame = frame;
		this.notifyFrameChanged(this.frame);
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
	
	private void notifyFrameChanged(int newValue) {
		for(KeyFrameListener<T> l : this.listeners)
			l.frameChanged(this, newValue);
	}
	
	private void notifyInterpChanged(EasingFunction newValue) {
		for(KeyFrameListener<T> l : this.listeners)
			l.interpChanged(this, newValue);
	}
}
