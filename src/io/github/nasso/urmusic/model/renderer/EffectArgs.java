package io.github.nasso.urmusic.model.renderer;

public class EffectArgs {
	public int width, height, frame, texInput, fboOutput;
	public boolean cancelled;
	
	public EffectArgs() {
	}
	
	public void clear() {
		this.width = this.height = 0;
		this.frame = this.texInput = this.fboOutput = -1;
		this.cancelled = false;
	}
}
