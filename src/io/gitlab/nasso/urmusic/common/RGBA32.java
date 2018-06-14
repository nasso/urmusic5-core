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
package io.gitlab.nasso.urmusic.common;

public interface RGBA32 extends Cloneable {
	public int getRed();
	public int getGreen();
	public int getBlue();
	public int getAlpha();
	public int getRGBA();
	
	public float getRedf();
	public float getGreenf();
	public float getBluef();
	public float getAlphaf();
	
	public RGBA32 clone();
	
	public static String toHexString(int value) {
		String istr = Integer.toHexString(value);
		return "#" + ("00000000" + istr).substring(istr.length());
	}
}
