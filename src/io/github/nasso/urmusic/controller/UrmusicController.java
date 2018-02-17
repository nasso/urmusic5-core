package io.github.nasso.urmusic.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.nasso.urmusic.common.event.FocusListener;
import io.github.nasso.urmusic.common.event.MultiFocusListener;
import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.project.Composition;
import io.github.nasso.urmusic.model.project.ProjectFileSystem;
import io.github.nasso.urmusic.model.project.Track;
import io.github.nasso.urmusic.model.project.Track.TrackActivityRange;
import io.github.nasso.urmusic.model.project.TrackEffect;
import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.github.nasso.urmusic.model.project.param.EffectParam;
import io.github.nasso.urmusic.model.project.param.KeyFrame;

public class UrmusicController {
	private UrmusicController() { }
	
	public static void init() {
		focusComposition(UrmusicModel.getCurrentProject().getMainComposition());
		
		UrmusicModel.addFrameCursorListener((oldPosition, newPosition)  -> {
			UrmusicModel.getRenderer().queueFrameASAP(getFocusedComposition(), newPosition);
		});
		
		addTrackEffectInstanceFocusListener(new FocusListener<TrackEffect.TrackEffectInstance>() {
			public void focusChanged(TrackEffectInstance oldFocus, TrackEffectInstance newFocus) {
				toggleFocusEffectParameter(null, false);
				
				if(newFocus == null) return;
				
				for(EffectParam<?> param : newFocus.getParameterListUnmodifiable()) {
					toggleFocusEffectParameter(param, true);
				}
			}
		});
	}
	
	public static void requestExit() {
		UrmusicModel.exit();
	}
	
	// -- Frame control --
	public static void setFramePosition(int frame) {
		if(UrmusicModel.isPlayingBack()) UrmusicModel.stopPlayback();
		
		UrmusicModel.setFrameCursor(frame);
	}
	
	public static void frameAdvance() {
		UrmusicModel.setFrameCursor(UrmusicModel.getFrameCursor() + 1);
	}
	
	public static void frameBack() {
		UrmusicModel.setFrameCursor(UrmusicModel.getFrameCursor() - 1);
	}

	public static void goToNextKeyFrame() {
		if(getFocusedEffectParameters().isEmpty()) return;
		
		int f = Integer.MAX_VALUE;
		boolean set = false;
		
		for(EffectParam<?> param : getFocusedEffectParameters()) {
			if(param == null) return;

			KeyFrame<?> kf = param.getKeyFrameAfter(UrmusicModel.getFrameCursor());
			if(kf != null) f = Math.min(f, kf.getFrame());
			
			set |= kf != null;
		}
		
		if(set) setFramePosition(f);
	}
	
	public static void goToPreviousKeyFrame() {
		if(getFocusedEffectParameters().isEmpty()) return;
		
		int f = Integer.MIN_VALUE;
		boolean set = false;
		
		for(EffectParam<?> param : getFocusedEffectParameters()) {
			if(param == null) return;

			KeyFrame<?> kf = param.getKeyFrameBefore(UrmusicModel.getFrameCursor());
			if(kf != null) f = Math.max(f, kf.getFrame());
			
			set |= kf != null;
		}
		
		if(set) setFramePosition(f);
	}
	
	public static void playPause() {
		if(UrmusicModel.isPlayingBack()) UrmusicModel.stopPlayback();
		else UrmusicModel.startPlayback();
	}
	
	// -- Project --
	public static void importFile(String absoluteFilePath) {
		ProjectFileSystem fs = UrmusicModel.getCurrentProject().getFileSystem();
		fs.root().add(fs.createFile(absoluteFilePath));
	}
	
	public static void newDirectory(String dirName) {
		ProjectFileSystem fs = UrmusicModel.getCurrentProject().getFileSystem();
		fs.root().add(fs.createDirectory(dirName));
	}
	
	// -- Edit --
	public static void addEffect(TrackEffect e) {
		if(e == null) return;
		
		Track t = getFocusedTrack();
		if(t == null) return;
		
		t.addEffect(e.instance());
	}
	
	public static void addEffects(List<TrackEffect> elist) {
		if(elist == null || elist.isEmpty()) return;
		
		Track t = getFocusedTrack();
		if(t == null) return;
		
		for(TrackEffect e : elist)
			t.addEffect(e.instance());
	}
	
