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
package io.github.nasso.urmusic.model.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.nasso.urmusic.common.event.TimelineListener;
import io.github.nasso.urmusic.model.UrmusicModel;

public class Timeline {	
	private List<Track> tracks = new ArrayList<>();
	private List<Track> unmodifiableTracks = Collections.unmodifiableList(this.tracks);
	
	private List<TimelineListener> timelineListeners = new ArrayList<>();
	
	private float duration = 180; // 3 minute
	private float framerate = 60;
	
	public Timeline() {
	}

	/**
	 * @return The duration expressed in frames, aka the total frame count in this timeline.
	 */
	public int getTotalFrameCount() {
		return (int) (this.duration * this.framerate);
	}
	
	public float getDuration() {
		return this.duration;
	}

	public void setDuration(float dur) {
		if(this.duration == dur) return;
		this.duration = dur;
		this.notifyDurationChanged();
	}
	
	public float getFramerate() {
		return this.framerate;
	}

	public void setFramerate(float framerate) {
		if(this.framerate == framerate) return;
		this.framerate = framerate;
		this.notifyFramerateChanged();
	}

	public void addTracklistListener(TimelineListener l) {
		this.timelineListeners.add(l);
	}
	
	public void removeTracklistListener(TimelineListener l) {
		this.timelineListeners.remove(l);
	}
	
	public List<Track> getTracks() {
		return this.unmodifiableTracks;
	}
	
	public void dispose() {
		for(int i = 0; i < this.tracks.size(); i++) {
			UrmusicModel.disposeTrack(this.tracks.get(i));
		}
	}
	
	public Track addTrack(Track t) {
		return this.addTrack(t, this.tracks.size());
	}
	
	public Track addTrack(Track t, int i) {
		if(t == null || i > this.tracks.size())
			return null;
		
		this.tracks.add(i, t);
		this.notifyTrackAdded(i, t);
		return t;
	}
	
	/**
	 * Removes the track at the specified index from the tracklist.
	 * @param index
	 */
	public void removeTrack(int index) {
		if(this.tracks.size() <= index || index < 0)
			throw new IndexOutOfBoundsException(index + " isn't a valid Track index. Should be in range [0; " + this.tracks.size() + "[");
		
		this.notifyTrackRemoved(index, this.tracks.remove(index));
	}
	
	/**
	 * Removes the Track t from the tracklist. Does nothing if the tracklist doesn't contain this track.
	 * @param t
	 */
	public void removeTrack(Track t) {
		int i = this.tracks.indexOf(t);
		if(i < 0) return;
		
		this.removeTrack(i);
	}
	
	public void moveTrack(int curPos, int newPos) {
		if(curPos < 0 || curPos >= this.tracks.size() || newPos < 0 || newPos >= this.tracks.size()) return;
		
		Track t = this.tracks.remove(curPos);
		this.tracks.add(newPos, t);
		
		this.notifyTrackMoved(curPos, newPos, t);
	}

	private void notifyDurationChanged() {
		for(TimelineListener l : this.timelineListeners) {
			l.durationChanged(this);
		}
	}
	
	private void notifyFramerateChanged() {
		for(TimelineListener l : this.timelineListeners) {
			l.framerateChanged(this);
		}
	}
	
	private void notifyTrackAdded(int index, Track t) {
		for(TimelineListener l : this.timelineListeners) {
			l.trackAdded(this, index, t);
		}
	}
	
	private void notifyTrackRemoved(int index, Track t) {
		for(TimelineListener l : this.timelineListeners) {
			l.trackRemoved(this, index, t);
		}
	}
	
	private void notifyTrackMoved(int oldIndex, int newIndex, Track t) {
		for(TimelineListener l : this.timelineListeners) {
			l.trackMoved(this, oldIndex, newIndex, t);
		}
	}
}
