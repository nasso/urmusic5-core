package io.github.nasso.urmusic.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.github.nasso.urmusic.model.effect.VignetteVFX;
import io.github.nasso.urmusic.model.event.CompositionFocusListener;
import io.github.nasso.urmusic.model.event.FrameCursorListener;
import io.github.nasso.urmusic.model.playback.PlaybackThread;
import io.github.nasso.urmusic.model.project.Composition;
import io.github.nasso.urmusic.model.project.Project;
import io.github.nasso.urmusic.model.project.Track;
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
	
	private static Composition focusedComposition;
	private static List<CompositionFocusListener> compositionFocusListeners = new ArrayList<>();
	
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
	
	public static void addCompositionFocusListener(CompositionFocusListener l) {
		compositionFocusListeners.add(l);
	}
	
	public static void removeCompositionFocusListener(CompositionFocusListener l) {
		compositionFocusListeners.remove(l);
	}
	
	public static void focusComposition(Composition newFocus) {
		Composition oldFocus = focusedComposition;
		focusedComposition = newFocus;
		notifyCompositionFocusListeners(oldFocus, newFocus);
	}
	
	public static void makeCompositionDirty(Composition comp) {
		renderer.makeCompositionDirty(comp);
	}
	
	public static Composition getFocusedComposition() {
		return focusedComposition;
	}
	
	private static void notifyCompositionFocusListeners(Composition oldFocus, Composition newFocus) {
		for(CompositionFocusListener l : compositionFocusListeners) {
			l.focusedCompositionChanged(oldFocus, newFocus);
		}
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
}
