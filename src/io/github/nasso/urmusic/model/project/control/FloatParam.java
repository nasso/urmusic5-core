package io.github.nasso.urmusic.model.project.control;

import io.github.nasso.urmusic.utils.MathUtils;

public class FloatParam extends ControlParam<Float> {
	private Float val;
	
	public FloatParam() {
	}
	
	public FloatParam(float val) {
		this.setValue(val, 0);
	}

	public void setValue(Float val) {
		this.val = val;
	}
	
	public Float getValue() {
		return this.val;
	}
	
	public Float ramp(Float s, Float e, float t) {
		return MathUtils.lerp(s, e, t);
	}

}
