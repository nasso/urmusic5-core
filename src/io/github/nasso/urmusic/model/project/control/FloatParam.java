package io.github.nasso.urmusic.model.project.control;

import io.github.nasso.urmusic.utils.MathUtils;

public class FloatParam extends ControlParam<Float> {
	public FloatParam() {
	}
	
	public FloatParam(float val) {
		this.setValue(val, 0);
	}

	public Float ramp(Float s, Float e, float t) {
		return MathUtils.lerp(s, e, t);
	}
}
