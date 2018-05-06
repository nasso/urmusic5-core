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
package io.github.nasso.urmusic.model.renderer.audio;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import io.github.nasso.urmusic.Urmusic;
import io.github.nasso.urmusic.common.FFTContext;
import io.github.nasso.urmusic.common.MathUtils;
import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.ffmpeg.FFmpegUtils;

public class AudioRenderer implements Runnable {
	public static final Path AUDIO_BUFFER_SOURCE_PATH = Urmusic.URM_HOME.toPath().resolve("current_song.wav").toAbsolutePath();
	
	public static final int FFT_SIZE = 1 << 14;
	
	// We keep a cache of the latest FFT computed just to try to optimize what can be optimized.
	// This might never be re-used though lol
	private FFTContext fft = new FFTContext(FFT_SIZE);
	private float[] fftData = new float[FFT_SIZE];
	
	private float fftDataTime = -1.0f;
	private float fftDuration = -1.0f;
	
	private boolean shouldStop = false;
	private Thread audioThread;
	
	private int bufferSize;
	private long syncPrecision = 8000;
	
	// PCM Data Access
	private WaveFormatLoader accessLoader = new WaveFormatLoader();
	private byte[] accessBuffer;
	
	// Loading
	private Path sourceToLoad = null;
	private Runnable loadCallback = null;
	
	// Curr source
	private Path currentSource = null;
	private int sampleRate = 48000;
	private float duration = 0.0f;
	private boolean playing = false;
	private long sampleOffset = 0;
	
	public AudioRenderer(int bufferSize) {
		this.bufferSize = bufferSize;
		
		this.audioThread = new Thread(this, "urmusic_audio_thread");
		this.audioThread.start();
	}
	
	public int getSampleRate() {
		return this.sampleRate;
	}
	
	public float getDuration() {
		return this.duration;
	}
	
