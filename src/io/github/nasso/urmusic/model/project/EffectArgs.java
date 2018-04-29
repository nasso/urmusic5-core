package io.github.nasso.urmusic.model.project;

import java.util.HashMap;
import java.util.Map;

public class EffectArgs {
	public Map<String, Object> parameters = new HashMap<>();
	
	public float time;
	public boolean cancelled;
	
	public EffectArgs() {
	}
	
	public void clear() {
		this.parameters.clear();
		this.time = -1;
		this.cancelled = false;
	}
}
