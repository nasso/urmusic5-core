package io.github.nasso.urmusic.model.project.control;

import io.github.nasso.urmusic.utils.easing.EasingFunction;

public class KeyFrame<T> {
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
		this.value = value;
	}

	public int getFrame() {
		return this.frame;
	}

	public void setFrame(int frame) {
		this.frame = frame;
	}

	public EasingFunction getInterpolationMethod() {
		return this.interp;
	}

	public void setInterpolationMethod(EasingFunction interp) {
		this.interp = interp;
	}
}
