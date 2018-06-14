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

import org.jtransforms.fft.FloatFFT_1D;

public class FFTContext {
	private final int size;
	private final FloatFFT_1D fft;
	
	public FFTContext(int fftSize) {
		this.size = fftSize;
		this.fft = new FloatFFT_1D(fftSize);
	}
	
	public void fft(float[] data, boolean convertToDb) {
		this.fft.realForward(data);
		
		for(int i = 0, l = data.length / 2; i < l; i++)
			data[data.length - 1 - i] = data[i] = (float) Math.hypot(data[i * 2], data[i * 2 + 1]);
		
		if(convertToDb) {
			for(int i = 0; i < data.length; i++) {
				data[i] = data[i] == 0 ? -Float.MAX_VALUE : (float) (20.0 * Math.log10(Math.abs(data[i] / this.size)));
			}
		}
	}
}
