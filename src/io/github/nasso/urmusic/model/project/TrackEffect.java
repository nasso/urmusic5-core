package io.github.nasso.urmusic.model.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jogamp.opengl.GL3;

import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.event.EffectParametersListener;
import io.github.nasso.urmusic.model.project.control.ControlParam;
import io.github.nasso.urmusic.model.renderer.EffectArgs;

public abstract class TrackEffect {
	public abstract class TrackEffectInstance {
		private Map<String, ControlParam<?>> parameters = new HashMap<>();
		private Map<String, ControlParam<?>> unmodifiableParameters = Collections.unmodifiableMap(this.parameters);
		
		private List<EffectParametersListener> paramListeners = new ArrayList<>();
		
		private boolean hasSetupVideo = false;
		
		public final boolean hasSetupVideo() {
			return this.hasSetupVideo || !(this.hasSetupVideo = true);
		}
		
		public void dispose() {
			UrmusicModel.disposeEffect(this);
		}
		
		public TrackEffect getEffectClass() {
			return TrackEffect.this;
		}
		
		public ControlParam<?> getControl(String name) {
			return this.parameters.get(name);
		}
		
		public void addControl(String name, ControlParam<?> ctrl) {
			this.parameters.put(name, ctrl);
			
			this.notifyParameterAdded(name, ctrl);
		}
		
		public void removeControl(String name) {
			if(!this.parameters.containsKey(name)) return;
			
			this.notifyParameterRemoved(name, this.parameters.remove(name));
		}
		
		public Map<String, ControlParam<?>> getParameterListUnmodifiable() {
			return this.unmodifiableParameters;
		}
		
		public void addParametersListener(EffectParametersListener l) {
			this.paramListeners.add(l);
		}
		
		public void removeParametersListener(EffectParametersListener l) {
			this.paramListeners.remove(l);
		}
		
		private void notifyParameterAdded(String name, ControlParam<?> ctrl) {
			for(EffectParametersListener l : this.paramListeners) {
				l.parameterAdded(name, ctrl);
			}
		}
		
		private void notifyParameterRemoved(String name, ControlParam<?> ctrl) {
			for(EffectParametersListener l : this.paramListeners) {
				l.parameterRemoved(name, ctrl);
			}
		}
		
		public abstract void setupVideo(GL3 gl);
		public abstract void applyVideo(GL3 gl, EffectArgs args);
		public abstract void disposeVideo(GL3 gl);
	}
	
	private boolean audioEffect = false, videoEffect = false;
	
	public void setAudioEffect() {
		this.audioEffect = true;
	}
	
	public void setVideoEffect() {
		this.videoEffect = true;
	}
	
	public boolean isAudioEffect() {
		return this.audioEffect;
	}
	
	public boolean isVideoEffect() {
		return this.videoEffect;
	}
	
	public abstract TrackEffectInstance instance();
	
	public abstract void globalVideoSetup(GL3 gl);
	public abstract void globalVideoDispose(GL3 gl);
	
	public abstract void effectMain();
}
