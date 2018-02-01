package io.github.nasso.urmusic.model.event;

import io.github.nasso.urmusic.model.project.Track;

public interface TracklistListener {
	/**
	 * A track has been added to the tracklist.
	 * @param index
	 * @param track
	 */
	public void trackAdded(int index, Track track);
	
	/**
	 * A track has been removed from the tracklist.
	 * @param index
	 * @param track
	 */
	public void trackRemoved(int index, Track track);
}
