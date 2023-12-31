/*******************************************************************************
 * urmusic - The Free and Open Source Music Visualizer Tool
 * Copyright (C) 2018  nasso (https://github.com/nasso)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * Contact "nasso": nassomails -at- gmail dot com
 ******************************************************************************/
package io.gitlab.nasso.urmusic.common.event;

import io.gitlab.nasso.urmusic.model.project.Timeline;
import io.gitlab.nasso.urmusic.model.project.Track;

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
	
	/**
	 * A track that is already present in the tracklist has been moved in the same tracklist, without getting removed.<br />
	 * Other tracks are shifted, just like when inserting/removing a track.
	 * 
	 * @param src
	 * @param oldIndex
	 * @param newIndex
	 * @param track
	 */
	public void trackMoved(Timeline src, int oldIndex, int newIndex, Track track);
}
