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
package io.github.nasso.urmusic.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.nasso.urmusic.common.event.ProjectLoadingListener;
import io.github.nasso.urmusic.common.event.VideoRendererListener;
import io.github.nasso.urmusic.model.effect.AffineTransformVFX;
import io.github.nasso.urmusic.model.effect.AudioScopeVFX;
import io.github.nasso.urmusic.model.effect.AudioSpectrumVFX;
import io.github.nasso.urmusic.model.effect.CircleMaskVFX;
import io.github.nasso.urmusic.model.effect.ImageDisplayVFX;
import io.github.nasso.urmusic.model.effect.PolarCoordsVFX;
import io.github.nasso.urmusic.model.effect.RectangleMaskVFX;
import io.github.nasso.urmusic.model.project.Composition;
import io.github.nasso.urmusic.model.project.Project;
import io.github.nasso.urmusic.model.project.Track;
import io.github.nasso.urmusic.model.project.Track.TrackActivityRange;
import io.github.nasso.urmusic.model.project.TrackEffect;
import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.github.nasso.urmusic.model.project.VideoEffect;
import io.github.nasso.urmusic.model.project.VideoEffectInstance;
import io.github.nasso.urmusic.model.renderer.audio.AudioRenderer;
import io.github.nasso.urmusic.model.renderer.video.VideoRenderer;
import io.github.nasso.urmusic.model.scripting.ScriptManager;
import io.github.nasso.urmusic.view.UrmusicView;

/**
 * Where stuff happens, rendering, exporting, importing...<br />
 * Controlled by the controller.
 * 
 * @author nasso
 */
public class UrmusicModel {
	public static final TrackEffect[] STOCK_EFFECTS = new TrackEffect[] {
		// Basics
		new ImageDisplayVFX(),
		new CircleMaskVFX(),
		new RectangleMaskVFX(),
		new AffineTransformVFX(),
		new PolarCoordsVFX(),
		
		// Audio
		new AudioScopeVFX(),
		new AudioSpectrumVFX(),
	};
	
	private UrmusicModel() { }

	private static Project project;
	private static VideoRenderer videoRenderer;
	private static AudioRenderer audioRenderer;
	
	private static Map<String, TrackEffect> loadedEffects = new HashMap<>();
	private static Map<String, TrackEffect> loadedEffectsUnmodifiable = Collections.unmodifiableMap(UrmusicModel.loadedEffects);
	
	public static void init() {
		try {
			ScriptManager.init();
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		// TODO: User prefs
		UrmusicModel.videoRenderer = new VideoRenderer(200);
		UrmusicModel.audioRenderer = new AudioRenderer(8192);
		
		UrmusicModel.videoRenderer.addVideoRendererListener(new VideoRendererListener() {
			public void frameRendered(Composition comp, float time) {
			}
			
			public void effectLoaded(TrackEffect fx) {
				synchronized(UrmusicModel.loadedEffects) {
					UrmusicModel.loadedEffects.put(fx.getEffectClassID(), fx);
				}
			}
			
			public void effectUnloaded(TrackEffect fx) {
				synchronized(UrmusicModel.loadedEffects) {
					UrmusicModel.loadedEffects.remove(fx.getEffectClassID());
				}
			}
		});
		
		for(TrackEffect fx : UrmusicModel.STOCK_EFFECTS) {
			UrmusicModel.loadEffect(fx);
		}
	}
	
	public static void exit() {
		UrmusicView.saveViewState();
		
		UrmusicModel.setProject(null);
		UrmusicModel.videoRenderer.dispose();
		UrmusicModel.audioRenderer.dispose();
		
		UrmusicView.dispose();
		
		System.exit(0);
	}
	
	public static boolean isEffectLoaded(TrackEffect fx) {
		synchronized(UrmusicModel.loadedEffects) {
			return UrmusicModel.loadedEffects.containsKey(fx.getEffectClassID());
		}
	}
	
	public static void loadEffect(TrackEffect fx) {
		if(UrmusicModel.isEffectLoaded(fx)) return;
		
		fx.effectMain();
		if(fx instanceof VideoEffect) {
			UrmusicModel.videoRenderer.initEffect(fx);
		}
	}
	
	public static void unloadEffect(TrackEffect fx) {
		if(!UrmusicModel.isEffectLoaded(fx)) return;

		if(fx instanceof VideoEffect) {
			UrmusicModel.videoRenderer.disposeEffect(fx);
		}
	}
	
	public static Map<String, TrackEffect> getLoadedEffects() {
		return UrmusicModel.loadedEffectsUnmodifiable;
	}
	
	public static TrackEffectInstance instanciateEffectById(String effectId) {
		if(!UrmusicModel.loadedEffects.containsKey(effectId))
			return null;
		
		return UrmusicModel.loadedEffects.get(effectId).instance();
	}
	
	public static void setProject(Project p) {
		if(UrmusicModel.project != null) {
			UrmusicModel.project.getMainComposition().dispose();
			UrmusicModel.notifyProjectUnloaded(UrmusicModel.project);
			
			UrmusicModel.project = null;
		}
		
		if(p != null) {
			UrmusicModel.project = p;
			UrmusicModel.videoRenderer.queueFrameASAP(UrmusicModel.project.getMainComposition(), 0);
			
			UrmusicModel.notifyProjectLoaded(UrmusicModel.project);
		}
	}
	
	public static VideoRenderer getVideoRenderer() {
		return UrmusicModel.videoRenderer;
	}
	
	public static AudioRenderer getAudioRenderer() {
		return UrmusicModel.audioRenderer;
	}
	
	public static Project getCurrentProject() {
		return UrmusicModel.project;
	}
	
	public static void disposeTrack(Track track) {
		for(int i = 0; i < track.getEffectCount(); i++) {
			track.getEffect(i).dispose();
		}
		
		UrmusicModel.videoRenderer.disposeTrack(track);
	}
	
	public static void disposeEffect(TrackEffectInstance fx) {
		if(fx instanceof VideoEffectInstance) {
			UrmusicModel.videoRenderer.disposeEffectInstance(fx);
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
}
