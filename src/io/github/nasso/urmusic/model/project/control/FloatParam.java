package io.github.nasso.urmusic.model.project.control;

import io.github.nasso.urmusic.utils.MathUtils;

public class FloatParam extends ControlParam<Float> {
	private Float val;
	
	public FloatParam(String name) {
		super(name);
	}
	
	public FloatParam(String name, float val) {
		super(name);
		this.setValue(val, 0);
	}

	protected void setStaticValue(Float val) {
		this.val = val;
	}
	
	protected Float getStaticValue() {
		return this.val;
	}
	
	protected Float cloneValue(Float val) {
		return val;
	}
	
	public Float ramp(Float s, Float e, float t) {
		return MathUtils.lerp(s, e, t);
	}
}
