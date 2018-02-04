package io.github.nasso.urmusic.model.event;

import io.github.nasso.urmusic.model.project.control.ControlParam;
import io.github.nasso.urmusic.model.project.control.KeyFrame;

public interface ControlParamListener<T> {
	public void valueChanged(ControlParam<T> source, T newVal);
	
	public void keyFrameAdded(ControlParam<T> source, KeyFrame<T> kf);
	public void keyFrameRemoved(ControlParam<T> source, KeyFrame<T> kf);
}
