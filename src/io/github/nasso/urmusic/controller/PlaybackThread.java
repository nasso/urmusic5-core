package io.github.nasso.urmusic.controller;

import io.github.nasso.urmusic.model.UrmusicModel;

public class PlaybackThread extends Thread {
	private static final long SECOND_NANO = 1_000_000_000l;
	
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
				while(true) {
					while(!this.isPlayingBack()) {
						this.lock.wait();
					}
					
					// Queue frames to render if we can
					int playbackStartFrame = UrmusicController.getFrameCursor();
					int cacheSize = UrmusicModel.getVideoRenderer().getCacheSize();
					int cacheSizeFifth = cacheSize / 5;
					UrmusicModel.getVideoRenderer().queueFrameRange(UrmusicModel.getCurrentProject().getMainComposition(), playbackStartFrame, cacheSize);
					
					UrmusicModel.getAudioRenderer().seek(UrmusicController.getTimePosition());
					UrmusicModel.getAudioRenderer().play();
					
					int cacheForNext = -1;
					while(this.isPlayingBack()) {
						frameStartTime = System.nanoTime();
						idealFrameTime = (long) (PlaybackThread.SECOND_NANO / this.fps);
						
						// Advance cursor
						if((cacheForNext = UrmusicModel.getVideoRenderer().getCacheFor(UrmusicModel.getCurrentProject().getMainComposition(), UrmusicController.getFrameCursor() + 1)) >= 0
								&& !UrmusicModel.getVideoRenderer().getCachedFrames()[cacheForNext].dirty)
							UrmusicController.setFrameCursor(UrmusicController.getFrameCursor() + 1);
						
						if(UrmusicController.getFrameCursor() - playbackStartFrame > cacheSizeFifth) {
							UrmusicModel.getVideoRenderer().queueFrameRange(UrmusicModel.getCurrentProject().getMainComposition(), playbackStartFrame + cacheSize, cacheSizeFifth);
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
		if(!this.isPlayingBack()) return;
		
		this.playingBack = false;
	}
	
	public boolean isPlayingBack() {
		return this.playingBack;
	}

	public float getFPS() {
		return this.fps;
	}

	public void setFPS(float fps) {
		this.fps = fps;
	}
}
