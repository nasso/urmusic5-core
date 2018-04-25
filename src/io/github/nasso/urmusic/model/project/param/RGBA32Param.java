package io.github.nasso.urmusic.model.project.param;

import io.github.nasso.urmusic.common.MutableRGBA32;
import io.github.nasso.urmusic.common.RGBA32;

public class RGBA32Param extends EffectParam<RGBA32> {
	private MutableRGBA32 value = new MutableRGBA32();
	
	public RGBA32Param(String name) {
		super(name);
	}
	
	public RGBA32Param(String name, int rgba) {
		super(name);
		this.value.setRGBA(rgba);
	}
	
	protected void setStaticValue(RGBA32 val) {
		this.value.set(val);
	}

	protected RGBA32 getStaticValue() {
		return this.value;
	}
	
	protected RGBA32 cloneValue(RGBA32 val) {
		return new MutableRGBA32(val);
	}
	
	public RGBA32 ramp(RGBA32 s, RGBA32 e, float t) {
		this.value.setFade(s, e, t);
		return this.value;
	}
}
