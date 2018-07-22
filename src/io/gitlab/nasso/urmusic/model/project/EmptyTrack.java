package io.gitlab.nasso.urmusic.model.project;

import io.gitlab.nasso.urmusic.model.project.VideoEffect.VideoEffectInstance;

public class EmptyTrack extends Track {
	public EmptyTrack(float initRangeLen) {
		super(initRangeLen);
	}
	
	public EmptyTrack() {
	}

	public VideoEffectInstance getRoot() {
		return null;
	}
}
