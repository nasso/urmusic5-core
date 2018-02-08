package io.github.nasso.urmusic.common.event;

public interface FocusListener<T> {
	public void focusChanged(T oldFocus, T newFocus);
}
