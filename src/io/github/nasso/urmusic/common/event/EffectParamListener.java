package io.github.nasso.urmusic.common.event;

import io.github.nasso.urmusic.model.project.param.EffectParam;
import io.github.nasso.urmusic.model.project.param.KeyFrame;

public interface EffectParamListener<T> {
	public void valueChanged(EffectParam<T> source, T newVal);
	
	public void keyFrameAdded(EffectParam<T> source, KeyFrame<T> kf);
	public void keyFrameRemoved(EffectParam<T> source, KeyFrame<T> kf);
}
