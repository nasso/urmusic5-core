package io.github.nasso.urmusic.common.event;

import io.github.nasso.urmusic.model.project.Track;
import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;

public interface TrackListener {
	public void nameChanged(Track source, String newName);
	public void enabledStateChanged(Track source, boolean isEnabledNow);
	
	public void rangesChanged(Track source);

	public void effectAdded(Track source, TrackEffectInstance e, int pos);
	public void effectRemoved(Track source, TrackEffectInstance e, int pos);
	public void effectMoved(Track source, TrackEffectInstance e, int oldPos, int newPos);
}
