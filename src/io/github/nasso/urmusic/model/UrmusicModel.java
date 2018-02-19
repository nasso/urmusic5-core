package io.github.nasso.urmusic.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.nasso.urmusic.common.event.FrameCursorListener;
import io.github.nasso.urmusic.common.event.ProjectLoadingListener;
import io.github.nasso.urmusic.common.event.RendererListener;
import io.github.nasso.urmusic.model.effect.AffineTransformVFX;
import io.github.nasso.urmusic.model.effect.CircleMaskVFX;
import io.github.nasso.urmusic.model.effect.ImageDisplayVFX;
import io.github.nasso.urmusic.model.effect.RectangleMaskVFX;
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
		new ImageDisplayVFX(),
		new CircleMaskVFX(),
		new RectangleMaskVFX(),
		new AffineTransformVFX(),
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
		
		for(TrackEffect fx : STOCK_EFFECTS) {
			loadEffect(fx);
		}
		
		loadProject(null);
		
		playbackThread = new PlaybackThread();
		playbackThread.setFPS(project.getMainComposition().getTimeline().getFramerate());
	}
	
	public static void exit() {
		UrmusicView.saveViewState();
		
		loadProject(null);
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
			renderer.queueFrameASAP(project.getMainComposition(), 0);
			
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
		frameCursor = Math.min(getCurrentProject().getMainComposition().getTimeline().getLength() - 1, Math.max(frameCursor, 0));
		
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
		
		range.getTrack().removeActiveRange(range);
	}
	
	public static void deleteTrack(Composition comp, int trackIndex) {
		if(comp == null || trackIndex < 0 || trackIndex >= comp.getTimeline().getTracks().size()) return;
		
		Track t = comp.getTimeline().getTracks().get(trackIndex);
		
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
}
