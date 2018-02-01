package io.github.nasso.urmusic.model.event;

import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;

public interface TrackEffectsListener {
	public void effectAdded(TrackEffectInstance e, int pos);
	public void effectRemoved(TrackEffectInstance e, int pos);
	public void effectMoved(TrackEffectInstance e, int oldPos, int newPos);
}
