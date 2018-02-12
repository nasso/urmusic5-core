package io.github.nasso.urmusic.model.renderer;

import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.awt.GLJPanel;

import io.github.nasso.urmusic.common.event.RendererListener;
import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.project.Composition;
import io.github.nasso.urmusic.model.project.Track;
import io.github.nasso.urmusic.model.project.TrackEffect;
import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;

public class Renderer implements Runnable {
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
		
		private void notifyLock() {
			synchronized(this.notifier) {
				this.notifier.notifyAll();
			}
		}
		
		private void pushCommand() {
			this.availableCommands++;
			
			if(Renderer.this.renderingThreadIdle) this.notifyLock();
		}
		
		private void clearCommandsOfType(int cmd) {
			synchronized(this.commands) {
				for(int i = 0; i < this.commands.size(); i++) {
					int c = (int) this.commands.get(i);
					int s = 0;
					
					switch(c) {
						case RCMD_RENDER_FRAME:
							s = 3;
							break;
						case RCMD_LOAD_EFFECT:
						case RCMD_UNLOAD_EFFECT:
						case RCMD_UNLOAD_EFFECT_INSTANCE:
						case RCMD_UNLOAD_TRACK:
							s = 1;
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
			this.clearCommandsOfType(RCMD_RENDER_FRAME);
		}
		
		public void consumeCommand() {
			this.availableCommands--;
		}
		
		public int getAvailableCommands() {
			return this.availableCommands;
		}
		
		public void queueRender(Composition comp, int frame, int frameCount) {
			synchronized(this.commands) {
				this.commands.add(RCMD_RENDER_FRAME);
				this.commands.add(comp);
				this.commands.add(frame);
				this.commands.add(frameCount);
				
				this.pushCommand();
			}
		}
		
		public void queueEffectLoad(TrackEffect fx) {
			synchronized(this.commands) {
				this.commands.add(RCMD_LOAD_EFFECT);
				this.commands.add(fx);
				
				this.pushCommand();
			}
		}
		
		public void queueEffectUnload(TrackEffect fx) {
			synchronized(this.commands) {
				this.commands.add(RCMD_UNLOAD_EFFECT);
				this.commands.add(fx);
	
				this.pushCommand();
			}
		}
		
		public void queueEffectInstanceUnload(TrackEffectInstance fx) {
			synchronized(this.commands) {
				this.commands.add(RCMD_UNLOAD_EFFECT_INSTANCE);
				this.commands.add(fx);
	
				this.pushCommand();
			}
		}
		
		public void queueTrackDispose(Track t) {
			synchronized(this.commands) {
				this.commands.add(RCMD_UNLOAD_TRACK);
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
	
	private List<RendererListener> listeners = new ArrayList<>();
	
	private Object renderLock = new Object();
	private RenderQueue renderQueue = new RenderQueue(this.renderLock);
	
	public Renderer(int gpuCachedFrameCount) {
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
		
		Thread renderingThread = new Thread(this);
		renderingThread.setName("urmusic_rendering_thread");
		renderingThread.start();
	}
	
	private boolean renderingThreadIdle = false;
	public void run() {
		int cmd = -1;
		Composition comp = null;
		int frame = -1;
		int frameCount = 0;
		TrackEffect fx = null;
		TrackEffectInstance fxi = null;
		Track track = null;
		
		// Wait for frames to render now
		try {
			while(true) {
				while(this.renderQueue.getAvailableCommands() == 0) {
					synchronized(this.renderLock) {
						this.renderingThreadIdle = true;
						this.renderLock.wait();
						this.renderingThreadIdle = false;
					}
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
							frameCount = (int) this.renderQueue.pop();
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
						for(int i = 0; i < frameCount; i++) {
							this.doFrame(comp, frame + i);
						}
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
		int cache = this.getCacheFor(comp, frame);
		if(cache != -1) {
			this.notifyFrameReady(comp, frame);
			return;
		}
		
		this.renderQueue.clearAllCommands();
		this.queueFrameRange(comp, frame, 1);
	}
	
	private void doFrame(Composition comp, int frame) {
		// -- ( runs on the rendering thread ) --
		
		int cache = this.getCacheFor(comp, frame);
		
		// If there's no cached frame, do the render
		if(cache == -1) {
			this.destCacheFrame = this.freeFrameCache(comp, frame);
			this.gpuCache[this.destCacheFrame].frame_id = frame;
			this.gpuCache[this.destCacheFrame].comp = comp;
			this.gpuCache[this.destCacheFrame].dirty = true;
			
			this.drawable.display();
			
			this.gpuCache[this.destCacheFrame].dirty = false;
		}
		
		this.notifyFrameReady(comp, frame);
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
		boolean needUpdate = false;
		for(int i = 0; i < this.gpuCache.length; i++) {
			this.gpuCache[i].dirty = true;
			needUpdate |= this.gpuCache[i].frame_id == UrmusicModel.getFrameCursor();
		}
		
		this.glRenderer.makeCompositionDirty(comp);
		
		if(needUpdate)
			this.queueFrameASAP(comp, UrmusicModel.getFrameCursor());
	}
	
	public int getLastCacheFor(Composition comp, int frame) {
		int lastCache = -1;
		
		for(int i = 0; i < this.gpuCache.length; i++) {
			if(this.gpuCache[i].comp == comp && !this.gpuCache[i].dirty) {
				if(this.gpuCache[i].frame_id == frame) return i;
				if(this.gpuCache[i].frame_id < frame && (lastCache == -1 || this.gpuCache[i].frame_id > this.gpuCache[lastCache].frame_id)) lastCache = i;
			}
		}
		
		return lastCache;
	}
	
	public int getCacheFor(Composition comp, int frame) {
		for(int i = 0; i < this.gpuCache.length; i++) {
			if(this.gpuCache[i].comp == comp && this.gpuCache[i].frame_id == frame && !this.gpuCache[i].dirty) return i;
		}
		
		return -1;
	}
	
	private int freeFrameCache(Composition comp, int frame) {
		int curs = UrmusicModel.getFrameCursor();
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
				(f.frame_id < curs && c.frame_id > curs) ||
				(f.frame_id < c.frame_id && c.frame_id < curs) ||
				(f.frame_id > c.frame_id && c.frame_id > curs)
			) bestChoice = i;
		}
		
		return bestChoice;
	}
	
	public void dispose() {
		this.drawable.destroy();
	}
	
	public GLJPanel createGLJPanelPreview() {
		GLJPanel gljp = new GLJPanel(this.glCapsPreview);
		gljp.setSkipGLOrientationVerticalFlip(true);
		gljp.setSharedAutoDrawable(this.drawable);
		gljp.addGLEventListener(this.glRenderer.createPreviewRenderer());
		
		gljp.display();
		
		return gljp;
	}
	
	public GLCanvas createGLCanvasPreview() {
		GLCanvas glcvs = new GLCanvas(this.glCapsPreview);
		glcvs.setSharedAutoDrawable(this.drawable);
		glcvs.addGLEventListener(this.glRenderer.createPreviewRenderer());
		
		glcvs.display();
		
		return glcvs;
	}
	
	public void addRendererListener(RendererListener l) {
		this.listeners.add(l);
	}
	
	public void removeRendererListener(RendererListener l) {
		this.listeners.remove(l);
	}
	
	private void notifyFrameReady(Composition comp, int frame) {
		for(RendererListener l : this.listeners) {
			l.frameRendered(comp, frame);
		}
	}
	
	private void notifyEffectLoaded(TrackEffect fx) {
		for(RendererListener l : this.listeners) {
			l.effectLoaded(fx);
		}
	}
	
	private void notifyEffectUnloaded(TrackEffect fx) {
		for(RendererListener l : this.listeners) {
			l.effectUnloaded(fx);
		}
	}
}
