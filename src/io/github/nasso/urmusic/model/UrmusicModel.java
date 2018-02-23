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
import io.github.nasso.urmusic.model.project.VideoEffect;
import io.github.nasso.urmusic.model.project.VideoEffectInstance;
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
	private static List<TrackEffect> loadedEffectsUnmodifiable = Collections.unmodifiableList(UrmusicModel.loadedEffects);
	
	public static void init() {
		// TODO: User prefs
		UrmusicModel.renderer = new Renderer(200);
		
		UrmusicModel.renderer.addRendererListener(new RendererListener() {
			public void frameRendered(Composition comp, int frame) {
			}
			
			public void effectLoaded(TrackEffect fx) {
				synchronized(UrmusicModel.loadedEffects) {
					UrmusicModel.loadedEffects.add(fx);
				}
			}
			
			public void effectUnloaded(TrackEffect fx) {
				synchronized(UrmusicModel.loadedEffects) {
					UrmusicModel.loadedEffects.remove(fx);
				}
			}
		});
		
		for(TrackEffect fx : UrmusicModel.STOCK_EFFECTS) {
			UrmusicModel.loadEffect(fx);
		}
		
		UrmusicModel.loadProject(null);
		
		UrmusicModel.playbackThread = new PlaybackThread();
		UrmusicModel.playbackThread.setFPS(UrmusicModel.project.getMainComposition().getTimeline().getFramerate());
	}
	
	public static void exit() {
		UrmusicView.saveViewState();
		
		UrmusicModel.loadProject(null);
		UrmusicModel.renderer.dispose();
		
		UrmusicView.dispose();
		
		System.exit(0);
	}
	
	public static boolean isEffectLoaded(TrackEffect fx) {
		synchronized(UrmusicModel.loadedEffects) {
			return UrmusicModel.loadedEffects.contains(fx);
		}
	}
	
	public static void loadEffect(TrackEffect fx) {
		if(UrmusicModel.isEffectLoaded(fx)) return;
		
		fx.effectMain();
		if(fx instanceof VideoEffect) {
			UrmusicModel.renderer.initEffect(fx);
		}
	}
	
	public static void unloadEffect(TrackEffect fx) {
		if(!UrmusicModel.isEffectLoaded(fx)) return;

		if(fx instanceof VideoEffect) {
			UrmusicModel.renderer.disposeEffect(fx);
		}
	}
	
	public static List<TrackEffect> getLoadedEffects() {
		return UrmusicModel.loadedEffectsUnmodifiable;
	}
	
	public static void loadProject(File f) {
		// TODO: project management
		if(UrmusicModel.project != null) {
			UrmusicModel.project.getMainComposition().dispose();
			UrmusicModel.notifyProjectUnloaded(UrmusicModel.project);
		}
		
		if(f == null) {
			UrmusicModel.project = new Project();
			UrmusicModel.renderer.queueFrameASAP(UrmusicModel.project.getMainComposition(), 0);
			
			UrmusicModel.notifyProjectLoaded(UrmusicModel.project);
		}
	}
	
	public static Renderer getRenderer() {
		return UrmusicModel.renderer;
	}
	
	public static Project getCurrentProject() {
		return UrmusicModel.project;
	}
	
	public static void makeCompositionDirty(Composition comp) {
		UrmusicModel.renderer.makeCompositionDirty(comp);
	}
	
	public static int getFrameCursor() {
		return UrmusicModel.frameCursor;
	}

	public static void setFrameCursor(int frameCursor) {
		frameCursor = Math.min(UrmusicModel.getCurrentProject().getMainComposition().getTimeline().getLength() - 1, Math.max(frameCursor, 0));
		
		if(UrmusicModel.frameCursor == frameCursor) return;
		int before = UrmusicModel.frameCursor;
		UrmusicModel.notifyFrameCursorChange(before, UrmusicModel.frameCursor = frameCursor);
	}
	
	public static boolean isPlayingBack() {
		return UrmusicModel.playbackThread.isPlayingBack();
	}
	
	public static void startPlayback() {
		UrmusicModel.playbackThread.startPlayback();
	}
	
	public static void stopPlayback() {
		UrmusicModel.playbackThread.stopPlayback();
	}

	public static void disposeTrack(Track track) {
		UrmusicModel.renderer.disposeTrack(track);
	}
	
	public static void disposeEffect(TrackEffectInstance fx) {
		if(fx instanceof VideoEffectInstance) {
			UrmusicModel.renderer.disposeEffectInstance(fx);
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
		UrmusicModel.disposeTrack(t);
	}
	
	// -- Listeners --
	private static List<ProjectLoadingListener> projectLoadingListeners = new ArrayList<>();
	public static void addProjectLoadingListener(ProjectLoadingListener l) {
		UrmusicModel.projectLoadingListeners.add(l);
	}
	
	public static void removeProjectLoadingListener(ProjectLoadingListener l) {
		UrmusicModel.projectLoadingListeners.remove(l);
	}

	private static void notifyProjectLoaded(Project p) {
		for(ProjectLoadingListener l : UrmusicModel.projectLoadingListeners) {
			l.projectLoaded(p);
		}
	}

	private static void notifyProjectUnloaded(Project p) {
		for(ProjectLoadingListener l : UrmusicModel.projectLoadingListeners) {
			l.projectUnloaded(p);
		}
	}
	
	private static List<FrameCursorListener> frameCursorListeners = new ArrayList<>();
	public static void addFrameCursorListener(FrameCursorListener l) {
		UrmusicModel.frameCursorListeners.add(l);
	}
	
	public static void removeFrameCursorListener(FrameCursorListener l) {
		UrmusicModel.frameCursorListeners.remove(l);
	}

	private static void notifyFrameCursorChange(int before, int after) {
		for(FrameCursorListener l : UrmusicModel.frameCursorListeners) {
			l.frameChanged(before, after);
		}
	}
}
