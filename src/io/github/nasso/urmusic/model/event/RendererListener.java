package io.github.nasso.urmusic.model.event;

import io.github.nasso.urmusic.model.project.Composition;

public interface RendererListener {
	public void frameRendered(Composition comp, int frame);
}
