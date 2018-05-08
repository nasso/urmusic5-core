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

public class Exporter {
	public static class ExportJob implements Runnable {
		private ExportProgressCallback callback;
		private ExportSettings settings;
		
		private boolean cancelled = false;
		private boolean done = false;
		
		private ExportJob(ExportSettings settings, ExportProgressCallback callback) {
			this.callback = callback;
			this.settings = settings;
		}
		
		public void run() {
			int frameIndex = 0;
			
			this.callback.exportBegin();

			float framerate = UrmusicModel.getCurrentProject().getMainComposition().getTimeline().getFramerate();
			float duration = UrmusicModel.getCurrentProject().getMainComposition().getTimeline().getDuration();
			int width = UrmusicModel.getCurrentProject().getMainComposition().getWidth();
			int height = UrmusicModel.getCurrentProject().getMainComposition().getHeight();
			
			Process p = FFmpeg.execute(
					// Input 1: Raw video data
					"-f", "rawvideo", "-r", String.valueOf(framerate), "-s", width + "x" + height, "-pix_fmt", "rgb24",
					"-t", String.valueOf(duration),
					"-i", "pipe:0",
					// Input 2: Audio (music file)
					"-i", AudioRenderer.AUDIO_BUFFER_SOURCE_PATH.toString(),
					// Output
					"-f", this.settings.muxer.getName(),
					"-c:v", this.settings.videoEncoder.getName(), "-r", String.valueOf(framerate), "-pix_fmt", "yuv420p",
					this.settings.useConstantBitrateVideo ? "-b:v" : "-q:v", String.valueOf(this.settings.useConstantBitrateVideo ? this.settings.bv + "K" : this.settings.vqscale),
					"-c:a", this.settings.audioEncoder.getName(),
					this.settings.useConstantBitrateAudio ? "-b:a" : "-q:a", String.valueOf(this.settings.useConstantBitrateAudio ? this.settings.ba + "K" : this.settings.aqscale),
					this.settings.destination.toString());
			
			Composition comp = UrmusicModel.getCurrentProject().getMainComposition();
			int framecount = UrmusicModel.getCurrentProject().getMainComposition().getTimeline().getTotalFrameCount();
			byte[] frameData = new byte[width * height * 3];
			ByteBuffer frameDataBuffer = ByteBuffer.wrap(frameData);
			
			try(OutputStream ffmpegIn = p.getOutputStream()) {
				this.callback.renderBegin();
				
				while(!this.cancelled && frameIndex < framecount) {
					// Render a frame!
					UrmusicModel.getVideoRenderer().syncRenderRawRGB(comp, frameIndex++, frameDataBuffer);
					
					ffmpegIn.write(frameData);
					
					this.callback.renderProgress((float) frameIndex / framecount);
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
			this.done = true;
		}
		
		public void cancel() {
			// equivalent to: if(!this.isDone()) this.cancelled = true;
			// except it's cooler this way
			
			this.cancelled |= !this.done;
		}
		
		public boolean isCancelled() {
			return this.cancelled;
		}
		
		public boolean isDone() {
			return this.done;
		}
	}

	public ExportJob start(ExportSettings settings, ExportProgressCallback callback) {
		ExportJob job = new ExportJob(settings, callback);
		new Thread(job, "urmusic_exporter_thread").start();
		
		return job;
	}
}
