package io.github.nasso.urmusic.model.project.control;

import io.github.nasso.urmusic.utils.MutableRGBA32;
import io.github.nasso.urmusic.utils.RGBA32;

public class RGBA32Param extends ControlParam<RGBA32> {
	private MutableRGBA32 value = new MutableRGBA32();
	
	public RGBA32Param() {
	}
	
	public RGBA32Param(int rgba) {
		this.value.setRGBA(rgba);
	}
	
	public void setValue(RGBA32 val) {
		this.value.set(val);
	}

	public RGBA32 getValue() {
		return this.value;
	}

	public RGBA32 ramp(RGBA32 s, RGBA32 e, float t) {
		this.value.setFade(s, e, t);
		return this.value;
	}
}
