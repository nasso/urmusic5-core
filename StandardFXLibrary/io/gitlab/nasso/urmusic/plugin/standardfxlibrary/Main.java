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
package io.gitlab.nasso.urmusic.plugin.standardfxlibrary;

import io.gitlab.nasso.urmusic.model.project.TrackEffect;
import io.gitlab.nasso.urmusic.plugin.UrmPlugin;

public class Main implements UrmPlugin {
	private static final TrackEffect[] EFFECTS = {
		// Basics
		new ImageDisplayVFX(),
		new CircleMaskVFX(),
		new RectangleMaskVFX(),
		new AffineTransformVFX(),
		new PolarCoordsVFX(),
		new MirrorVFX(),
		
		// Audio
		new AudioScopeVFX(),
		new AudioSpectrumVFX(),
	};
	
	public TrackEffect[] getEffects() {
		return EFFECTS;
	}
	
	public void pluginInit() {
	}
	
	public void pluginDispose() {
	}
}
