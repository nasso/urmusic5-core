package io.github.nasso.urmusic.model.event;

import io.github.nasso.urmusic.model.project.control.ControlParam;

public interface EffectParametersListener {
	public void parameterAdded(String name, ControlParam<?> ctrl);
	public void parameterRemoved(String name, ControlParam<?> ctrl);
}
