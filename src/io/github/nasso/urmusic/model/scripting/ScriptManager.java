package io.github.nasso.urmusic.model.scripting;

import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

public class ScriptManager {
	private ScriptManager() {}
	
	static NashornScriptEngine engine;
	
	public static void init() throws ClassNotFoundException {
		engine = (NashornScriptEngine) new NashornScriptEngineFactory().getScriptEngine();

		if(engine == null) throw new ClassNotFoundException("Couldn't find Nashorn.");
	}
	
	
}
