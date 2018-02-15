package io.github.nasso.urmusic.model.project.param;

import io.github.nasso.urmusic.common.MathUtils;

public class FloatParam extends EffectParam<Float> {
	private Float val;
	private float step = 1.0f;
	
	private float min = -Float.MAX_VALUE;
	private float max = +Float.MAX_VALUE;
	
	public FloatParam(String name) {
		this(name, 0.0f);
	}
	
	public FloatParam(String name, float val) {
		this(name, val, 1.0f);
	}
	
	public FloatParam(String name, float val, float step) {
		this(name, val, step, -Float.MAX_VALUE, Float.MAX_VALUE);
	}
	
	public FloatParam(String name, float val, float step, float min, float max) {
		super(name);
		this.setValue(val, 0);
		this.step = step;
		this.min = min;
		this.max = max;
	}

	protected void setStaticValue(Float val) {
		this.val = MathUtils.clamp(val, this.min, this.max);
	}
	
	protected Float getStaticValue() {
		return this.val;
	}
	
	protected Float cloneValue(Float val) {
		return MathUtils.clamp(val, this.min, this.max);
	}
	
	public Float ramp(Float s, Float e, float t) {
		return MathUtils.clamp(MathUtils.lerp(s, e, t), this.min, this.max);
	}
	
	public float getStep() {
		return this.step;
	}

	public float getMin() {
		return this.min;
	}

	public void setMin(float min) {
		this.min = min;
	}

	public float getMax() {
		return this.max;
	}

	public void setMax(float max) {
		this.max = max;
	}
}
