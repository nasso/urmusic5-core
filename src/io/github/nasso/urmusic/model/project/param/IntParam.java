package io.github.nasso.urmusic.model.project.param;

import io.github.nasso.urmusic.common.MathUtils;

public class IntParam extends EffectParam<Integer> {
	private Integer val;
	private int step = 1;
	
	private int min = -Integer.MAX_VALUE;
	private int max = +Integer.MAX_VALUE;
	
	public IntParam(String name) {
		this(name, 0);
	}
	
	public IntParam(String name, int val) {
		this(name, val, 1);
	}
	
	public IntParam(String name, int val, int step) {
		this(name, val, step, -Integer.MAX_VALUE, Integer.MAX_VALUE);
	}
	
	public IntParam(String name, int val, int step, int min, int max) {
		super(name);
		this.setValue(val, 0);
		this.step = step;
		this.min = min;
		this.max = max;
	}

	protected void setStaticValue(Integer val) {
		this.val = MathUtils.clamp(val, this.min, this.max);
	}
	
	protected Integer getStaticValue() {
		return this.val;
	}
	
	protected Integer cloneValue(Integer val) {
		return MathUtils.clamp(val, this.min, this.max);
	}
	
	public Integer ramp(Integer s, Integer e, float t) {
		return MathUtils.clamp(MathUtils.lerp(s, e, t), this.min, this.max);
	}
	
	public int getStep() {
		return this.step;
	}

	public int getMin() {
		return this.min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public int getMax() {
		return this.max;
	}

	public void setMax(int max) {
		this.max = max;
	}
}
