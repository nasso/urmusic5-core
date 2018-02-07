package io.github.nasso.urmusic.model.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.event.TimelineListener;

public class Timeline {	
	private List<Track> tracks = new ArrayList<>();
	private List<Track> unmodifiableTracks = Collections.unmodifiableList(this.tracks);
	
	private List<TimelineListener> timelineListeners = new ArrayList<>();
	
	private int length = 2400; // 1:20 @ 30fps
	private int framerate = 30;
	
	public Timeline() {
	}

	public int getLength() {
		return this.length;
	}

	public void setLength(int length) {
		if(this.length == length) return;
		this.length = length;
		this.notifyLengthChanged();
	}
	
	public int getFramerate() {
		return this.framerate;
	}

	public void setFramerate(int framerate) {
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
		this.tracks.add(t);
		this.notifyTrackAdded(this.tracks.size() - 1, t);
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

	private void notifyLengthChanged() {
		for(TimelineListener l : this.timelineListeners) {
			l.lengthChanged(this);
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
}
