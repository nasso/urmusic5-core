package io.github.nasso.urmusic.model.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.nasso.urmusic.common.easing.EasingFunction;
import io.github.nasso.urmusic.common.event.EffectInstanceListener;
import io.github.nasso.urmusic.common.event.EffectParamListener;
import io.github.nasso.urmusic.common.event.KeyFrameListener;
import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.project.param.EffectParam;
import io.github.nasso.urmusic.model.project.param.KeyFrame;

public abstract class TrackEffect {
	@SuppressWarnings({"rawtypes", "unchecked"})
	public abstract class TrackEffectInstance implements EffectParamListener, KeyFrameListener {
		private List<EffectParam<?>> parameters = new ArrayList<>();
		private List<EffectParam<?>> unmodifiableParameters = Collections.unmodifiableList(this.parameters);
		
		private List<EffectInstanceListener> listeners = new ArrayList<>();
		
		private boolean enabled = true;
		
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
		
		public EffectParam<?> getParamByName(String name) {
			for(EffectParam<?> param : this.parameters) {
				if(param.getName().equals(name))
					return param;
			}
			
			return null;
		}
		
		public EffectParam<?> getParameter(int i) {
			return this.parameters.get(i);
		}
		
		public int getParameterCount() {
			return this.parameters.size();
		}
		
		public void addParameter(EffectParam<?> param) {
			this.addParameter(param, this.parameters.size());
		}
		
		public void addParameter(EffectParam<?> param, int i) {
			this.parameters.add(i, param);
			param.addEffectParamListener(this);
			
			this.notifyParameterAdded(param, i);
		}
		
		public void removeParameter(EffectParam<?> param) {
			int i = this.parameters.indexOf(param);
			if(i < 0) return;
			
			this.notifyParameterRemoved(this.parameters.remove(i), i);
		}
		
		public List<EffectParam<?>> getParameterListUnmodifiable() {
			return this.unmodifiableParameters;
		}
		
		public void valueChanged(EffectParam source, Object newVal) {
			this.markDirty();
		}

		public void keyFrameAdded(EffectParam source, KeyFrame kf) {
			kf.addKeyFrameListener(this);
			this.markDirty();
		}

		public void keyFrameRemoved(EffectParam source, KeyFrame kf) {
			kf.removeKeyFrameListener(this);
			this.markDirty();
		}
		
		public void valueChanged(KeyFrame source, Object newValue) {
			this.markDirty();
		}
		
		public void frameChanged(KeyFrame source, int newFrame) {
			this.markDirty();
		}
		
		public void interpChanged(KeyFrame source, EasingFunction newInterp) {
			this.markDirty();
		}
		
		public void addEffectInstanceListener(EffectInstanceListener l) {
			this.listeners.add(l);
		}
		
		public void removeEffectInstanceListener(EffectInstanceListener l) {
			this.listeners.remove(l);
		}
		
		public void markDirty() {
			this.notifyDirtyFlag();
		}
		
		private void notifyDirtyFlag() {
			for(EffectInstanceListener l : this.listeners) {
				l.dirtyFlagged(this);
			}
		}
		
		private void notifyEnabledStateChanged() {
			for(EffectInstanceListener l : this.listeners) {
				l.enabledStateChanged(this, this.isEnabled());
			}
		}
		
		private void notifyParameterAdded(EffectParam<?> param, int i) {
			for(EffectInstanceListener l : this.listeners) {
				l.parameterAdded(this, i, param);
			}
		}
		
		private void notifyParameterRemoved(EffectParam<?> param, int i) {
			for(EffectInstanceListener l : this.listeners) {
				l.parameterRemoved(this, i, param);
			}
		}
	}
	
	public TrackEffect() {
	}
	
	public abstract TrackEffectInstance instance();
	
	public abstract void effectMain();
	
	public abstract String getEffectClassName();
}
