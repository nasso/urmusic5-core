package io.github.nasso.urmusic.model.playback;

import io.github.nasso.urmusic.model.UrmusicModel;

public class PlaybackThread extends Thread {
	private static final long SECOND_NANO = 1_000_000_000l;
	
	private boolean playingBack = false;
	private int fps = 30;
	
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
					while(!this.isPlayingBack())
						this.lock.wait();
					
					int playbackStartFrame = UrmusicModel.getFrameCursor();
					
					// Queue frames to render if we can
					int cacheSize = UrmusicModel.getRenderer().getCacheSize();
					int cacheSizeFifth = cacheSize / 5;
					UrmusicModel.getRenderer().queueFrameRange(UrmusicModel.getFocusedComposition(), playbackStartFrame, cacheSize);
					
					while(this.isPlayingBack()) {
						frameStartTime = System.nanoTime();
						idealFrameTime = SECOND_NANO / this.fps;
						
						// Advance cursor
						UrmusicModel.setFrameCursor(UrmusicModel.getFrameCursor() + 1);
						
						if(UrmusicModel.getFrameCursor() - playbackStartFrame > cacheSizeFifth) {
							UrmusicModel.getRenderer().queueFrameRange(UrmusicModel.getFocusedComposition(), playbackStartFrame + cacheSize, cacheSizeFifth);
							playbackStartFrame += cacheSizeFifth;
						}
						
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

	public int getFPS() {
		return this.fps;
	}

	public void setFPS(int fps) {
		this.fps = fps;
	}
}
