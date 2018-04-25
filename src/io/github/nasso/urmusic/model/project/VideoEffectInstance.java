package io.github.nasso.urmusic.model.project;

import com.jogamp.opengl.GL3;

public interface VideoEffectInstance {
	public void setupVideo(GL3 gl);
	public void applyVideo(GL3 gl, VideoEffectArgs args);
	public void disposeVideo(GL3 gl);
}
