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
import io.github.nasso.urmusic.model.ffmpeg.FFmpegUtils;

public class AudioRenderer implements Runnable {
	public static final Path AUDIO_BUFFER_SOURCE_PATH = Urmusic.URM_HOME.toPath().resolve("current_song.wav").toAbsolutePath();
	
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
	
	public void dispose() {
		this.shouldStop = true;
		
		try {
			this.audioThread.join();
		} catch(InterruptedException e) {
			e.printStackTrace();
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
					
					// First, check if the target sample offset is outside of the current buffer
					if(targetPosition >= this.accessLoader.getSampleCount()) {
						data[i] = 0;
						continue;
					}
					
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
