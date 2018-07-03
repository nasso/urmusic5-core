package io.gitlab.nasso.urmusic.model.project.param;

public class AngleParam extends FloatParam {
	public AngleParam(String name, float val, float step, float min, float max) {
		super(name, val, step, min, max);
	}
	
	public AngleParam(String name, float val, float step) {
		this(name, val, step, -Float.MAX_VALUE, Float.MAX_VALUE);
	}
	
	public AngleParam(String name, float val) {
		this(name, val, 1.0f);
	}
	
	public AngleParam(String name) {
		this(name, 0.0f);
	}
}
