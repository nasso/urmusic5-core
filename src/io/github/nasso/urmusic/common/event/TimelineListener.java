package io.github.nasso.urmusic.common.event;

import io.github.nasso.urmusic.model.project.Timeline;
import io.github.nasso.urmusic.model.project.Track;

public interface TimelineListener {
	public void durationChanged(Timeline src);
	public void framerateChanged(Timeline src);
	
	/**
	 * A track has been added to the tracklist.
	 * @param index
	 * @param track
	 */
	public void trackAdded(Timeline src, int index, Track track);
	
	/**
	 * A track has been removed from the tracklist.
	 * @param index
	 * @param track
	 */
	public void trackRemoved(Timeline src, int index, Track track);
}
