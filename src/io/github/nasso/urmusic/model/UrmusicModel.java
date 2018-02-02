package io.github.nasso.urmusic.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.github.nasso.urmusic.model.effect.VignetteVFX;
import io.github.nasso.urmusic.model.event.FocusListener;
import io.github.nasso.urmusic.model.event.FrameCursorListener;
import io.github.nasso.urmusic.model.playback.PlaybackThread;
import io.github.nasso.urmusic.model.project.Composition;
import io.github.nasso.urmusic.model.project.Project;
import io.github.nasso.urmusic.model.project.Track;
import io.github.nasso.urmusic.model.project.Track.TrackActivityRange;
import io.github.nasso.urmusic.model.project.TrackEffect;
import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.github.nasso.urmusic.model.renderer.Renderer;
import io.github.nasso.urmusic.view.UrmusicView;

public class UrmusicModel {
	public static final TrackEffect[] STOCK_EFFECTS = new TrackEffect[] {
		VignetteVFX.FX
	};
	
	private UrmusicModel() { }

	private static Project project;
	private static Renderer renderer;
	private static PlaybackThread playbackThread;
	
	private static List<FrameCursorListener> frameCursorListeners = new ArrayList<>();
	private static int frameCursor = 0;
	
	private static List<TrackEffect> loadedEffects = new ArrayList<>();
	
	public static void init() {
		// TODO: User prefs
		renderer = new Renderer(200);
		
		loadProject(null);
		
		addFrameCursorListener((oldPosition, newPosition)  -> {
			renderer.queueFrameRender(focusedComposition, newPosition);
		});
		
		// -- EFFECTS
		for(TrackEffect fx : STOCK_EFFECTS) {
			loadEffect(fx);
		}
		// -- EFFECTS END
		
		playbackThread = new PlaybackThread();
		playbackThread.setFPS(getFocusedComposition().getFramerate());
		
		addCompositionFocusListener((oldComp, newComp) -> {
			playbackThread.setFPS(newComp.getFramerate());
		});
	}
	
	public static void exit() {
		UrmusicView.saveViewState();
		
		renderer.dispose();
		
		System.exit(0);
	}
	
	public static boolean isEffectLoaded(TrackEffect fx) {
		return loadedEffects.contains(fx);
	}
	
	public static void loadEffect(TrackEffect fx) {
		if(isEffectLoaded(fx)) return;
		
		fx.effectMain();
		if(fx.isVideoEffect()) {
			renderer.initEffect(fx);
			loadedEffects.add(fx);
		}
	}
	
	public static void unloadEffect(TrackEffect fx) {
		if(!isEffectLoaded(fx)) return;

		if(fx.isVideoEffect()) {
			renderer.disposeEffect(fx);
		}
		
		loadedEffects.remove(fx);
	}
	
	public static void loadProject(File f) {
		if(project != null) {
			// TODO: unload project
		}
		
		if(f == null) {
			project = new Project();
			focusComposition(project.getMainComposition());
			renderer.queueFrameRender(focusedComposition, 0);
		}
	}
	
	public static Renderer getRenderer() {
		return renderer;
	}
	
	public static Project getCurrentProject() {
		return project;
	}
	
	public static void makeCompositionDirty(Composition comp) {
		renderer.makeCompositionDirty(comp);
	}
	
	public static void addFrameCursorListener(FrameCursorListener l) {
		frameCursorListeners.add(l);
	}
	
	public static void removeFrameCursorListener(FrameCursorListener l) {
		frameCursorListeners.remove(l);
	}

	public static int getFrameCursor() {
		return frameCursor;
	}

	public static void setFrameCursor(int frameCursor) {
		frameCursor = Math.min(getFocusedComposition().getLength() - 1, Math.max(frameCursor, 0));
		
		if(UrmusicModel.frameCursor == frameCursor) return;
		int before = UrmusicModel.frameCursor;
		notifyFrameCursorChange(before, UrmusicModel.frameCursor = frameCursor);
	}
	
	public static boolean isPlayingBack() {
		return playbackThread.isPlayingBack();
	}
	
	public static void startPlayback() {
		playbackThread.startPlayback();
	}
	
	public static void stopPlayback() {
		playbackThread.stopPlayback();
	}
	
	private static void notifyFrameCursorChange(int before, int after) {
		for(FrameCursorListener l : frameCursorListeners) {
			l.frameChanged(before, after);
		}
	}
	
	public static void disposeTrack(Track track) {
		renderer.disposeTrack(track);
	}
	
	public static void disposeEffect(TrackEffectInstance fx) {
		if(fx.getEffectClass().isVideoEffect()) {
			renderer.disposeEffectInstance(fx);
		}
	}
	
	// -- Focus Listeners --
	// Composition
	private static Composition focusedComposition;
	private static List<FocusListener<Composition>> compFocusListeners = new ArrayList<>();
	
	public static void addCompositionFocusListener(FocusListener<Composition> l) {
		compFocusListeners.add(l);
	}
	
	public static void removeCompositionFocusListener(FocusListener<Composition> l) {
		compFocusListeners.remove(l);
	}
	
	public static void focusComposition(Composition newFocus) {
		Composition oldFocus = focusedComposition;
		focusedComposition = newFocus;
		
		for(FocusListener<Composition> l : compFocusListeners) {
			l.focusChanged(oldFocus, newFocus);
		}
	}
	
	public static Composition getFocusedComposition() {
		return focusedComposition;
	}
	
	// Track
	private static Track focusedTrack = null;
	private static List<FocusListener<Track>> trackFocusListeners = new ArrayList<>();
	
	public static void addTrackFocusListener(FocusListener<Track> l) {
		trackFocusListeners.add(l);
	}
	
	public static void removeTrackFocusListener(FocusListener<Track> l) {
		trackFocusListeners.remove(l);
	}
	
	public static void focusTrack(Track newFocus) {
		Track oldFocus = focusedTrack;
		focusedTrack = newFocus;
		
		if(focusedTrackRange != null && focusedTrackRange.getTrack() != newFocus) {
			focusTrackActivityRange(null);
		}
		
		for(FocusListener<Track> l : trackFocusListeners) {
			l.focusChanged(oldFocus, newFocus);
		}
	}
	
	public static Track getFocusedTrack() {
		return focusedTrack;
	}
	
	// Track Activity Range
	private static TrackActivityRange focusedTrackRange = null;
	private static List<FocusListener<TrackActivityRange>> trackRangesFocusListeners = new ArrayList<>();
	
	public static void addTrackActivityRangeFocusListener(FocusListener<TrackActivityRange> l) {
		trackRangesFocusListeners.add(l);
	}
	
	public static void removeTrackActivityRangeFocusListener(FocusListener<TrackActivityRange> l) {
		trackRangesFocusListeners.remove(l);
	}
	
	public static void focusTrackActivityRange(TrackActivityRange newFocus) {
		TrackActivityRange oldFocus = focusedTrackRange;
		focusedTrackRange = newFocus;
		
		if(newFocus != null && focusedTrack != newFocus.getTrack()) {
			focusTrack(newFocus.getTrack());
		}
		
		for(FocusListener<TrackActivityRange> l : trackRangesFocusListeners) {
			l.focusChanged(oldFocus, newFocus);
		}
	}
	
	public static TrackActivityRange getFocusedTrackActivityRange() {
		return focusedTrackRange;
	}
}
