package io.github.nasso.urmusic.model.project.control;

import io.github.nasso.urmusic.common.BoolValue;

public class BooleanParam extends EffectParam<BoolValue> {
	private BoolValue val = BoolValue.FALSE;
	
	public BooleanParam(String name, BoolValue val) {
		super(name);
		this.val = val;
	}
	
	protected void setStaticValue(BoolValue val) {
		this.val = val;
	}
	
	protected BoolValue getStaticValue() {
		return this.val;
	}
	
	protected BoolValue cloneValue(BoolValue val) {
		return val;
	}
	
	public BoolValue ramp(BoolValue s, BoolValue e, float t) {
		return t < 0.5f ? s : e;
	}
}
