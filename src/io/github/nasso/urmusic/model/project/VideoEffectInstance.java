package io.github.nasso.urmusic.model.project;

import com.jogamp.opengl.GL3;

import io.github.nasso.urmusic.model.renderer.EffectArgs;

public interface VideoEffectInstance {
	public void setupVideo(GL3 gl);
	public void applyVideo(GL3 gl, EffectArgs args);
	public void disposeVideo(GL3 gl);
}
