package io.github.nasso.urmusic.common;

public interface ScriptRuntimeErrorListener {
	public void onError(String message, int line, int column);
}
