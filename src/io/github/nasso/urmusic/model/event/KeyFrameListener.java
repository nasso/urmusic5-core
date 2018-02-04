package io.github.nasso.urmusic.model.event;

import io.github.nasso.urmusic.model.project.control.KeyFrame;
import io.github.nasso.urmusic.utils.easing.EasingFunction;

public interface KeyFrameListener<T> {
	public void valueChanged(KeyFrame<T> source, T newValue);
	public void frameChanged(KeyFrame<T> source, int newFrame);
	public void interpChanged(KeyFrame<T> source, EasingFunction newInterp);
}
