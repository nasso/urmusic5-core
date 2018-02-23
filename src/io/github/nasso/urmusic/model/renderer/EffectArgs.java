package io.github.nasso.urmusic.model.renderer;

public class EffectArgs {
	public int width, height, texInput, fboOutput;
	public float time;
	public boolean cancelled;
	
	public EffectArgs() {
	}
	
	public void clear() {
		this.width = this.height = 0;
		this.time = this.texInput = this.fboOutput = -1;
		this.cancelled = false;
	}
}
