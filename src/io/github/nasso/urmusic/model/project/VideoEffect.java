package io.github.nasso.urmusic.model.project;

import com.jogamp.opengl.GL3;

public interface VideoEffect {
	public void globalVideoSetup(GL3 gl);
	public void globalVideoDispose(GL3 gl);
}
