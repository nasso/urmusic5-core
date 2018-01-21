package io.github.nasso.urmusic.model.timeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.nasso.urmusic.model.event.TracklistListener;

public class Timeline {	
	private List<Track> tracks = new ArrayList<>();
	private List<Track> unmodifiableTracks = Collections.unmodifiableList(this.tracks);
	
	private List<TracklistListener> tracklistListeners = new ArrayList<>();
	
	public Timeline() {
	}
	
	public void addTracklistListener(TracklistListener l) {
		this.tracklistListeners.add(l);
	}
	
	public void removeTracklistListener(TracklistListener l) {
		this.tracklistListeners.remove(l);
	}
	
	public List<Track> getTracks() {
		return this.unmodifiableTracks;
	}

	/**
	 * Adds a video track with the given name and no effect (empty invisible image).
	 * @param name
	 * @return The {@link VideoTrack} object created and added.
	 */
	public VideoTrack addVideoTrack(String name) {
		VideoTrack t = new VideoTrack();
		t.setName(name);
		this.tracks.add(t);
		
		this.fireTrackAdded(this.tracks.size() - 1, t);
		
		return t;
	}
	
	/**
	 * Adds an audio track with the given name and no effect (silence).
	 * @param name
	 * @return The {@link AudioTrack} object created and added.
	 */
	public AudioTrack addAudioTrack(String name) {
		AudioTrack t = new AudioTrack();
		t.setName(name);
		this.tracks.add(t);
		
		this.fireTrackAdded(this.tracks.size() - 1, t);
		
		return t;
	}
	
	/**
	 * Removes the track at the specified index from the tracklist.
	 * @param index
	 */
	public void removeTrack(int index) {
		if(this.tracks.size() <= index || index < 0)
			throw new IndexOutOfBoundsException(index + " isn't a valid Track index. Should be in range [0; " + this.tracks.size() + "[");
		
		this.fireTrackRemoved(index, this.tracks.remove(index));
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
	
	private void fireTrackAdded(int index, Track t) {
		for(TracklistListener l : this.tracklistListeners) {
			l.trackAdded(index, t);
		}
	}
	
	private void fireTrackRemoved(int index, Track t) {
		for(TracklistListener l : this.tracklistListeners) {
			l.trackRemoved(index, t);
		}
	}
}
