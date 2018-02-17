package io.github.nasso.urmusic.common.event;

public interface MultiFocusListener<T> {
	public void focused(T o);
	public void unfocused(T o);
}
