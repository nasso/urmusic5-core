package io.github.nasso.urmusic.model.project;

public class VideoEffectArgs extends EffectArgs {
	public int width, height, texInput, fboOutput;
	
	public VideoEffectArgs() {
	}
	
	public void clear() {
		super.clear();
		
		this.width = this.height = 0;
		this.texInput = this.fboOutput = -1;
	}
}
