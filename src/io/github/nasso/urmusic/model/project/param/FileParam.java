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

import java.nio.file.Path;

public class FileParam extends EffectParam<Path> {
	private Path val;
	
	public FileParam(String name) {
		super(name, false);
	}
	
	protected void setStaticValue(Path val) {
		this.val = val;
	}
	
	protected Path getStaticValue() {
		return this.val;
	}
	
	protected Path cloneValue(Path val) {
		return val;
	}
	
	public Path ramp(Path s, Path e, float t) {
		return t < 1.0f ? s : e;
	}
}
