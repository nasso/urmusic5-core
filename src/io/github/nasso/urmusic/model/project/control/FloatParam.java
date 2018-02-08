package io.github.nasso.urmusic.model.project.control;

import io.github.nasso.urmusic.common.MathUtils;

public class FloatParam extends EffectParam<Float> {
	private Float val;
	private float step = 1.0f;
	
	public FloatParam(String name) {
		this(name, 0.0f);
	}
	
	public FloatParam(String name, float val) {
		this(name, val, 1.0f);
	}
	
	public FloatParam(String name, float val, float step) {
		super(name);
		this.setValue(val, 0);
		this.step = step;
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
	
	public float getStep() {
		return this.step;
	}
}
