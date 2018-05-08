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
	public Path destination;
	
	public Encoder videoEncoder;
	public Encoder audioEncoder;

	public Muxer muxer;
	
	public void reset() {
		this.reset(FFmpeg.getEncoderByName("libx264", "libopenh264", "h264"), FFmpeg.getEncoderByName("aac"), FFmpeg.getMuxerByName("mp4"));
	}
	
	public void reset(Encoder videoEncoder, Encoder audioEncoder, Muxer muxer) {
		this.destination = null;
		this.videoEncoder = videoEncoder;
		this.audioEncoder = audioEncoder;
		this.muxer = muxer;
	}
}
