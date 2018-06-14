/*******************************************************************************
 * urmusic - The Free and Open Source Music Visualizer Tool
 * Copyright (C) 2018  nasso (https://github.com/nasso)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * Contact "nasso": nassomails -at- gmail dot com
 ******************************************************************************/
package io.gitlab.nasso.urmusic.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.gitlab.nasso.urmusic.common.ActionValidator;
import io.gitlab.nasso.urmusic.common.CancellableAction;
import io.gitlab.nasso.urmusic.common.RGBA32;
import io.gitlab.nasso.urmusic.common.event.ExportJobCallback;
import io.gitlab.nasso.urmusic.common.event.FocusListener;
import io.gitlab.nasso.urmusic.common.event.FrameCursorListener;
import io.gitlab.nasso.urmusic.common.event.MultiFocusListener;
import io.gitlab.nasso.urmusic.common.event.ProjectListener;
import io.gitlab.nasso.urmusic.common.event.ProjectLoadingListener;
import io.gitlab.nasso.urmusic.model.UrmusicModel;
import io.gitlab.nasso.urmusic.model.exporter.ExportSettings;
import io.gitlab.nasso.urmusic.model.exporter.Exporter.ExportJob;
import io.gitlab.nasso.urmusic.model.project.Composition;
import io.gitlab.nasso.urmusic.model.project.Project;
import io.gitlab.nasso.urmusic.model.project.Timeline;
import io.gitlab.nasso.urmusic.model.project.Track;
import io.gitlab.nasso.urmusic.model.project.TrackEffect;
import io.gitlab.nasso.urmusic.model.project.Track.TrackActivityRange;
import io.gitlab.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.gitlab.nasso.urmusic.model.project.TrackEffect.TrackEffectScript;
import io.gitlab.nasso.urmusic.model.project.codec.ProjectCodec;
import io.gitlab.nasso.urmusic.model.project.param.EffectParam;
import io.gitlab.nasso.urmusic.model.project.param.KeyFrame;

/**
 * Controls the model.
 * 
 * @author nasso
 */
public class UrmusicController {
	private UrmusicController() { }
	
	private static PlaybackThread playbackThread;
	
	private static int frameCursor = 0;
	
	private static Path currentProjectPath = null;
	private static boolean projectHasUnsavedChanges = false;
	
	public static void init() {
		UrmusicModel.addProjectLoadingListener(new ProjectLoadingListener() {
			public void projectUnloaded(Project p) {
			}
			
			public void projectLoaded(Project p) {
				toggleFocusEffectParameter(null, false);
				focusTrackEffectInstance(null);
				focusTrackActivityRange(null);
				focusTrack(null);
				focusComposition(p.getMainComposition());
			}
		});
		
		UrmusicModel.setProject(new Project());
		
		UrmusicController.playbackThread = new PlaybackThread();
		
		UrmusicController.addFrameCursorListener((oldPosition, newPosition)  -> {
			UrmusicModel.getVideoRenderer().queueFrameASAP(UrmusicController.getFocusedComposition(), newPosition);
		});
		
		UrmusicController.addProjectListener(new ProjectListener() {
			public void saved() {
				UrmusicController.projectHasUnsavedChanges = false;
			}
			
			public void loaded() {
				UrmusicController.projectHasUnsavedChanges = false;
			}
			
			public void changed() {
				UrmusicController.projectHasUnsavedChanges = true;
			}
		});
		
		UrmusicController.addTrackEffectInstanceFocusListener(new FocusListener<TrackEffect.TrackEffectInstance>() {
			public void focusChanged(TrackEffectInstance oldFocus, TrackEffectInstance newFocus) {
				UrmusicController.toggleFocusEffectParameter(null, false);
				
				if(newFocus == null) return;
				
				for(EffectParam<?> param : newFocus.getParameterListUnmodifiable()) {
					UrmusicController.toggleFocusEffectParameter(param, true);
				}
			}
		});
	}
	
	public static void forceImmediateVideoRender() {
		UrmusicModel.getVideoRenderer().queueFrameASAP(UrmusicModel.getCurrentProject().getMainComposition(), UrmusicController.getFrameCursor());
	}
	
	public static void markVideoDirty() {
		UrmusicModel.getVideoRenderer().makeCompositionDirty(UrmusicModel.getCurrentProject().getMainComposition());
		UrmusicController.forceImmediateVideoRender();
		
		playbackThread.restartPlayback();
	}
	
