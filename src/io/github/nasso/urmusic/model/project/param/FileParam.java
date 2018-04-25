package io.github.nasso.urmusic.model.project.param;

import java.nio.file.Path;

public class FileParam extends EffectParam<Path> {
	private Path val;
	
	public FileParam(String name) {
		super(name, false);
	}
	
	protected void setStaticValue(Path val) {
		this.val = val;
	}
	
	protected Path getStaticValue() {
		return this.val;
	}
	
	protected Path cloneValue(Path val) {
		return val;
	}
	
	public Path ramp(Path s, Path e, float t) {
		return t < 1.0f ? s : e;
	}
}
