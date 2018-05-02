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
		} catch(Exception e) {
			this.notifyError(e.getMessage(), 0, 0);
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
