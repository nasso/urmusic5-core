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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import io.github.nasso.urmusic.common.ExportProgressCallback;
import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.ffmpeg.FFmpeg;
import io.github.nasso.urmusic.model.project.Composition;
import io.github.nasso.urmusic.model.renderer.audio.AudioRenderer;

public class Exporter implements Runnable {
	private ExportProgressCallback callback;
	private ExportSettings settings;
	private boolean exporting = false;
	
	private int frameIndex = 0;
	
	public Exporter(ExportSettings settings, ExportProgressCallback callback) {
		this.callback = callback;
		this.settings = settings;
	}

	public void run() {
		this.callback.exportBegin();

		float framerate = UrmusicModel.getCurrentProject().getMainComposition().getTimeline().getFramerate();
		float duration = UrmusicModel.getCurrentProject().getMainComposition().getTimeline().getDuration();
		int width = UrmusicModel.getCurrentProject().getMainComposition().getWidth();
		int height = UrmusicModel.getCurrentProject().getMainComposition().getHeight();
		
		ProcessBuilder pb = new ProcessBuilder(
				FFmpeg.FFMPEG_LOCATION.toString(), "-y",
				// Input 1: Raw video data
				"-f", "rawvideo", "-r", String.valueOf(framerate), "-s", width + "x" + height, "-pix_fmt", "rgb24",
				"-t", String.valueOf(duration),
				"-i", "pipe:0",
				// Input 2: Audio (music file)
				"-i", AudioRenderer.AUDIO_BUFFER_SOURCE_PATH.toString(),
				// Output
				"-f", this.settings.muxer.getName(),
				"-c:v", this.settings.videoEncoder.getName(), "-r", String.valueOf(framerate), "-pix_fmt", "yuv420p",
				"-c:a", this.settings.audioEncoder.getName(),
				this.settings.destination.toString());
		pb.redirectErrorStream(true);
		pb.redirectOutput(FFmpeg.FFMPEG_OUTPUT_LOG.toFile());
		pb.redirectInput(ProcessBuilder.Redirect.PIPE);
		
		Process p = null;
		try {
			p = pb.start();
		} catch(IOException e) {
			this.callback.exportException(e);
			return;
		}
		
		Composition comp = UrmusicModel.getCurrentProject().getMainComposition();
		int framecount = UrmusicModel.getCurrentProject().getMainComposition().getTimeline().getTotalFrameCount();
		byte[] frameData = new byte[width * height * 3];
		ByteBuffer frameDataBuffer = ByteBuffer.wrap(frameData);
		
		try(OutputStream ffmpegIn = p.getOutputStream()) {
			this.callback.renderBegin();
			
			while(this.exporting && this.frameIndex < framecount) {
				// Render a frame!
				UrmusicModel.getVideoRenderer().syncRenderRawRGB(comp, this.frameIndex++, frameDataBuffer);
				
				ffmpegIn.write(frameData);
				
				this.callback.renderProgress((float) this.frameIndex / framecount);
			}
			
			this.callback.renderDone();
			
			ffmpegIn.flush();
		} catch(IOException e) {
			this.callback.exportException(e);
			return;
		}
		
		try {
			p.waitFor();
		} catch(InterruptedException e) {
			this.callback.exportException(e);
			return;
		}
		
		this.callback.exportDone();
	}

	public void start() {
		this.exporting = true;
		new Thread(this, "urmusic_exporter_thread").start();
	}

	public void cancel() {
		this.exporting = false;
	}
}
