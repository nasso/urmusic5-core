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
package io.gitlab.nasso.urmusic.model.renderer.video;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.awt.GLJPanel;

import io.gitlab.nasso.urmusic.common.event.VideoRendererListener;
import io.gitlab.nasso.urmusic.controller.UrmusicController;
import io.gitlab.nasso.urmusic.model.project.Composition;
import io.gitlab.nasso.urmusic.model.project.Track;
import io.gitlab.nasso.urmusic.model.project.TrackEffect;
import io.gitlab.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;

public class VideoRenderer implements Runnable {
	private static final int RCMD_RENDER_FRAME = 0;
	private static final int RCMD_LOAD_EFFECT = 1;
	private static final int RCMD_UNLOAD_EFFECT = 2;
	private static final int RCMD_UNLOAD_EFFECT_INSTANCE = 3;
	private static final int RCMD_UNLOAD_TRACK = 4;
	
	private class RenderQueue {
		private int availableCommands = 0;
		private List<Object> commands = new ArrayList<>();
		
		private final Object notifier;
		
		public RenderQueue(Object notifier) {
			this.notifier = notifier;
		}
		
		public void waitForStop() {
			this.clearAllCommands();
			
			VideoRenderer.this.renderingThreadStopRequest = true;
			if(VideoRenderer.this.renderingThreadIdle) this.notifyLock();
			
			try {
				VideoRenderer.this.renderingThread.join();
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		private void notifyLock() {
			synchronized(this.notifier) {
				this.notifier.notifyAll();
			}
		}
		
		private void pushCommand() {
			this.availableCommands++;
			
			if(VideoRenderer.this.renderingThreadIdle) this.notifyLock();
		}
		
		public void clearCommandsOfType(int cmd) {
			synchronized(this.commands) {
				for(int i = 0; i < this.commands.size(); i++) {
					int c = (int) this.commands.get(i);
					int s = 0;
					
					switch(c) {
						case RCMD_RENDER_FRAME:
							s = 2;
							break;
						case RCMD_LOAD_EFFECT:
						case RCMD_UNLOAD_EFFECT:
						case RCMD_UNLOAD_EFFECT_INSTANCE:
						case RCMD_UNLOAD_TRACK:
							s = 1;
							break;
						default:
							s = 0;
							break;
					}
					
					if(c == cmd) {
						for(int j = 0; j <= s; j++)
							this.commands.remove(i);
						
						i--;
						this.availableCommands--;
					} else {
						i += s;
					}
				}
			}
		}
		
		public void clearAllCommands() {
			synchronized(this.commands) {
				this.commands.clear();
				this.availableCommands = 0;
			}
		}
		
		public void consumeCommand() {
			this.availableCommands--;
		}
		
		public int getAvailableCommands() {
			return this.availableCommands;
		}
		
		public void queueRender(Composition comp, int frame, int frameCount) {
			synchronized(this.commands) {
				for(int i = 0; i < frameCount; i++) {
					this.commands.add(VideoRenderer.RCMD_RENDER_FRAME);
					this.commands.add(comp);
					this.commands.add(frame + i);
					
					this.pushCommand();
				}
			}
		}
		
		public void queueEffectLoad(TrackEffect fx) {
			synchronized(this.commands) {
				this.commands.add(VideoRenderer.RCMD_LOAD_EFFECT);
				this.commands.add(fx);
				
				this.pushCommand();
			}
		}
		
		public void queueEffectUnload(TrackEffect fx) {
			synchronized(this.commands) {
				this.commands.add(VideoRenderer.RCMD_UNLOAD_EFFECT);
				this.commands.add(fx);
	
				this.pushCommand();
			}
		}
		
		public void queueEffectInstanceUnload(TrackEffectInstance fx) {
			synchronized(this.commands) {
				this.commands.add(VideoRenderer.RCMD_UNLOAD_EFFECT_INSTANCE);
				this.commands.add(fx);
	
				this.pushCommand();
			}
		}
		
		public void queueTrackDispose(Track t) {
			synchronized(this.commands) {
				this.commands.add(VideoRenderer.RCMD_UNLOAD_TRACK);
				this.commands.add(t);
	
				this.pushCommand();
			}
		}
		
		public Object pop() {
			synchronized(this.commands) {
				return this.commands.remove(0);
			}
		}
	}
	
	GLAutoDrawable drawable;
	
	private int destCacheFrame = 0;

	private CachedFrame[] gpuCache;
	private GLRenderer glRenderer;
	
	private GLCapabilities glCaps, glCapsPreview;
	
	private List<VideoRendererListener> listeners = new ArrayList<>();
	
	private Thread renderingThread;
	
	private Object renderLock = new Object();
	private Object renderIdleLock = new Object();
	private RenderQueue renderQueue = new RenderQueue(this.renderIdleLock);
	
	public VideoRenderer(int gpuCachedFrameCount) {
		this.gpuCache = new CachedFrame[gpuCachedFrameCount];
		
		for(int i = 0; i < gpuCachedFrameCount; i++) this.gpuCache[i] = new CachedFrame(i);

		this.glRenderer = new GLRenderer(this);

		// JOGL init
		GLProfile glp = this.glRenderer.getProfile();
		this.glCapsPreview = new GLCapabilities(glp);
		this.glCaps = new GLCapabilities(glp);
		this.glCaps.setOnscreen(false);
		
		GLDrawableFactory factory = GLDrawableFactory.getFactory(glp);
		this.drawable = factory.createDummyAutoDrawable(null, true, this.glCaps, null);
		this.drawable.setContext(this.drawable.createContext(null), false);
		this.drawable.addGLEventListener(this.glRenderer);
		
		this.renderingThread = new Thread(this);
		this.renderingThread.setName("urmusic_rendering_thread");
		this.renderingThread.start();
	}
	
	private boolean renderingThreadStopRequest = false;
	private boolean renderingThreadIdle = false;
	public void run() {
		int cmd = -1;
		Composition comp = null;
		int frame = -1;
		TrackEffect fx = null;
		TrackEffectInstance fxi = null;
		Track track = null;
		
		// Wait for frames to render now
		try {
			main_loop: while(!this.renderingThreadStopRequest) {
				while(this.renderQueue.getAvailableCommands() == 0) {
					synchronized(this.renderIdleLock) {
						this.renderingThreadIdle = true;
						this.renderIdleLock.wait();
						this.renderingThreadIdle = false;
					}
					
					if(this.renderingThreadStopRequest) break main_loop;
				}
				
				// Get stuff from the render queue
				synchronized(this.renderQueue.commands) {
					// Check again for commands
					if(this.renderQueue.getAvailableCommands() == 0) continue;
					
					cmd = (int) this.renderQueue.pop();
					
					switch(cmd) {
						case RCMD_RENDER_FRAME:
							comp = (Composition) this.renderQueue.pop();
							frame = (int) this.renderQueue.pop();
							break;
						case RCMD_LOAD_EFFECT:
						case RCMD_UNLOAD_EFFECT:
							fx = (TrackEffect) this.renderQueue.pop();
							break;
						case RCMD_UNLOAD_EFFECT_INSTANCE:
							fxi = (TrackEffectInstance) this.renderQueue.pop();
							break;
						case RCMD_UNLOAD_TRACK:
							track = (Track) this.renderQueue.pop();
							break;
					}
					
					this.renderQueue.consumeCommand();
				}
				
				switch(cmd) {
					case RCMD_RENDER_FRAME:
						this.doFrame(comp, frame);
						comp = null; // gc free
						break;
					case RCMD_LOAD_EFFECT:
						this.glRenderer.initEffect(fx);
						this.notifyEffectLoaded(fx);
						fx = null; // gc free
						break;
					case RCMD_UNLOAD_EFFECT:
						this.glRenderer.disposeEffect(fx);
						this.notifyEffectUnloaded(fx);
						fx = null; // gc free
						break;
					case RCMD_UNLOAD_EFFECT_INSTANCE:
						this.glRenderer.disposeEffectInstance(fxi);
						fxi = null; // gc free
						break;
					case RCMD_UNLOAD_TRACK:
						this.glRenderer.disposeTrack(track);
						track = null; // gc free
						break;
				}
			}
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void queueFrameRange(Composition comp, int frameStart, int frameCount) {
		this.renderQueue.queueRender(comp, frameStart, frameCount);
	}
	
	public void queueFrameASAP(Composition comp, int frame) {
		float time = frame / comp.getTimeline().getFramerate();
		
		int cache = this.getCacheFor(comp, frame);
		if(cache != -1) {
			this.notifyFrameReady(comp, time);
			return;
		}
		
		this.renderQueue.clearCommandsOfType(VideoRenderer.RCMD_RENDER_FRAME);
		this.queueFrameRange(comp, frame, 1);
	}
	
	private void doFrame(Composition comp, int frame) {
		// -- ( runs on the rendering thread ) --
		synchronized(this.renderLock) {
			int cache = this.getCacheFor(comp, frame);
			
			// If there's no cached frame, do the render
			if(cache == -1) {
				this.destCacheFrame = this.freeFrameCache(comp);
				this.gpuCache[this.destCacheFrame].frame_pos = frame;
				this.gpuCache[this.destCacheFrame].comp = comp;
				this.gpuCache[this.destCacheFrame].dirty = true;
				this.gpuCache[this.destCacheFrame].destRGB = null;
				
				this.drawable.display();
				
				this.gpuCache[this.destCacheFrame].dirty = false;
			}
			
			this.notifyFrameReady(comp, frame / comp.getTimeline().getFramerate());
		}
	}
	
	public void syncRenderRawRGB(Composition comp, int frame, ByteBuffer destRGB) {
		synchronized(this.renderLock) {
			// Force render 
			this.destCacheFrame = this.freeFrameCache(comp);
			this.gpuCache[this.destCacheFrame].frame_pos = frame;
			this.gpuCache[this.destCacheFrame].comp = comp;
			this.gpuCache[this.destCacheFrame].dirty = true;
			this.gpuCache[this.destCacheFrame].destRGB = destRGB;
			
			this.drawable.display();
		}
	}
	
	public CachedFrame[] getCachedFrames() {
		return this.gpuCache;
	}
	
	public int getCacheSize() {
		return this.gpuCache.length;
	}
	
	public CachedFrame getCurrentDestCacheFrame() {
		return this.gpuCache[this.destCacheFrame];
	}
	
	public void initEffect(TrackEffect fx) {
		this.renderQueue.queueEffectLoad(fx);
	}
	
	public void disposeEffect(TrackEffect vfx) {
		this.renderQueue.queueEffectUnload(vfx);
	}
	
	public void disposeEffectInstance(TrackEffectInstance vfx) {
		this.renderQueue.queueEffectInstanceUnload(vfx);
	}
	
	public void disposeTrack(Track track) {
		this.renderQueue.queueTrackDispose(track);
	}
	
	public void makeCompositionDirty(Composition comp) {
		for(int i = 0; i < this.gpuCache.length; i++) {
			this.gpuCache[i].dirty = true;
		}
		
		this.glRenderer.makeCompositionDirty(comp);
	}
	
	public int getLastCacheFor(Composition comp, int frame) {
		int lastCache = -1;
		
		for(int i = 0; i < this.gpuCache.length; i++) {
			if(this.gpuCache[i].comp == comp && !this.gpuCache[i].dirty) {
				if(this.gpuCache[i].frame_pos == frame) return i;
				if(this.gpuCache[i].frame_pos < frame && (lastCache == -1 || this.gpuCache[i].frame_pos > this.gpuCache[lastCache].frame_pos)) lastCache = i;
			}
		}
		
		return lastCache;
	}
	
	public int getCacheFor(Composition comp, int frame) {
		for(int i = 0; i < this.gpuCache.length; i++) {
			if(this.gpuCache[i].comp == comp && this.gpuCache[i].frame_pos == frame && !this.gpuCache[i].dirty) return i;
		}
		
		return -1;
	}
	
	private int freeFrameCache(Composition comp) {
		int curs = UrmusicController.getFrameCursor();
		int bestChoice = 0;
		
		for(int i = 0; i < this.gpuCache.length; i++) {
			CachedFrame f = this.gpuCache[i];
			CachedFrame c = this.gpuCache[bestChoice];

			if(f.dirty && !c.dirty) {
				bestChoice = i;
				continue;
			} else if(!f.dirty && c.dirty) continue;
			
			// Compare
			if(
				(f.frame_pos < curs && c.frame_pos > curs) ||
				(f.frame_pos < c.frame_pos && c.frame_pos < curs) ||
				(f.frame_pos > c.frame_pos && c.frame_pos > curs)
			) bestChoice = i;
		}
		
		return bestChoice;
	}
	
	public void dispose() {
		this.renderQueue.waitForStop();
		
		this.drawable.destroy();
	}
	
	public GLPreviewer createGLJPanelPreview() {
		GLPreviewer previewer = new GLPreviewer(this.glRenderer);
		
		GLJPanel gljp = new GLJPanel(this.glCapsPreview);
		gljp.setSkipGLOrientationVerticalFlip(true);
		gljp.setSharedAutoDrawable(this.drawable);
		gljp.addGLEventListener(previewer.renderer);
		
		gljp.display();
		
		previewer.panel = gljp;
		
		return previewer;
	}
	
	public GLPreviewer createGLCanvasPreview() {
		GLPreviewer previewer = new GLPreviewer(this.glRenderer);
		
		GLCanvas glcvs = new GLCanvas(this.glCapsPreview);
		glcvs.setSharedAutoDrawable(this.drawable);
		glcvs.addGLEventListener(previewer.renderer);
		
		glcvs.display();
		
		previewer.panel = glcvs;
		
		return previewer;
	}
	
	public void addVideoRendererListener(VideoRendererListener l) {
		synchronized(this.listeners) {
			this.listeners.add(l);
		}
	}
	
	public void removeRendererListener(VideoRendererListener l) {
		synchronized(this.listeners) {
			this.listeners.remove(l);
		}
	}
	
	private void notifyFrameReady(Composition comp, float time) {
		synchronized(this.listeners) {
			for(VideoRendererListener l : this.listeners) {
				l.frameRendered(comp, time);
			}
		}
	}
	
	private void notifyEffectLoaded(TrackEffect fx) {
		for(VideoRendererListener l : this.listeners) {
			l.effectLoaded(fx);
		}
	}
	
	private void notifyEffectUnloaded(TrackEffect fx) {
		for(VideoRendererListener l : this.listeners) {
			l.effectUnloaded(fx);
		}
	}
}
