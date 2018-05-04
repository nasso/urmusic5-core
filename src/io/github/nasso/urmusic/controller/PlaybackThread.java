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
package io.github.nasso.urmusic.controller;

import io.github.nasso.urmusic.model.UrmusicModel;

public class PlaybackThread extends Thread {
	private static final long SECOND_NANO = 1_000_000_000l;
	
	private boolean shouldQuit = false;
	private boolean shouldRestart = false;
	private boolean playingBack = false;
	private float fps = 30;
	
	private Object lock = new Object();
	
	public PlaybackThread() {
		super("urmusic_playback_thread");
		
		this.start();
	}
	
	public void run() {
		long idealFrameTime, frameStartTime, frameEndTime, sleepTime;
		
		try {
			synchronized(this.lock) {
				while(!this.shouldQuit) {
					while(!this.playingBack && !this.shouldQuit) {
						this.lock.wait();
					}
					
					this.shouldRestart = false;
					
					// Queue frames to render if we can
					int playbackStartFrame = UrmusicController.getFrameCursor();
					int cacheSize = UrmusicModel.getVideoRenderer().getCacheSize();
					int cacheSizeFifth = cacheSize / 5;
					UrmusicModel.getVideoRenderer().queueFrameRange(UrmusicController.getFocusedComposition(), playbackStartFrame, cacheSize);
					
					UrmusicModel.getAudioRenderer().seek(UrmusicController.getTimePosition());
					UrmusicModel.getAudioRenderer().play();
					
					int cacheForNext = -1;
					while(this.isPlayingBack() && !this.shouldRestart) {
						frameStartTime = System.nanoTime();
						idealFrameTime = (long) (PlaybackThread.SECOND_NANO / this.fps);
						
						// Stop playback when we reach the end
						if(UrmusicController.getFrameCursor() == UrmusicController.getFocusedComposition().getTimeline().getTotalFrameCount() - 1) {
							this.stopPlayback();
							break;
						}
						
						// Advance cursor
						if((cacheForNext = UrmusicModel.getVideoRenderer().getCacheFor(UrmusicController.getFocusedComposition(), UrmusicController.getFrameCursor() + 1)) >= 0
								&& !UrmusicModel.getVideoRenderer().getCachedFrames()[cacheForNext].dirty)
							UrmusicController.setFrameCursor(UrmusicController.getFrameCursor() + 1);
						
						if(UrmusicController.getFrameCursor() - playbackStartFrame > cacheSizeFifth) {
							UrmusicModel.getVideoRenderer().queueFrameRange(UrmusicController.getFocusedComposition(), playbackStartFrame + cacheSize, cacheSizeFifth);
							playbackStartFrame += cacheSizeFifth;
						}

						// Sync audio
						UrmusicModel.getAudioRenderer().sync(UrmusicController.getTimePosition());
						
						frameEndTime = System.nanoTime();
						sleepTime = idealFrameTime - frameEndTime + frameStartTime;
						if(sleepTime > 0) {
							try {
								Thread.sleep(sleepTime / 1_000_000l, (int) (sleepTime % 1_000_000l));
							} catch(InterruptedException e) {
								e.printStackTrace();
							}
						}
					}

					UrmusicModel.getAudioRenderer().stop();
				}
			}
		} catch(InterruptedException e1) {
			e1.printStackTrace();
		}
	}

	public void startPlayback() {
		if(this.isPlayingBack()) return;
		
		this.playingBack = true;
		
		synchronized(this.lock) {
			this.lock.notifyAll();
		}
	}
	
	public void stopPlayback() {
		this.playingBack = false;
	}
	
	public void restartPlayback() {
		if(this.playingBack) this.shouldRestart = true;
	}
	
	public boolean isPlayingBack() {
		return this.playingBack;
	}
	
	public void waitForExit() throws InterruptedException {
		this.shouldQuit = true;
		
		if(this.playingBack) this.stopPlayback();
		else {
			synchronized(this.lock) {
				this.lock.notifyAll();
			}
		}
		
		this.join();
	}

	public float getFPS() {
		return this.fps;
	}

	public void setFPS(float fps) {
		this.fps = fps;
	}
}
