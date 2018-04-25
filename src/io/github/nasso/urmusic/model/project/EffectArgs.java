package io.github.nasso.urmusic.model.project;

public class EffectArgs {
	public float time;
	public boolean cancelled;
	
	public EffectArgs() {
	}
	
	public void clear() {
		this.time = -1;
		this.cancelled = false;
	}
}
