package io.github.nasso.urmusic.common.event;

import io.github.nasso.urmusic.common.easing.EasingFunction;
import io.github.nasso.urmusic.model.project.param.KeyFrame;

public interface KeyFrameListener<T> {
	public void valueChanged(KeyFrame<T> source, T newValue);
	public void frameChanged(KeyFrame<T> source, int newFrame);
	public void interpChanged(KeyFrame<T> source, EasingFunction newInterp);
}
