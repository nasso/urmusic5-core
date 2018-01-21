package io.github.nasso.urmusic.model.event;

import io.github.nasso.urmusic.model.timeline.Track;

public interface TrackRangesListener {
	public void rangesChanged(Track source);
}
