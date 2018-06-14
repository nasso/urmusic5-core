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

public class OptionParam extends EffectParam<Integer> {
	private String[] options;
	
	private Integer value = 0;
	
	public OptionParam(String name, int defaultValue, String... values) {
		super(name);
		
		this.options = new String[values.length];
		for(int i = 0; i < values.length; i++)
			this.options[i] = values[i];
		
		this.value = defaultValue;
	}
	
	public int getOptionCount() {
		return this.options.length;
	}
	
	public String getOptionName(int i) {
		return this.options[i];
	}
	
	protected void setStaticValue(Integer val) {
		this.value = val;
	}
	
	protected Integer getStaticValue() {
		return this.value;
	}
	
	protected Integer cloneValue(Integer val) {
		return val;
	}
	
	public Integer ramp(Integer s, Integer e, float t) {
		return t < 1.0f ? s : e;
	}
}
