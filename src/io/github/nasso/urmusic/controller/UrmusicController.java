package io.github.nasso.urmusic.controller;

import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.project.Composition;
import io.github.nasso.urmusic.model.project.Track;
import io.github.nasso.urmusic.model.project.Track.TrackActivityRange;
import io.github.nasso.urmusic.model.project.control.EffectParam;
import io.github.nasso.urmusic.model.project.control.KeyFrame;

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
	
	public static void focusEffectParameter(EffectParam<?> p) {
		UrmusicModel.focusEffectParameter(p);
	}
	
	// -- Frame control --
	public static void setFramePosition(int frame) {
		UrmusicModel.setFrameCursor(frame);
	}
	
	public static void frameAdvance() {
		UrmusicModel.setFrameCursor(UrmusicModel.getFrameCursor() + 1);
	}
	
	public static void frameBack() {
		UrmusicModel.setFrameCursor(UrmusicModel.getFrameCursor() - 1);
	}

	public static void goToNextKeyFrame() {
		EffectParam<?> param = UrmusicModel.getFocusedEffectParameter();
		if(param == null) return;

		KeyFrame<?> kf = param.getKeyFrameAfter(UrmusicModel.getFrameCursor());
		if(kf != null) setFramePosition(kf.getFrame());
	}
	
	public static void goToPreviousKeyFrame() {
		EffectParam<?> param = UrmusicModel.getFocusedEffectParameter();
		if(param == null) return;

		KeyFrame<?> kf = param.getKeyFrameBefore(UrmusicModel.getFrameCursor());
		if(kf != null) setFramePosition(kf.getFrame());
	}
	
	public static void playPause() {
		if(UrmusicModel.isPlayingBack()) UrmusicModel.stopPlayback();
		else UrmusicModel.startPlayback();
	}
	
	// -- Edit --
	public static void addTrack() {
		Composition comp = UrmusicModel.getFocusedComposition();
		if(comp == null) return;
		
		comp.getTimeline().addTrack(new Track(comp.getTimeline().getLength()));
	}
	
	public static void splitTrack() {
		Track t = UrmusicModel.getFocusedTrack();
		
		if(t != null) {
			t.splitAt(UrmusicModel.getFrameCursor());
		}
	}
	
	public static void deleteTrack(Track t) {
		Composition comp = UrmusicModel.getFocusedComposition();
		
		if(comp == null) return;
		if(t == null) return;
		
		int i = comp.getTimeline().getTracks().indexOf(t);
		
		if(i < 0) return;
		
		UrmusicModel.deleteTrack(comp, i);
	}
	
	public static void deleteFocusedTrackActivityRange() {
		UrmusicModel.deleteTrackActivityRange(UrmusicModel.getFocusedTrackActivityRange());
	}
	
	public static void deleteFocusedTrack() {
		deleteTrack(UrmusicModel.getFocusedTrack());
	}
}