	public static void addTrack() {
		Composition comp = getFocusedComposition();
		if(comp == null) return;
		
		comp.getTimeline().addTrack(new Track(comp.getTimeline().getLength()));
	}
	
	public static void splitTrack() {
		Track t = getFocusedTrack();
		
		if(t != null) {
			t.splitAt(UrmusicModel.getFrameCursor());
		}
	}
	
	public static void deleteTrack(Track t) {
		Composition comp = getFocusedComposition();
		
		if(comp == null) return;
		if(t == null) return;
		
		int i = comp.getTimeline().getTracks().indexOf(t);
		
		if(i < 0) return;
		
		UrmusicModel.deleteTrack(comp, i);
	}
	
	public static void deleteFocusedTrackActivityRange() {
		TrackActivityRange r = getFocusedTrackActivityRange();
		focusTrackActivityRange(null);
		UrmusicModel.deleteTrackActivityRange(r);
	}
	
	public static void deleteFocusedTrack() {
		Track t = getFocusedTrack();
		focusTrack(null);
		deleteTrack(t);
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
	
	// Track Effect Instance
	private static TrackEffectInstance focusedEffect = null;
	private static List<FocusListener<TrackEffectInstance>> trackEffectFocusListeners = new ArrayList<>();
	
	public static void addTrackEffectInstanceFocusListener(FocusListener<TrackEffectInstance> l) {
		trackEffectFocusListeners.add(l);
	}
	
	public static void removeTrackEffectInstanceFocusListener(FocusListener<TrackEffectInstance> l) {
		trackEffectFocusListeners.remove(l);
	}
	
	public static void focusTrackEffectInstance(TrackEffectInstance newFocus) {
		if(focusedEffect == newFocus) return;
		
		TrackEffectInstance oldFocus = focusedEffect;
		focusedEffect = newFocus;
		
		for(FocusListener<TrackEffectInstance> l : trackEffectFocusListeners) {
			l.focusChanged(oldFocus, newFocus);
		}
	}
	
	public static TrackEffectInstance getFocusedTrackEffectInstance() {
		return focusedEffect;
	}
	
	
	// Control parameters
	private static List<EffectParam<?>> focusedParams = new ArrayList<>();
	private static List<EffectParam<?>> focusedParamsUnmodifiable = Collections.unmodifiableList(focusedParams);
	private static List<MultiFocusListener<EffectParam<?>>> controlParamFocusListeners = new ArrayList<>();
	
	public static void addEffectParameterFocusListener(MultiFocusListener<EffectParam<?>> l) {
		controlParamFocusListeners.add(l);
	}
	
	public static void removeEffectParameterFocusListener(MultiFocusListener<EffectParam<?>> l) {
		controlParamFocusListeners.remove(l);
	}
	
	/**
	 * calling this with null and false effectively unselects everything
	 * 
	 * @param p
	 * @param multifocus If false, unfocuses the previously focused parameter
	 */
	public static void toggleFocusEffectParameter(EffectParam<?> p, boolean multifocus) {
		boolean willHaveFocus = !(p != null && focusedParams.contains(p) && focusedParams.size() > 1);
		
		if(!multifocus) {
			while(!focusedParams.isEmpty()) {
				EffectParam<?> param = focusedParams.remove(0);
				
				for(MultiFocusListener<EffectParam<?>> l : controlParamFocusListeners) {
					l.unfocused(param);
				}
			}
		}
		
		if(p == null) return;
		
		if(willHaveFocus) {
			focusedParams.add(p);
			
			for(MultiFocusListener<EffectParam<?>> l : controlParamFocusListeners) {
				l.focused(p);
			}
		} else if(multifocus) {
			focusedParams.remove(p);
			
			for(MultiFocusListener<EffectParam<?>> l : controlParamFocusListeners) {
				l.unfocused(p);
			}
		}
	}
	
	public static boolean isFocused(EffectParam<?> param) {
		return focusedParams.contains(param);
	}
	
	public static List<EffectParam<?>> getFocusedEffectParameters() {
		return focusedParamsUnmodifiable;
	}
}
