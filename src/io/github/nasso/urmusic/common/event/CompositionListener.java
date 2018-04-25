package io.github.nasso.urmusic.common.event;

import io.github.nasso.urmusic.model.project.Composition;

public interface CompositionListener {
	public void clearColorChanged(Composition comp);
	public void resize(Composition comp);
	public void dispose(Composition comp);
}