	public static ExportJob export(ExportSettings settings, ExportJobCallback callback) {
		return UrmusicModel.getExporter().start(settings, callback);
	}

	public static void cancelExport(ExportJob job) {
		job.cancel();
	}
	
	private static List<ActionValidator> exitRequestValidators = new ArrayList<>();
	public static boolean requestExit() {
		CancellableAction action = new CancellableAction();
		
		for(ActionValidator v : exitRequestValidators) {
			v.onAction(action);
			if(action.isCancelled()) return false;
		}
		
		try {
			playbackThread.waitForExit();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		
		UrmusicModel.exit();
		return true; // Should approximately never happen
	}
	
	public static void addExitRequestValidator(ActionValidator v) {
		exitRequestValidators.add(v);
	}
	
	public static void removeExitRequestValidator(ActionValidator v) {
		exitRequestValidators.remove(v);
	}
	
	// -- Playback & Frame Control --
	private static List<FrameCursorListener> frameCursorListeners = new ArrayList<>();
	public static int getFrameCursor() {
		return UrmusicController.frameCursor;
	}

	public static void setFrameCursor(int frameCursor) {
		frameCursor = Math.min(UrmusicModel.getCurrentProject().getMainComposition().getTimeline().getTotalFrameCount() - 1, Math.max(frameCursor, 0));
		
		if(UrmusicController.frameCursor == frameCursor) return;
		int before = frameCursor;
		UrmusicController.notifyFrameCursorChange(before, UrmusicController.frameCursor = frameCursor);
	}
	
	public static boolean isPlayingBack() {
		return UrmusicController.playbackThread.isPlayingBack();
	}
	
	public static void startPlayback() {
		UrmusicController.playbackThread.startPlayback();
	}
	
	public static void stopPlayback() {
		UrmusicController.playbackThread.stopPlayback();
	}

	private static void notifyFrameCursorChange(int before, int after) {
		for(FrameCursorListener l : UrmusicController.frameCursorListeners) {
			l.frameChanged(before, after);
		}
	}
	
	public static void addFrameCursorListener(FrameCursorListener l) {
		UrmusicController.frameCursorListeners.add(l);
	}
	
	public static void removeFrameCursorListener(FrameCursorListener l) {
		UrmusicController.frameCursorListeners.remove(l);
	}
	
	public static float getTimePosition() {
		return UrmusicController.getFrameCursor() / UrmusicController.getFocusedComposition().getTimeline().getFramerate();
	}
	
	public static void setFramePosition(int frame) {
		if(UrmusicController.isPlayingBack()) UrmusicController.stopPlayback();
		
		UrmusicController.setFrameCursor(frame);
	}
	
	public static void setTimePosition(float time) {
		UrmusicController.setFramePosition(Math.round(UrmusicModel.getCurrentProject().getMainComposition().getTimeline().getFramerate() * time));
	}
	
	public static void frameAdvance() {
		UrmusicController.setFrameCursor(UrmusicController.getFrameCursor() + 1);
	}
	
	public static void frameBack() {
		UrmusicController.setFrameCursor(UrmusicController.getFrameCursor() - 1);
	}

	public static void goToNextKeyFrame() {
		if(UrmusicController.getFocusedEffectParameters().isEmpty()) return;
		
		float t = Float.MAX_VALUE;
		boolean set = false;
		
		for(EffectParam<?> param : UrmusicController.getFocusedEffectParameters()) {
			if(param == null) return;

			KeyFrame<?> kf = param.getKeyFrameAfter(UrmusicController.getTimePosition());
			if(kf != null) t = Math.min(t, kf.getPosition());
			
			set |= kf != null;
		}
		
		if(set) UrmusicController.setTimePosition(t);
	}
	
	public static void goToPreviousKeyFrame() {
		if(UrmusicController.getFocusedEffectParameters().isEmpty()) return;
		
		float t = Float.MIN_VALUE;
		boolean set = false;
		
		for(EffectParam<?> param : UrmusicController.getFocusedEffectParameters()) {
			if(param == null) return;

			KeyFrame<?> kf = param.getKeyFrameBefore(UrmusicController.getTimePosition());
			if(kf != null) t = Math.max(t, kf.getPosition());
			
			set |= kf != null;
		}
		
		if(set) UrmusicController.setTimePosition(t);
	}
	
	public static void playPause() {
		if(UrmusicController.isPlayingBack()) UrmusicController.stopPlayback();
		else UrmusicController.startPlayback();
	}
	
	// -- Project --
	private static List<ProjectListener> projectListeners = new ArrayList<>();
	public static void setCurrentSong(Path filePath, Runnable callback) {
		UrmusicModel.getAudioRenderer().setAudioBufferSource(filePath, () -> {
			markVideoDirty();
			callback.run();
		});
	}
	
	public static Path getCurrentSong() {
		return UrmusicModel.getAudioRenderer().getAudioBufferSource();
	}
	
	public static Path getCurrentProjectPath() {
		return UrmusicController.currentProjectPath;
	}
	
	public static void newProject() {
		UrmusicModel.setProject(new Project());
		UrmusicController.currentProjectPath = null;
		notifyProjectLoaded();
	}

	public static void saveCurrentProject() {
		saveCurrentProject(getCurrentProjectPath());
	}
	
	public static void saveCurrentProject(Path where) {
		if(where == null) return;
		
		try(OutputStream out = new BufferedOutputStream(Files.newOutputStream(where))) {
			ProjectCodec.save(UrmusicModel.getCurrentProject(), out);
			UrmusicController.currentProjectPath = where.toAbsolutePath();
			notifyProjectSaved();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void openProject(Path where) {
		try(BufferedInputStream in = new BufferedInputStream(Files.newInputStream(where))) {
			UrmusicModel.setProject(ProjectCodec.load(in));
			UrmusicController.currentProjectPath = where.toAbsolutePath();
			notifyProjectLoaded();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean projectHasUnsavedChanges() {
		return projectHasUnsavedChanges;
	}
	
	public static void addProjectListener(ProjectListener l) {
		if(!projectListeners.contains(l)) projectListeners.add(l);
	}
	
	public static void removeProjectListener(ProjectListener l) {
		projectListeners.remove(l);
	}
	
	private static void notifyProjectChanged() {
		for(ProjectListener l : projectListeners) {
			l.changed();
		}
	}
	
	private static void notifyProjectSaved() {
		for(ProjectListener l : projectListeners) {
			l.saved();
		}
	}
	
	private static void notifyProjectLoaded() {
		for(ProjectListener l : projectListeners) {
			l.loaded();
		}
	}
	
	// -- Edit --
	public static void updateProjectSettings(int frameWidth, int frameHeight, float framerate, float duration, RGBA32 rgba) {
		UrmusicModel.getCurrentProject().getMainComposition().setWidth(frameWidth);
		UrmusicModel.getCurrentProject().getMainComposition().setHeight(frameHeight);
		UrmusicModel.getCurrentProject().getMainComposition().getTimeline().setFramerate(framerate);
		UrmusicModel.getCurrentProject().getMainComposition().getTimeline().setDuration(duration);
		UrmusicModel.getCurrentProject().getMainComposition().setClearColor(rgba);

		UrmusicController.notifyProjectChanged();
		UrmusicController.markVideoDirty();
	}
	
	public static <T> T getParamValueNow(EffectParam<T> param) {
		return param.getValue(UrmusicController.getTimePosition());
	}
	
	public static <T> void setParamValueNow(EffectParam<T> param, T value) {
		param.setValue(value, UrmusicController.getTimePosition());
		
		UrmusicController.notifyProjectChanged();
		UrmusicController.markVideoDirty();
	}

	public static <T> void toggleKeyFrame(EffectParam<T> param) {
		KeyFrame<T> kf;
		if((kf = param.getKeyFrameAt(UrmusicController.getTimePosition())) != null)
			param.removeKeyFrame(kf);
		else
			param.addKeyFrame(UrmusicController.getTimePosition());

		UrmusicController.notifyProjectChanged();
		UrmusicController.markVideoDirty();
	}
	
	public static void addEffect(TrackEffect e) {
		if(e == null) return;
		
		Track t = UrmusicController.getFocusedTrack();
		if(t == null) return;
		
		t.addEffect(e.instance());
		
		UrmusicController.notifyProjectChanged();
		UrmusicController.markVideoDirty();
	}
	
	public static void addEffects(List<TrackEffect> elist) {
		if(elist == null || elist.isEmpty()) return;
		
		Track t = UrmusicController.getFocusedTrack();
		if(t == null) return;
		
		for(TrackEffect e : elist)
			t.addEffect(e.instance());

		UrmusicController.notifyProjectChanged();
		UrmusicController.markVideoDirty();
	}
	
	public static void duplicateTrackEffect(Track track, TrackEffectInstance fx) {
		int i = track.getEffects().indexOf(fx) + 1;
		
		TrackEffectInstance copy = UrmusicModel.instanciateEffectById(fx.getEffectClass().getEffectClassID());
		copy.getScript().setSource(copy.getScript().getSource());
		
		for(EffectParam<?> param : fx.getParameterListUnmodifiable()) {
			EffectParam<?> paramCopy = copy.getParamByID(param.getID());
			
			if(param.isAutomated()) {
				for(KeyFrame<?> kf : param.getKeyFrames())
					paramCopy.addKeyFrame(kf.getPosition(), kf.getValue(), kf.getEasingFunction());
			} else paramCopy.setValue(param.getValue(0), 0);
		}
		
		track.addEffect(copy, i);

		UrmusicController.notifyProjectChanged();
		UrmusicController.markVideoDirty();
	}
	
	public static void moveEffect(Track track, TrackEffectInstance fx, int index) {
		track.moveEffect(track.getEffects().indexOf(fx), index);
		
		UrmusicController.notifyProjectChanged();
		UrmusicController.markVideoDirty();
	}
	
	public static void moveEffectUp(Track track, TrackEffectInstance fx) {
		UrmusicController.moveEffect(track, fx, track.getEffects().indexOf(fx) - 1);
	}
	
	public static void moveEffectDown(Track track, TrackEffectInstance fx) {
		UrmusicController.moveEffect(track, fx, track.getEffects().indexOf(fx) + 1);
	}

	public static void setEffectEnabled(TrackEffectInstance fx, boolean enabled) {
		if(fx.isEnabled() == enabled) return;

		fx.setEnabled(enabled);
		
		UrmusicController.notifyProjectChanged();
		UrmusicController.markVideoDirty();
	}
	
	public static void addTrack() {
		Composition comp = UrmusicController.getFocusedComposition();
		if(comp == null) return;
		
		comp.getTimeline().addTrack(new Track(comp.getTimeline().getDuration()));

		UrmusicController.notifyProjectChanged();
		// We technically don't have to render after adding an empty track
		// UrmusicController.forceImmediateRender();
	}
	
	public static void setTrackEnabled(Track t, boolean isEnabled) {
		t.setEnabled(isEnabled);

		UrmusicController.notifyProjectChanged();
		UrmusicController.markVideoDirty();
	}
	
	public static void moveTrack(Timeline tl, Track t, int i) {
		if(tl == null || t == null || !tl.getTracks().contains(t) || i < 0 || i >= tl.getTracks().size()) return;
		
		tl.moveTrack(tl.getTracks().indexOf(t), i);

		UrmusicController.notifyProjectChanged();
		UrmusicController.markVideoDirty();
	}
	
	public static void moveTrackUp(Timeline tl, Track t) {
		UrmusicController.moveTrack(tl, t, tl.getTracks().indexOf(t) - 1);
	}
	
	public static void moveTrackDown(Timeline tl, Track t) {
		UrmusicController.moveTrack(tl, t, tl.getTracks().indexOf(t) + 1);
	}
	
	public static void renameTrack(Track t, String newName) {
		t.setName(newName);

		UrmusicController.notifyProjectChanged();
		// No need to render
	}
	
	public static void splitTrack() {
		Track t = UrmusicController.getFocusedTrack();
		if(t == null) return;
		
		t.splitAt(UrmusicController.getTimePosition());
		
		UrmusicController.notifyProjectChanged();
		// We technically don't have to render after a simple split
		// UrmusicController.forceImmediateRender();
	}
	
	public static void moveTrackActivityRange(TrackActivityRange range, float position) {
		range.moveTo(position);

		UrmusicController.notifyProjectChanged();
		UrmusicController.markVideoDirty();
	}
	
	public static void setTrackActivityRangeStart(TrackActivityRange range, float start) {
		range.setStart(start);

		UrmusicController.notifyProjectChanged();
		UrmusicController.markVideoDirty();
	}
	
	public static void setTrackActivityRangeEnd(TrackActivityRange range, float end) {
		range.setEnd(end);

		UrmusicController.notifyProjectChanged();
		UrmusicController.markVideoDirty();
	}
	
	public static void deleteTrack(Track t) {
		Composition comp = UrmusicController.getFocusedComposition();
		
		if(comp == null) return;
		if(t == null) return;
		
		if(!comp.getTimeline().getTracks().contains(t)) return;
		
		if(UrmusicController.focusedTrack == t)
			UrmusicController.focusTrack(null);
		
		if(t.getEffects().contains(UrmusicController.focusedEffect))
			UrmusicController.focusTrackEffectInstance(null);
		
		comp.getTimeline().removeTrack(t);
		UrmusicModel.disposeTrack(t);

		UrmusicController.notifyProjectChanged();
		UrmusicController.markVideoDirty();
	}
	
	public static void deleteTrackEffect(Track t, TrackEffectInstance fx) {
		for(int i = 0; i < fx.getParameterCount(); i++) {
			EffectParam<?> param = fx.getParameter(i);
			
			if(UrmusicController.isFocused(param)) UrmusicController.toggleFocusEffectParameter(param, true);
		}
		
		if(UrmusicController.focusedEffect == fx)
			UrmusicController.focusTrackEffectInstance(null);
		
		t.removeEffect(fx);
		fx.dispose();

		UrmusicController.notifyProjectChanged();
		UrmusicController.markVideoDirty();
	}
	
	public static void deleteFocusedTrackActivityRange() {
		TrackActivityRange r = UrmusicController.getFocusedTrackActivityRange();
		deleteTrackActivityRange(r);
	}
	
	public static void deleteTrackActivityRange(TrackActivityRange r) {
		if(r == null) return;
		if(r == UrmusicController.getFocusedTrackActivityRange()) UrmusicController.focusTrackActivityRange(null);
		r.getTrack().removeActiveRange(r);
		
		UrmusicController.notifyProjectChanged();
		UrmusicController.markVideoDirty();
	}
	
	public static void deleteFocusedTrack() {
		UrmusicController.deleteTrack(UrmusicController.getFocusedTrack());
	}
	
	public static void updateScriptSource(TrackEffectScript script, String newSource) {
		if(script == null || newSource == null) return;
		
		script.setSource(newSource);

		UrmusicController.notifyProjectChanged();
		UrmusicController.markVideoDirty();
	}
	
	// -- Focus --
	// Composition
	private static Composition focusedComposition;
	private static List<FocusListener<Composition>> compFocusListeners = new ArrayList<>();
	
	public static void addCompositionFocusListener(FocusListener<Composition> l) {
		UrmusicController.compFocusListeners.add(l);
	}
	
	public static void removeCompositionFocusListener(FocusListener<Composition> l) {
		UrmusicController.compFocusListeners.remove(l);
	}
	
	public static void focusComposition(Composition newFocus) {
		if(UrmusicController.focusedComposition == newFocus) return;
		
		Composition oldFocus = UrmusicController.focusedComposition;
		UrmusicController.focusedComposition = newFocus;
		
		for(FocusListener<Composition> l : UrmusicController.compFocusListeners) {
			l.focusChanged(oldFocus, newFocus);
		}
	}
	
	public static Composition getFocusedComposition() {
		return UrmusicController.focusedComposition;
	}
	
	// Track
	private static Track focusedTrack = null;
	private static List<FocusListener<Track>> trackFocusListeners = new ArrayList<>();
	
	public static void addTrackFocusListener(FocusListener<Track> l) {
		UrmusicController.trackFocusListeners.add(l);
	}
	
	public static void removeTrackFocusListener(FocusListener<Track> l) {
		UrmusicController.trackFocusListeners.remove(l);
	}
	
	public static void focusTrack(Track newFocus) {
		if(UrmusicController.focusedTrack == newFocus) return;
		
		Track oldFocus = UrmusicController.focusedTrack;
		UrmusicController.focusedTrack = newFocus;
		
		if(UrmusicController.focusedTrackRange != null && UrmusicController.focusedTrackRange.getTrack() != newFocus) {
			UrmusicController.focusTrackActivityRange(null);
		}
		
		for(FocusListener<Track> l : UrmusicController.trackFocusListeners) {
			l.focusChanged(oldFocus, newFocus);
		}
	}
	
	public static Track getFocusedTrack() {
		return UrmusicController.focusedTrack;
	}
	
	// Track Activity Range
	private static TrackActivityRange focusedTrackRange = null;
	private static List<FocusListener<TrackActivityRange>> trackRangesFocusListeners = new ArrayList<>();
	
	public static void addTrackActivityRangeFocusListener(FocusListener<TrackActivityRange> l) {
		UrmusicController.trackRangesFocusListeners.add(l);
	}
	
	public static void removeTrackActivityRangeFocusListener(FocusListener<TrackActivityRange> l) {
		UrmusicController.trackRangesFocusListeners.remove(l);
	}
	
	public static void focusTrackActivityRange(TrackActivityRange newFocus) {
		if(UrmusicController.focusedTrackRange == newFocus) return;
		
		TrackActivityRange oldFocus = UrmusicController.focusedTrackRange;
		UrmusicController.focusedTrackRange = newFocus;
		
		if(newFocus != null && UrmusicController.focusedTrack != newFocus.getTrack()) {
			UrmusicController.focusTrack(newFocus.getTrack());
		}
		
		for(FocusListener<TrackActivityRange> l : UrmusicController.trackRangesFocusListeners) {
			l.focusChanged(oldFocus, newFocus);
		}
	}
	
	public static TrackActivityRange getFocusedTrackActivityRange() {
		return UrmusicController.focusedTrackRange;
	}
	
	// Track Effect Instance
	private static TrackEffectInstance focusedEffect = null;
	private static List<FocusListener<TrackEffectInstance>> trackEffectFocusListeners = new ArrayList<>();
	
	public static void addTrackEffectInstanceFocusListener(FocusListener<TrackEffectInstance> l) {
		UrmusicController.trackEffectFocusListeners.add(l);
	}
	
	public static void removeTrackEffectInstanceFocusListener(FocusListener<TrackEffectInstance> l) {
		UrmusicController.trackEffectFocusListeners.remove(l);
	}
	
	public static void focusTrackEffectInstance(TrackEffectInstance newFocus) {
		if(UrmusicController.focusedEffect == newFocus) return;
		
		TrackEffectInstance oldFocus = UrmusicController.focusedEffect;
		UrmusicController.focusedEffect = newFocus;
		
		for(FocusListener<TrackEffectInstance> l : UrmusicController.trackEffectFocusListeners) {
			l.focusChanged(oldFocus, newFocus);
		}
	}
	
	public static TrackEffectInstance getFocusedTrackEffectInstance() {
		return UrmusicController.focusedEffect;
	}
	
	// Control parameters
	private static List<EffectParam<?>> focusedParams = new ArrayList<>();
	private static List<EffectParam<?>> focusedParamsUnmodifiable = Collections.unmodifiableList(UrmusicController.focusedParams);
	private static List<MultiFocusListener<EffectParam<?>>> controlParamFocusListeners = new ArrayList<>();
	
	public static void addEffectParameterFocusListener(MultiFocusListener<EffectParam<?>> l) {
		UrmusicController.controlParamFocusListeners.add(l);
	}
	
	public static void removeEffectParameterFocusListener(MultiFocusListener<EffectParam<?>> l) {
		UrmusicController.controlParamFocusListeners.remove(l);
	}
	
	/**
	 * calling this with null and false effectively unselects everything
	 * 
	 * @param p
	 * @param keepSelection If false, unfocuses the previously focused parameters
	 */
	public static void toggleFocusEffectParameter(EffectParam<?> p, boolean keepSelection) {
		boolean willHaveFocus = !(p != null && UrmusicController.focusedParams.contains(p) && UrmusicController.focusedParams.size() > 1);
		
		if(!keepSelection) {
			while(!UrmusicController.focusedParams.isEmpty()) {
				EffectParam<?> param = UrmusicController.focusedParams.remove(0);
				
				for(MultiFocusListener<EffectParam<?>> l : UrmusicController.controlParamFocusListeners) {
					l.unfocused(param);
				}
			}
		}
		
		if(p == null) return;
		
		if(willHaveFocus) {
			UrmusicController.focusedParams.add(p);
			
			for(MultiFocusListener<EffectParam<?>> l : UrmusicController.controlParamFocusListeners) {
				l.focused(p);
			}
		} else if(keepSelection) {
			UrmusicController.focusedParams.remove(p);
			
			for(MultiFocusListener<EffectParam<?>> l : UrmusicController.controlParamFocusListeners) {
				l.unfocused(p);
			}
		}
	}
	
	public static boolean isFocused(EffectParam<?> param) {
		return UrmusicController.focusedParams.contains(param);
	}
	
	public static List<EffectParam<?>> getFocusedEffectParameters() {
		return UrmusicController.focusedParamsUnmodifiable;
	}
}
