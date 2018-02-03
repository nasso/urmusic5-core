package io.github.nasso.urmusic.model.event;

import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.github.nasso.urmusic.model.project.control.ControlParam;

public interface EffectInstanceListener {
	public void dirtyFlagged(TrackEffectInstance source);
	public void enabledStateChanged(TrackEffectInstance source, boolean isEnabledNow);
	
	public void parameterAdded(TrackEffectInstance source, String name, ControlParam<?> ctrl);
	public void parameterRemoved(TrackEffectInstance source, String name, ControlParam<?> ctrl);
}
