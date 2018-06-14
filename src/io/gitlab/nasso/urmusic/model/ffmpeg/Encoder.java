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
package io.gitlab.nasso.urmusic.model.ffmpeg;

public class Encoder {
	public static enum Type {
		VIDEO,
		AUDIO
	}
	
	private Type type;
	private boolean frameLevelMultithreading;
	private boolean sliceLevelMultithreading;
	private boolean experimental;
	private boolean supports_draw_horiz_band;
	private boolean supportsDirectRenderingMethod1;
	
	private String name;
	private String desc;
	
	Encoder(Type type, boolean f, boolean s, boolean x, boolean b, boolean d, String name, String desc) {
		this.type = type;
		
		this.frameLevelMultithreading = f;
		this.sliceLevelMultithreading = s;
		this.experimental = x;
		this.supports_draw_horiz_band = b;
		this.supportsDirectRenderingMethod1 = d;
		
		this.name = name;
		this.desc = desc;
	}
	
	public Type getType() {
		return this.type;
	}
	
	public boolean isFrameLevelMultithreading() {
		return this.frameLevelMultithreading;
	}

	public boolean isSliceLevelMultithreading() {
		return this.sliceLevelMultithreading;
	}

	public boolean isExperimental() {
		return this.experimental;
	}

	public boolean supports_draw_horiz_band() {
		return this.supports_draw_horiz_band;
	}

	public boolean supportsDirectRenderingMethod1() {
		return this.supportsDirectRenderingMethod1;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getDescription() {
		return this.desc;
	}
	
	public String toString() {
		return this.name + " - " + this.desc;
	}
}
