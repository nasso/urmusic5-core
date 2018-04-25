package io.github.nasso.urmusic.common.event;

import io.github.nasso.urmusic.model.project.Composition;
import io.github.nasso.urmusic.model.project.TrackEffect;

public interface VideoRendererListener {
	public void effectLoaded(TrackEffect fx);
	public void effectUnloaded(TrackEffect fx);
	
	public void frameRendered(Composition comp, float time);
}
