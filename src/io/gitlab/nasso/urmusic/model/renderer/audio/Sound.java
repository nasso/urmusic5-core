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
package io.gitlab.nasso.urmusic.model.renderer.audio;

import java.nio.ByteBuffer;

/**
 * A <code>Sound</code> represents a sound resource.
 * 
 * @author nasso
 */
public class Sound {
	private ByteBuffer data;
	private int channels;
	private int sampleRate;
	private int sampleCount;
	private int bitsPerSample;
	
	private long dataOffset;
	
	/**
	 * Constructs a sound from the given raw data and with the specified properties.
	 * 
	 * @param data
	 *            The PCM data
	 * @param channels
	 *            The number of channels (1 = mono; 2 = stereo)
	 * @param sampleRate
	 *            The sample rate
	 * @param bitsPerSample
	 *            The bits per sample (usually 16)
	 * @param sampleCount
	 *            The total count of samples in the data
	 */
	public Sound(ByteBuffer data, int channels, int sampleRate, int bitsPerSample, int sampleCount) {
		this.data = data;
		this.channels = channels;
		this.sampleRate = sampleRate;
		this.bitsPerSample = bitsPerSample;
		this.sampleCount = sampleCount;
	}
	
	public long getDataOffset() {
		return this.dataOffset;
	}
	
	/**
	 * @return The duration in seconds
	 */
	public float getDuration() {
		return (float) this.sampleCount / this.sampleRate;
	}
	
	/**
	 * @return The number of channels in this sound
	 */
	public int getChannels() {
		return this.channels;
	}
	
	/**
	 * @return The sample rate
	 */
	public int getSampleRate() {
		return this.sampleRate;
	}
	
	/**
	 * @return The total count of samples in the sound
	 */
	public int getSampleCount() {
		return this.sampleCount;
	}
	
	/**
	 * @return The number of bits per sample (usually 16)
	 */
	public int getBitsPerSample() {
		return this.bitsPerSample;
	}
	
	/**
	 * @return The PCM data buffer
	 */
	public ByteBuffer getData() {
		return this.data;
	}
}
