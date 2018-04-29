package io.github.nasso.urmusic.model.scripting;

import java.util.ArrayList;
import java.util.List;

import javax.script.Bindings;
import javax.script.ScriptException;

import io.github.nasso.urmusic.common.ScriptRuntimeErrorListener;

public class Script {
	private List<ScriptRuntimeErrorListener> errorListeners = new ArrayList<>();
	
	private String source = "";
	
	protected Bindings bindings;
	
	public Script() {
		this.bindings = ScriptManager.engine.createBindings();
	}
	
	public void addErrorListener(ScriptRuntimeErrorListener listener) {
		this.errorListeners.add(listener);
	}
	
	public void removeErrorListener(ScriptRuntimeErrorListener listener) {
		this.errorListeners.remove(listener);
	}
	
	public String getSource() {
		return this.source;
	}
	
	public void setSource(String source) {
		this.source = source;
		
		this.bindings.clear();
		try {
			ScriptManager.engine.eval(source, this.bindings);
		} catch(ScriptException e) {
			this.notifyError(e.getMessage(), e.getLineNumber(), e.getColumnNumber());
		}
		
		this.onSourceChanged();
	}
	
	protected void notifyError(String message, int line, int column) {
		for(int i = 0; i < this.errorListeners.size(); i++) {
			this.errorListeners.get(i).onError(message, line, column);
		}
	}
	
	protected void onSourceChanged() {
	}
}
