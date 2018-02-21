package io.github.nasso.urmusic.model.project.param;

import org.joml.Vector4f;
import org.joml.Vector4fc;

public class BoundsParam extends EffectParam<Vector4fc> {
	private Vector4f value = new Vector4f();
	private Vector4f step = new Vector4f();
	
	public BoundsParam(String name, float x, float y, float w, float h, float stepX, float stepY, float stepW, float stepH) {
		super(name);
		
		this.value.set(x, y, w, h);
		this.step.set(stepX, stepY, stepW, stepH);
	}
	
	public BoundsParam(String name, float x, float y, float w, float h) {
		this(name, x, y, w, h, 1, 1, 1, 1);
	}

	public Vector4fc getStep() {
		return this.step;
	}
	
	protected void setStaticValue(Vector4fc val) {
		this.value.set(val.x(), val.y(), Math.max(val.z(), 0.0f), Math.max(val.w(), 0.0f));
	}
	
	protected Vector4fc getStaticValue() {
		return this.value;
	}
	
	protected Vector4fc cloneValue(Vector4fc val) {
		return new Vector4f(val.x(), val.y(), Math.max(val.z(), 0.0f), Math.max(val.w(), 0.0f));
	}
	
	public Vector4fc ramp(Vector4fc s, Vector4fc e, float t) {
		this.value.set(s).lerp(e, t);
		
		this.value.z = Math.max(this.value.z, 0.0f);
		this.value.w = Math.max(this.value.w, 0.0f);
		
		return this.value;
	}
}
