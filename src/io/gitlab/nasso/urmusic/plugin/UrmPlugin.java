package io.gitlab.nasso.urmusic.plugin;

import io.gitlab.nasso.urmusic.model.project.TrackEffect;

public interface UrmPlugin {
	public void pluginInit();
	public void pluginDispose();
	
	public TrackEffect[] getEffects();
}
