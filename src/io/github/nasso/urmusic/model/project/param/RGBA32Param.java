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

import io.github.nasso.urmusic.common.MutableRGBA32;
import io.github.nasso.urmusic.common.RGBA32;

public class RGBA32Param extends EffectParam<RGBA32> {
	private ThreadLocal<MutableRGBA32> garbageValue = ThreadLocal.withInitial(() -> new MutableRGBA32());
	private MutableRGBA32 value = new MutableRGBA32();
	
	public RGBA32Param(String name) {
		this(name, 0x000000FF);
	}
	
	public RGBA32Param(String name, int rgba) {
		super(name);
		this.value.setRGBA(rgba);
	}
	
	protected void setStaticValue(RGBA32 val) {
		this.value.set(val);
	}

	protected RGBA32 getStaticValue() {
		return this.garbageValue.get().set(this.value);
	}
	
	protected RGBA32 cloneValue(RGBA32 val) {
		return new MutableRGBA32(val);
	}
	
	public RGBA32 ramp(RGBA32 s, RGBA32 e, float t) {
		this.value.setFade(s, e, t);
		return this.value;
	}
}
