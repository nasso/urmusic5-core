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
package io.gitlab.nasso.urmusic.model.project.param;

import io.gitlab.nasso.urmusic.common.BoolValue;

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
		return t < 1.0f ? s : e;
	}
}
