package io.github.nasso.urmusic.model.project.video;

import com.jogamp.opengl.GL3;

import io.github.nasso.urmusic.model.project.TrackEffect;

public abstract class VideoEffect extends TrackEffect {
	public abstract class VideoEffectInstance extends TrackEffectInstance {
		public abstract void setup(GL3 gl);
		public abstract void apply(GL3 gl, int texInput, int fboOutput);
		public abstract void dispose(GL3 gl);
	}
	
	public abstract void globalSetup(GL3 gl);
	public abstract void globalDispose(GL3 gl);
	
	public abstract VideoEffectInstance instance();
}
