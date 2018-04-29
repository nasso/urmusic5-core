package io.github.nasso.urmusic.model.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.nasso.urmusic.common.event.EffectInstanceListener;
import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.project.param.EffectParam;
import io.github.nasso.urmusic.model.scripting.Script;
import jdk.nashorn.api.scripting.NashornException;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

public abstract class TrackEffect {
	public class TrackEffectScript extends Script {
		private Object[] args = { 0.0f, 0, 0 };
		
		private Map<String, Object> ctxCopy = new HashMap<>();
		private ScriptObjectMirror fUpdate;
		
		public TrackEffectScript(TrackEffectInstance fx) {
			StringBuilder src = new StringBuilder();
			
			src.append("//\n// Properties of this effect:\n");

			for(int i = 0; i < fx.getParameterCount(); i++) {
				EffectParam<?> param = fx.getParameter(i);
				src.append("// - ").append(param.getID()).append("\n");
			}
			src.append("//\n"
					+ "\n"
					+ "//\n"
					+ "// Called before rendering each frame.\n"
					+ "// \"this\" corresponds to the effect.\n"
					+ "// Default values are key-frame animated.\n"
					+ "//\n"
					+ "// Arguments:\n"
					+ "// - time: The current time, in second.\n"
					+ "// - width: Width of the framebuffer, in pixels.\n"
					+ "// - height: Height of the framebuffer, in pixels.\n"
					+ "//\n"
					+ "function update(time, width, height) {\n"
						+ "\t// Code here\n"
					+ "}\n");
			
			this.setSource(src.toString());
		}
		
		public void update(Map<String, Object> ctx, float time, int width, int height) {
			this.args[0] = time;
			this.args[1] = width;
			this.args[2] = height;
			
			this.ctxCopy.clear();
			this.ctxCopy.putAll(ctx);
			
			try {
				this.fUpdate.call(this.ctxCopy, this.args);
			} catch(NashornException e) {
				this.notifyError(e.getMessage(), e.getLineNumber(), e.getColumnNumber());
				return;
			}
			
			for(String key : ctx.keySet()) {
				Object oldVal = ctx.get(key);
				Object newVal = this.ctxCopy.get(key);
				
				if(oldVal != newVal) {
					if(newVal instanceof Number) {
						if(oldVal instanceof Float) newVal = ((Number) newVal).floatValue();
						else if(oldVal instanceof Integer) newVal = ((Number) newVal).intValue();
						else if(oldVal instanceof Double) newVal = ((Number) newVal).doubleValue();
						else if(oldVal instanceof Long) newVal = ((Number) newVal).longValue();
						else newVal = oldVal;
					} else if(oldVal instanceof Number) newVal = oldVal;
				}
				
				ctx.put(key, newVal);
			}
		}
		
		protected void onSourceChanged() {
			this.fUpdate = (ScriptObjectMirror) this.bindings.get("update");
			if(this.fUpdate != null && !this.fUpdate.isFunction()) this.fUpdate = null;
		}
	}
	
	public abstract class TrackEffectInstance {
		private List<EffectParam<?>> parameters = new ArrayList<>();
		private List<EffectParam<?>> unmodifiableParameters = Collections.unmodifiableList(this.parameters);
		
		private List<EffectInstanceListener> listeners = new ArrayList<>();
		
		private TrackEffectScript script;
		
		private boolean enabled = true;
		
		public TrackEffectInstance() {
			this.setupParameters();
			this.script = new TrackEffectScript(this);
		}
		
		/**
		 * Called when the object is constructed, so subclass fields aren't initialized yet!
		 */
		public abstract void setupParameters();
		
		public TrackEffectScript getScript() {
			return this.script;
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
		
		public EffectParam<?> getParamByID(String name) {
			for(EffectParam<?> param : this.parameters) {
				if(param.getID().equals(name))
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
		
		public void addEffectInstanceListener(EffectInstanceListener l) {
			this.listeners.add(l);
		}
		
		public void removeEffectInstanceListener(EffectInstanceListener l) {
			this.listeners.remove(l);
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
	
	public abstract String getEffectClassID();
}
