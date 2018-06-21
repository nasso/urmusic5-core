package io.gitlab.nasso.urmusic.plugin;

import io.gitlab.nasso.urmusic.model.project.VideoEffect;

public interface UrmPlugin {
	public void pluginInit();
	public void pluginDispose();
	
	public VideoEffect[] getEffects();
}
