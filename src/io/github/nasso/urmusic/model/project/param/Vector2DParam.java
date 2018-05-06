/*******************************************************************************
 * urmusic - The Free and Open Source Music Visualizer Tool
 * Copyright (C) 2018  nasso (https://github.com/nasso)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * Contact "nasso": nassomails -at- gmail dot com
 ******************************************************************************/
package io.github.nasso.urmusic.model.project.param;

import org.joml.Vector2f;
import org.joml.Vector2fc;

public class Vector2DParam extends EffectParam<Vector2fc> {
	private ThreadLocal<Vector2f> garbageValue = ThreadLocal.withInitial(() -> new Vector2f());
	private Vector2f value = new Vector2f();
	private Vector2f step = new Vector2f();
	
	public Vector2DParam(String name) {
		this(name, 0, 0);
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
		return this.garbageValue.get().set(this.value);
	}

	protected Vector2fc cloneValue(Vector2fc val) {
		return new Vector2f(val);
	}

	public Vector2fc ramp(Vector2fc s, Vector2fc e, float t) {
		return this.value.set(s).lerp(e, t);
	}
}
