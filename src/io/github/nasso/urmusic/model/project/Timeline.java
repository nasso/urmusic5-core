package io.github.nasso.urmusic.model.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.nasso.urmusic.model.event.TracklistListener;
import io.github.nasso.urmusic.model.project.audio.AudioTrack;
import io.github.nasso.urmusic.model.project.video.VideoTrack;

public class Timeline {	
	private List<Track<?>> tracks = new ArrayList<>();
	private List<Track<?>> unmodifiableTracks = Collections.unmodifiableList(this.tracks);
	
	private List<TracklistListener> tracklistListeners = new ArrayList<>();
	
	public Timeline() {
	}
	
	public void addTracklistListener(TracklistListener l) {
		this.tracklistListeners.add(l);
	}
	
	public void removeTracklistListener(TracklistListener l) {
		this.tracklistListeners.remove(l);
	}
	
	public List<Track<?>> getTracks() {
		return this.unmodifiableTracks;
	}
	
	public void dispose() {
		for(int i = 0; i < this.tracks.size(); i++) {
			this.tracks.get(i).dispose();
		}
	}
	
	/**
	 * Adds a video track with the given name and no effect (empty invisible image).
	 * @param name
	 * @return The {@link VideoTrack} object created and added.
	 */
	public VideoTrack addVideoTrack(String name) {
		VideoTrack t = new VideoTrack();
		t.setName(name);
		this.addTrack(t);
		
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
		this.addTrack(t);
		
		return t;
	}
	
	/**
	 * Adds a composite track with the given name and with the given composition.
	 * @param name
	 * @param comp
	 * @return The {@link CompositeTrack} object created and added.
	 */
	public CompositeTrack addCompositeTrack(String name, Composition comp) {
		CompositeTrack t = new CompositeTrack(comp);
		t.setName(name);
		t.setComposition(comp);
		this.addTrack(t);
		
		return t;
	}
	
	/**
	 * Adds a composite track with the given name and an empty default composition.
	 * @param name
	 * @return The {@link CompositeTrack} object created and added.
	 */
	public CompositeTrack addCompositeTrack(String name) {
		return this.addCompositeTrack(name, new Composition());
	}
	
	public Track<?> addTrack(Track<?> t) {
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
	public void removeTrack(Track<?> t) {
		int i = this.tracks.indexOf(t);
		if(i < 0) return;
		
		this.removeTrack(i);
	}
	
	private void notifyTrackAdded(int index, Track<?> t) {
		for(TracklistListener l : this.tracklistListeners) {
			l.trackAdded(index, t);
		}
	}
	
	private void notifyTrackRemoved(int index, Track<?> t) {
		for(TracklistListener l : this.tracklistListeners) {
			l.trackRemoved(index, t);
		}
	}
}