	public void dispose() {
		this.shouldStop = true;
		
		try {
			this.audioThread.join();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void getFreqData(float time, float duration, float[] dest) {
		this.getFreqData(time, duration, dest, false);
	}
	
	public void getFreqData(float time, float duration, float[] dest, boolean quadInterpolation) {
		if(this.fftDataTime < 0 || this.fftDuration <= 0 || this.fftDataTime != time || duration != this.fftDuration) {
			// Compute it if it isn't already
			this.fftDataTime = time;
			this.fftDuration = duration;
			
			UrmusicModel.getAudioRenderer().getSamples(time, duration, this.fftData);
			MathUtils.applyBlackmanWindow(this.fftData, this.fftData.length);
			this.fft.fft(this.fftData, true);
		}
		
		if(dest != null) {
			if(dest.length == FFT_SIZE) System.arraycopy(this.fftData, 0, dest, 0, FFT_SIZE);
			else {
				for(int i = 0; i < dest.length; i++)
					dest[i] = MathUtils.getValue(this.fftData, ((float) i / dest.length) * this.fftData.length, quadInterpolation);
			}
		}
	}
	
	public float maxFreqValue(float time) {
		return this.maxFreqValue(time, this.fftDuration);
	}
	
	public float maxFreqValue(float time, float duration) {
		// You know I had to do it to em... although this will probably be already computed
		this.getFreqData(time, duration, null);
		
		float m = -Float.MAX_VALUE;
		for(int i = 0; i < this.fftData.length; i++)
			m = Math.max(m, this.fftData[i]);
		
		return m;
	}
	
	public float minFreqValue(float time) {
		return this.maxFreqValue(time, this.fftDuration);
	}
	
	public float minFreqValue(float time, float duration) {
		// Same as above
		this.getFreqData(time, duration, null);
		
		float m = Float.MAX_VALUE;
		for(int i = 0; i < this.fftData.length; i++)
			m = Math.min(m, this.fftData[i]);
		
		return m;
	}
	
	public float peakToPeakAmp(float time, float duration) {
		synchronized(this.accessLoader) {
			long startSample = this.timeToSamples(time);
			long sampleCount = this.timeToSamples(duration);
			
			float posPeak = 0;
			float negPeak = 0;
			
			try { 
				long lastReadPosition = startSample;
				long bufferPosition = startSample;
				
				for(int i = 0; i < sampleCount; i++) {
					long targetPosition = startSample + i;
					
					if(targetPosition >= this.accessLoader.getSampleCount()) return (posPeak - negPeak) / 2f;
					
					// Check if the target sample offset is outside of the current buffer
					if(targetPosition >= lastReadPosition) {
						bufferPosition = targetPosition;
						lastReadPosition = bufferPosition + this.accessLoader.loadSamples(bufferPosition, this.accessBuffer);
					}
					
					float sample = 0.0f;
					for(int c = 0; c < this.accessLoader.getChannels(); c++) {
						int bi = (int) (targetPosition - bufferPosition) * this.accessLoader.getChannels() + c;
						
						if(this.accessLoader.getBitsPerSample() == 8) {
							sample += (float) this.accessBuffer[bi] / Byte.MAX_VALUE;
						} else if(this.accessLoader.getBitsPerSample() == 16) {
							bi *= 2;
							
							byte a = this.accessBuffer[bi];
							byte b = this.accessBuffer[bi + 1];
							
							if(this.accessLoader.isBigEndian()) {
								sample += (float) ((a << 8) | b) / Short.MAX_VALUE;
							} else {
								sample += (float) ((b << 8) | a) / Short.MAX_VALUE;
							}
						}
					}
					
					sample /= this.accessLoader.getChannels();
					
					posPeak = Math.max(sample, posPeak);
					negPeak = Math.min(sample, negPeak);
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
			
			return (posPeak - negPeak) / 2f;
		}
	}
	
	public float peakAmp(float time, float duration) {
		synchronized(this.accessLoader) {
			long startSample = this.timeToSamples(time);
			long sampleCount = this.timeToSamples(duration);
			
			float peak = 0;
			
			try {
				long lastReadPosition = startSample;
				long bufferPosition = startSample;
				
				for(int i = 0; i < sampleCount; i++) {
					long targetPosition = startSample + i;
					
					if(targetPosition >= this.accessLoader.getSampleCount()) return peak;
					
					// Check if the target sample offset is outside of the current buffer
					if(targetPosition >= lastReadPosition) {
						bufferPosition = targetPosition;
						lastReadPosition = bufferPosition + this.accessLoader.loadSamples(bufferPosition, this.accessBuffer);
					}
					
					float sample = 0.0f;
					for(int c = 0; c < this.accessLoader.getChannels(); c++) {
						int bi = (int) (targetPosition - bufferPosition) * this.accessLoader.getChannels() + c;
						
						if(this.accessLoader.getBitsPerSample() == 8) {
							sample += (float) this.accessBuffer[bi] / Byte.MAX_VALUE;
						} else if(this.accessLoader.getBitsPerSample() == 16) {
							bi *= 2;
							
							byte a = this.accessBuffer[bi];
							byte b = this.accessBuffer[bi + 1];
							
							if(this.accessLoader.isBigEndian()) {
								sample += (float) ((a << 8) | b) / Short.MAX_VALUE;
							} else {
								sample += (float) ((b << 8) | a) / Short.MAX_VALUE;
							}
						}
					}
					
					sample /= this.accessLoader.getChannels();
					
					peak = Math.max(Math.abs(sample), peak);
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
			
			return peak;
		}
	}
	
	public void getSamples(float startTime, float duration, float[] data) {
		if(
			duration <= 0.0f ||
			this.accessLoader == null ||
			this.accessBuffer == null ||
			!this.accessLoader.isLoaded()) {
			Arrays.fill(data, 0.0f);
			return;
		}
		
		synchronized(this.accessLoader) {
			long startSample = this.timeToSamples(startTime);
			long sampleRange = this.timeToSamples(duration);
			
			try {
				long lastReadPosition = startSample;
				long bufferPosition = startSample;
				
				for(int i = 0; i < data.length; i++) {
					long targetPosition = startSample + (int) ((float) i / (data.length - 1) * sampleRange);
					
					if(targetPosition >= this.accessLoader.getSampleCount()) {
						data[i] = 0;
						continue;
					}
					
					// Check if the target sample offset is outside of the current buffer
					if(targetPosition >= lastReadPosition) {
						bufferPosition = targetPosition;
						lastReadPosition = bufferPosition + this.accessLoader.loadSamples(bufferPosition, this.accessBuffer);
					}
					
					float sample = 0.0f;
					for(int c = 0; c < this.accessLoader.getChannels(); c++) {
						int bi = (int) (targetPosition - bufferPosition) * this.accessLoader.getChannels() + c;
						
						if(this.accessLoader.getBitsPerSample() == 8) {
							sample += (float) this.accessBuffer[bi] / Byte.MAX_VALUE;
						} else if(this.accessLoader.getBitsPerSample() == 16) {
							bi *= 2;
							
							byte a = this.accessBuffer[bi];
							byte b = this.accessBuffer[bi + 1];
							
							if(this.accessLoader.isBigEndian()) {
								sample += (float) ((a << 8) | b) / Short.MAX_VALUE;
							} else {
								sample += (float) ((b << 8) | a) / Short.MAX_VALUE;
							}
						}
					}
					
					sample /= this.accessLoader.getChannels();
					
					data[i] = sample;
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setAutoSyncPrecision(float timeDiff) {
		this.syncPrecision = this.timeToSamples(timeDiff);
	}
	
	private long timeToSamples(float time) {
		return (long) (time * this.sampleRate);
	}
	
	public void sync(float time) {
		long syncOffset = this.timeToSamples(time);
		
		if(this.playing && this.sampleOffset - syncOffset >= this.syncPrecision) {
			this.sampleOffset = syncOffset;
		}
	}
	
	public void seek(float time) {
		this.sampleOffset = this.timeToSamples(time);
	}
	
	public void play() {
		this.playing = true;
	}
	
	public void stop() {
		this.playing = false;
	}
	
	public void setAudioBufferSource(Path sourcePath, Runnable callback) {
		this.loadCallback = callback;
		this.sourceToLoad = sourcePath;
	}
	
	public int getBufferSize() {
		return this.bufferSize;
	}
	
	// AUDIO THREAD
	private WaveFormatLoader audioLoader;
	private AudioFormat format;
	private DataLine.Info lineInfo;
	private SourceDataLine audioSrc;
	private byte[] buffer;
	
	private void loadNewSource() {
		Path newSourcePath = this.sourceToLoad.toAbsolutePath();
		
		try {
			this.audioLoader.close();
		} catch(IOException e1) {
			e1.printStackTrace();
		}
		
		Process proc = FFmpegUtils.execute(
			"-i",
			newSourcePath.toString(),
			AudioRenderer.AUDIO_BUFFER_SOURCE_PATH.toString()
		);
		
		try {
			proc.waitFor();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		
		try {
			this.audioLoader.loadFile(AudioRenderer.AUDIO_BUFFER_SOURCE_PATH, false);
			this.audioLoader.setBufferSize(this.bufferSize * this.audioLoader.getChannels());

			synchronized(this.accessLoader) {
				this.accessLoader.loadFile(AudioRenderer.AUDIO_BUFFER_SOURCE_PATH, false);
				this.accessLoader.setBufferSize(this.bufferSize * this.accessLoader.getChannels());
				
				if(this.accessBuffer == null || this.accessBuffer.length != this.bufferSize * this.accessLoader.getChannels())
					this.accessBuffer = new byte[this.bufferSize * this.accessLoader.getChannels()];
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		this.sampleRate = this.audioLoader.getSampleRate();
		this.duration = (float) this.audioLoader.getSampleCount() / this.sampleRate;
		this.format = new AudioFormat(
			this.audioLoader.getSampleRate(),
			this.audioLoader.getBitsPerSample(),
			this.audioLoader.getChannels(),
			this.audioLoader.isSigned(),
			this.audioLoader.isBigEndian()
		);
		this.lineInfo = new DataLine.Info(SourceDataLine.class, this.format);
		
		try {
			this.audioSrc = (SourceDataLine) AudioSystem.getLine(this.lineInfo);
			this.audioSrc.open(this.format, this.bufferSize * this.format.getChannels());
			this.audioSrc.start();
		} catch(LineUnavailableException e) {
			e.printStackTrace();
		}
		
		if(this.buffer == null || this.buffer.length != this.bufferSize * this.format.getChannels())
			this.buffer = new byte[this.bufferSize * this.format.getChannels()];
		
		this.currentSource = newSourcePath;
		this.loadCallback.run();
	}
	
	private void updateBuffers() {
		if(!this.playing || this.audioLoader == null || !this.audioLoader.isLoaded()) {
			Arrays.fill(this.buffer, (byte) 0);
			return;
		}
		
		try {
			int samplesRead = this.audioLoader.loadSamples(this.sampleOffset, this.buffer);
			
			if(samplesRead == -1) {
				// EOF
				this.stop();
				Arrays.fill(this.buffer, (byte) 0);
				
				return;
			}
			
			this.sampleOffset += samplesRead;
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		this.audioLoader = new WaveFormatLoader();
		
		while(!this.shouldStop) {
			if(this.sourceToLoad != this.currentSource) {
				if(this.sourceToLoad != null) this.loadNewSource();
			}
			
			if(this.audioSrc != null && this.buffer != null) {
				this.updateBuffers();
				this.audioSrc.write(this.buffer, 0, this.buffer.length);
			} else {
				try {
					Thread.sleep(100);
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		try {
			this.audioLoader.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
