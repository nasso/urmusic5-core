package io.github.nasso.urmusic.model.project.control;

import org.joml.Vector2f;
import org.joml.Vector2fc;

public class Point2DParam extends EffectParam<Vector2fc> {
	private Vector2f value = new Vector2f();
	
	public Point2DParam(String name) {
		super(name);
	}
	
	public Point2DParam(String name, float x, float y) {
		super(name);
		this.value.set(x, y);
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
