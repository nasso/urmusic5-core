package io.github.nasso.urmusic.controller;

import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.project.Composition;
import io.github.nasso.urmusic.model.project.Track;
import io.github.nasso.urmusic.model.project.Track.TrackActivityRange;

public class UrmusicController {
	private UrmusicController() { }
	
	public static void init() {
		
	}
	
	public static void requestExit() {
		UrmusicModel.exit();
	}
	
	// -- Focus --
	public static void focusComposition(Composition comp) {
		UrmusicModel.focusComposition(comp);
	}
	
	public static void focusTrack(Track t) {
		UrmusicModel.focusTrack(t);
	}
	
	public static void focusTrackActivityRange(TrackActivityRange r) {
		UrmusicModel.focusTrackActivityRange(r);
	}
	
	// -- Playback --
	public static void setFramePosition(int frame) {
		UrmusicModel.setFrameCursor(frame);
	}
	
	public static void frameAdvance() {
		UrmusicModel.setFrameCursor(UrmusicModel.getFrameCursor() + 1);
	}
	
	public static void frameBack() {
		UrmusicModel.setFrameCursor(UrmusicModel.getFrameCursor() - 1);
	}
	
	public static void playPause() {
		if(UrmusicModel.isPlayingBack()) UrmusicModel.stopPlayback();
		else UrmusicModel.startPlayback();
	}
	
	// -- Edit --
	public static void splitTrack() {
		Track t = UrmusicModel.getFocusedTrack();
		
		if(t != null) {
			t.splitAt(UrmusicModel.getFrameCursor());
		}
	}
}
