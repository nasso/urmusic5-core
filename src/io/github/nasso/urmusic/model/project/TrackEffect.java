package io.github.nasso.urmusic.model.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jogamp.opengl.GL3;

import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.event.EffectInstanceListener;
import io.github.nasso.urmusic.model.project.control.ControlParam;
import io.github.nasso.urmusic.model.renderer.EffectArgs;

public abstract class TrackEffect {
	public abstract class TrackEffectInstance {
		private Map<String, ControlParam<?>> parameters = new HashMap<>();
		private Map<String, ControlParam<?>> unmodifiableParameters = Collections.unmodifiableMap(this.parameters);
		
		private List<EffectInstanceListener> listeners = new ArrayList<>();
		
		private boolean enabled = true;
		private boolean hasSetupVideo = false;
		
		/**
		 * Assumes the video is setup just after the first call to this method.
		 * @return
		 */
		public final boolean hasSetupVideo() {
			return this.hasSetupVideo || !(this.hasSetupVideo = true);
		}
		
		public boolean isEnabled() {
			return this.enabled;
		}

		public void setEnabled(boolean enabled) {
			if(this.enabled == enabled) return;
			
			this.enabled = enabled;
			this.notifyEnabledStateChanged();
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
		
		public void addParametersListener(EffectInstanceListener l) {
			this.listeners.add(l);
		}
		
		public void removeParametersListener(EffectInstanceListener l) {
			this.listeners.remove(l);
		}
		
		private void notifyEnabledStateChanged() {
			for(EffectInstanceListener l : this.listeners) {
				l.enabledStateChanged(this, this.isEnabled());
			}
		}
		
		private void notifyParameterAdded(String name, ControlParam<?> ctrl) {
			for(EffectInstanceListener l : this.listeners) {
				l.parameterAdded(this, name, ctrl);
			}
		}
		
		private void notifyParameterRemoved(String name, ControlParam<?> ctrl) {
			for(EffectInstanceListener l : this.listeners) {
				l.parameterRemoved(this, name, ctrl);
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
	
	public abstract String getEffectClassName();
}
