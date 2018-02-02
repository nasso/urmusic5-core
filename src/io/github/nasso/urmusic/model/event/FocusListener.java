package io.github.nasso.urmusic.model.event;

public interface FocusListener<T> {
	public void focusChanged(T oldFocus, T newFocus);
}
