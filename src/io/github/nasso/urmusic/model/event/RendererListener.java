package io.github.nasso.urmusic.model.event;

import io.github.nasso.urmusic.model.project.Composition;
import io.github.nasso.urmusic.model.project.TrackEffect;

public interface RendererListener {
	public void effectLoaded(TrackEffect fx);
	public void effectUnloaded(TrackEffect fx);
	
	public void frameRendered(Composition comp, int frame);
}
