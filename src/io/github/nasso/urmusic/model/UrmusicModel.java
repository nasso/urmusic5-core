package io.github.nasso.urmusic.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.nasso.urmusic.common.event.FocusListener;
import io.github.nasso.urmusic.common.event.FrameCursorListener;
import io.github.nasso.urmusic.common.event.ProjectLoadingListener;
import io.github.nasso.urmusic.common.event.RendererListener;
import io.github.nasso.urmusic.model.effect.CircleMaskVFX;
import io.github.nasso.urmusic.model.playback.PlaybackThread;
import io.github.nasso.urmusic.model.project.Composition;
import io.github.nasso.urmusic.model.project.Project;
import io.github.nasso.urmusic.model.project.Track;
import io.github.nasso.urmusic.model.project.Track.TrackActivityRange;
import io.github.nasso.urmusic.model.project.TrackEffect;
import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.github.nasso.urmusic.model.project.control.EffectParam;
import io.github.nasso.urmusic.model.renderer.Renderer;
import io.github.nasso.urmusic.view.UrmusicView;

public class UrmusicModel {
	public static final TrackEffect[] STOCK_EFFECTS = new TrackEffect[] {
		CircleMaskVFX.FX,
	};
	
	private UrmusicModel() { }

	private static Project project;
	private static Renderer renderer;
	private static PlaybackThread playbackThread;
	
	private static int frameCursor = 0;
	
	private static List<TrackEffect> loadedEffects = new ArrayList<>();
	private static List<TrackEffect> loadedEffectsUnmodifiable = Collections.unmodifiableList(loadedEffects);
	
	public static void init() {
		// TODO: User prefs
		renderer = new Renderer(200);
		
		renderer.addRendererListener(new RendererListener() {
			public void frameRendered(Composition comp, int frame) {
			}
			
			public void effectLoaded(TrackEffect fx) {
				synchronized(loadedEffects) {
					loadedEffects.add(fx);
				}
			}
			
			public void effectUnloaded(TrackEffect fx) {
				synchronized(loadedEffects) {
					loadedEffects.remove(fx);
				}
			}
		});
		
		// -- EFFECTS
		for(TrackEffect fx : STOCK_EFFECTS) {
			loadEffect(fx);
		}
		// -- EFFECTS END
		
		addFrameCursorListener((oldPosition, newPosition)  -> {
			renderer.queueFrameASAP(focusedComposition, newPosition);
		});
		
		loadProject(null);
		
		playbackThread = new PlaybackThread();
		playbackThread.setFPS(getFocusedComposition().getTimeline().getFramerate());
		
		addCompositionFocusListener((oldComp, newComp) -> {
			playbackThread.setFPS(newComp.getTimeline().getFramerate());
		});
	}
	
	public static void exit() {
		UrmusicView.saveViewState();
		
		renderer.dispose();
		
		System.exit(0);
	}
	
	public static boolean isEffectLoaded(TrackEffect fx) {
		synchronized(loadedEffects) {
			return loadedEffects.contains(fx);
		}
	}
	
	public static void loadEffect(TrackEffect fx) {
		if(isEffectLoaded(fx)) return;
		
		fx.effectMain();
		if(fx.isVideoEffect()) {
			renderer.initEffect(fx);
		}
	}
	
	public static void unloadEffect(TrackEffect fx) {
		if(!isEffectLoaded(fx)) return;

		if(fx.isVideoEffect()) {
			renderer.disposeEffect(fx);
		}
	}
	
	public static List<TrackEffect> getLoadedEffects() {
		return loadedEffectsUnmodifiable;
	}
	
	public static void loadProject(File f) {
		// TODO: project management
		if(project != null) {
			project.getMainComposition().dispose();
			notifyProjectUnloaded(project);
		}
		
		if(f == null) {
			project = new Project();
			focusComposition(project.getMainComposition());
			renderer.queueFrameASAP(focusedComposition, 0);

			notifyProjectLoaded(project);
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
	
	public static int getFrameCursor() {
		return frameCursor;
	}

	public static void setFrameCursor(int frameCursor) {
		frameCursor = Math.min(getFocusedComposition().getTimeline().getLength() - 1, Math.max(frameCursor, 0));
		
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

	public static void disposeTrack(Track track) {
		renderer.disposeTrack(track);
	}
	
	public static void disposeEffect(TrackEffectInstance fx) {
		if(fx.getEffectClass().isVideoEffect()) {
			renderer.disposeEffectInstance(fx);
		}
	}
	
	// -- Edit --
	public static void deleteTrackActivityRange(TrackActivityRange range) {
		if(range == null) return;
		if(focusedTrackRange == range) focusTrackActivityRange(null);
		
		range.getTrack().removeActiveRange(range);
	}
	
	public static void deleteTrack(Composition comp, int trackIndex) {
		if(comp == null || trackIndex < 0 || trackIndex >= comp.getTimeline().getTracks().size()) return;
		
		Track t = comp.getTimeline().getTracks().get(trackIndex);
		if(focusedTrack == t) focusTrack(null);
		
		comp.getTimeline().removeTrack(trackIndex);
		disposeTrack(t);
	}
	
	// -- Listeners --
	private static List<ProjectLoadingListener> projectLoadingListeners = new ArrayList<>();
	public static void addProjectLoadingListener(ProjectLoadingListener l) {
		projectLoadingListeners.add(l);
	}
	
	public static void removeProjectLoadingListener(ProjectLoadingListener l) {
		projectLoadingListeners.remove(l);
	}

	private static void notifyProjectLoaded(Project p) {
		for(ProjectLoadingListener l : projectLoadingListeners) {
			l.projectLoaded(p);
		}
	}

	private static void notifyProjectUnloaded(Project p) {
		for(ProjectLoadingListener l : projectLoadingListeners) {
			l.projectUnloaded(p);
		}
	}
	
	private static List<FrameCursorListener> frameCursorListeners = new ArrayList<>();
	public static void addFrameCursorListener(FrameCursorListener l) {
		frameCursorListeners.add(l);
	}
	
	public static void removeFrameCursorListener(FrameCursorListener l) {
		frameCursorListeners.remove(l);
	}

	private static void notifyFrameCursorChange(int before, int after) {
		for(FrameCursorListener l : frameCursorListeners) {
			l.frameChanged(before, after);
		}
	}
	
	// -- Focus
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
		if(focusedComposition == newFocus) return;
		
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
		if(focusedTrack == newFocus) return;
		
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
		if(focusedTrackRange == newFocus) return;
		
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
	
	// Control parameter
	private static EffectParam<?> focusedParam = null;
	private static List<FocusListener<EffectParam<?>>> controlParamFocusListeners = new ArrayList<>();
	
	public static void addEffectParameterFocusListener(FocusListener<EffectParam<?>> l) {
		controlParamFocusListeners.add(l);
	}
	
	public static void removeEffectParameterFocusListener(FocusListener<EffectParam<?>> l) {
		controlParamFocusListeners.remove(l);
	}
	
	public static void focusEffectParameter(EffectParam<?> newFocus) {
		if(focusedParam == newFocus) return;
		
		EffectParam<?> oldFocus = focusedParam;
		focusedParam = newFocus;
		
		for(FocusListener<EffectParam<?>> l : controlParamFocusListeners) {
			l.focusChanged(oldFocus, newFocus);
		}
	}
	
	public static EffectParam<?> getFocusedEffectParameter() {
		return focusedParam;
	}
}
