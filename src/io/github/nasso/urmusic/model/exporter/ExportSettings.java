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
package io.github.nasso.urmusic.model.exporter;

import java.nio.file.Path;

import io.github.nasso.urmusic.model.ffmpeg.Encoder;
import io.github.nasso.urmusic.model.ffmpeg.FFmpeg;
import io.github.nasso.urmusic.model.ffmpeg.Muxer;

public class ExportSettings {
	public static final int QSCALE_MIN = 1;
	public static final int QSCALE_MAX = 31;
	
	public Path destination;
	
	public Encoder videoEncoder;
	public Encoder audioEncoder;

	public Muxer muxer;
	
	public boolean useConstantBitrateVideo = false;
	public boolean useConstantBitrateAudio = true;
	
	public int vqscale = 20;
	public int aqscale = 20;
	
	public int bv = 10_000; // kbps
	public int ba = 384; // kbps
	
	public ExportSettings() {
		this.reset();
	}
	
	public void reset() {
		this.reset(FFmpeg.getEncoderByName("libx264", "libopenh264", "h264"), FFmpeg.getEncoderByName("aac"), FFmpeg.getMuxerByName("mp4"));
	}
	
	public void reset(Encoder videoEncoder, Encoder audioEncoder, Muxer muxer) {
		this.destination = null;
		this.videoEncoder = videoEncoder;
		this.audioEncoder = audioEncoder;
		this.muxer = muxer;
	}
	
	public String toString() {
		return new StringBuilder()
				.append("destination: ").append(this.destination).append("\n")
				.append("videoEncoder: ").append(this.videoEncoder).append("\n")
				.append("audioEncoder: ").append(this.audioEncoder).append("\n")
				.append("muxer: ").append(this.muxer).append("\n")
				.append("useConstantBitrateVideo: ").append(this.useConstantBitrateVideo).append("\n")
				.append("useConstantBitrateAudio: ").append(this.useConstantBitrateAudio).append("\n")
				.append("vqscale: ").append(this.vqscale).append("\n")
				.append("aqscale: ").append(this.aqscale).append("\n")
				.append("bv: ").append(this.bv).append("\n")
				.append("ba: ").append(this.ba)
				.toString();
	}
}
