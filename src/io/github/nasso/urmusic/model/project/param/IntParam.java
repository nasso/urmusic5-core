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
