package io.github.nasso.urmusic.model.event;

import io.github.nasso.urmusic.model.project.Composition;

public interface CompositionFocusListener {
	public void focusedCompositionChanged(Composition oldFocus, Composition newFocus);
}
