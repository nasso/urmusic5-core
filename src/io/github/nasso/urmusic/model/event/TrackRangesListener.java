package io.github.nasso.urmusic.model.event;

import io.github.nasso.urmusic.model.project.Track;

public interface TrackRangesListener {
	public void rangesChanged(Track<?> source);
}
