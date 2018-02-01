package io.github.nasso.urmusic.model.event;

import io.github.nasso.urmusic.model.project.control.ControlParam;

public interface EffectParametersListener {
	public void parameterAdded(ControlParam<?> ctrl);
	public void parameterRemoved(ControlParam<?> ctrl);
}
