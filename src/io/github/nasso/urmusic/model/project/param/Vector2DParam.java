package io.github.nasso.urmusic.model.project.param;

import org.joml.Vector2f;
import org.joml.Vector2fc;

public class Vector2DParam extends EffectParam<Vector2fc> {
	private Vector2f value = new Vector2f();
	private Vector2f step = new Vector2f();
	
	public Vector2DParam(String name) {
		super(name);
	}
	
	public Vector2DParam(String name, float x, float y) {
		this(name, x, y, 1, 1);
	}
	
	public Vector2DParam(String name, float x, float y, float stepX, float stepY) {
		super(name);
		this.value.set(x, y);
		this.step.set(stepX, stepY);
	}
	
	public Vector2fc getStep() {
		return this.step;
	}
	
	protected void setStaticValue(Vector2fc val) {
		this.value.set(val);
	}
	
	protected Vector2fc getStaticValue() {
		return this.value;
	}

	protected Vector2fc cloneValue(Vector2fc val) {
		return new Vector2f(val);
	}

	public Vector2fc ramp(Vector2fc s, Vector2fc e, float t) {
		return this.value.set(s).lerp(e, t);
	}
}
