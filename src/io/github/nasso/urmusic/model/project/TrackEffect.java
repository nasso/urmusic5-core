package io.github.nasso.urmusic.model.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.event.EffectParametersListener;
import io.github.nasso.urmusic.model.project.control.ControlParam;

public abstract class TrackEffect {
	public abstract class TrackEffectInstance {
		private List<ControlParam<?>> parameters = new ArrayList<>();
		private List<ControlParam<?>> unmodifiableParameters = Collections.unmodifiableList(this.parameters);
		
		private List<EffectParametersListener> paramListeners = new ArrayList<>();
		
		public void dispose() {
			UrmusicModel.disposeEffect(this);
		}
		
		public TrackEffect getEffectClass() {
			return TrackEffect.this;
		}
		
		public void addControlParameter(ControlParam<?> ctrl) {
			this.parameters.add(ctrl);
			
			this.notifyParameterAdded(ctrl);
		}
		
		public void removeControlParameter(ControlParam<?> ctrl) {
			this.parameters.remove(ctrl);
			
			this.notifyParameterRemoved(ctrl);
		}
		
		public List<ControlParam<?>> getParameterListUnmodifiable() {
			return this.unmodifiableParameters;
		}
		
		public void addParametersListener(EffectParametersListener l) {
			this.paramListeners.add(l);
		}
		
		public void removeParametersListener(EffectParametersListener l) {
			this.paramListeners.remove(l);
		}
		
		private void notifyParameterAdded(ControlParam<?> ctrl) {
			for(EffectParametersListener l : this.paramListeners) {
				l.parameterAdded(ctrl);
			}
		}
		
		private void notifyParameterRemoved(ControlParam<?> ctrl) {
			for(EffectParametersListener l : this.paramListeners) {
				l.parameterRemoved(ctrl);
			}
		}
	}
}
