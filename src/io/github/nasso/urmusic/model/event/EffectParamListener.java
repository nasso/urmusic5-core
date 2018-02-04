package io.github.nasso.urmusic.model.event;

import io.github.nasso.urmusic.model.project.control.EffectParam;
import io.github.nasso.urmusic.model.project.control.KeyFrame;

public interface EffectParamListener<T> {
	public void valueChanged(EffectParam<T> source, T newVal);
	
	public void keyFrameAdded(EffectParam<T> source, KeyFrame<T> kf);
	public void keyFrameRemoved(EffectParam<T> source, KeyFrame<T> kf);
}
