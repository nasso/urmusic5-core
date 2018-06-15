package io.gitlab.nasso.urmusic.plugin;

import io.gitlab.nasso.urmusic.model.project.TrackEffect;

public interface UrmPlugin {
	public TrackEffect[] getEffects();
	public String getName();
}
