package io.github.nasso.urmusic.model.renderer;

import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.awt.GLJPanel;

import io.github.nasso.urmusic.model.event.RendererListener;
import io.github.nasso.urmusic.model.project.Composition;
import io.github.nasso.urmusic.model.project.Track;
import io.github.nasso.urmusic.model.project.TrackEffect;
import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;

public class Renderer {
	GLAutoDrawable drawable;
	private final int width, height;
	
	private int destCacheFrame = 0;

	private CachedFrame[] gpuCache;
	private GLRenderer glRenderer;
	
	private GLCapabilities glCaps, glCapsPreview;
	
	private List<RendererListener> listeners = new ArrayList<>();
	
	public Renderer(int width, int height, int gpuCachedFrameCount) {
		this.width = width;
		this.height = height;
		
		this.gpuCache = new CachedFrame[gpuCachedFrameCount];
		
		for(int i = 0; i < gpuCachedFrameCount; i++) this.gpuCache[i] = new CachedFrame(i);
		
		// JOGL init
		this.glRenderer = new GLRenderer(this);

		GLProfile glp = this.glRenderer.getProfile();
		this.glCapsPreview = new GLCapabilities(glp);
		this.glCaps = new GLCapabilities(glp);
		this.glCaps.setOnscreen(false);
		
		GLDrawableFactory factory = GLDrawableFactory.getFactory(glp);
		this.drawable = factory.createOffscreenAutoDrawable(null, this.glCaps, null, this.width, this.height);
		this.drawable.addGLEventListener(this.glRenderer);
	}
	
	public CachedFrame[] getCachedFrames() {
		return this.gpuCache;
	}
	
	public CachedFrame getCurrentDestCacheFrame() {
		return this.gpuCache[this.destCacheFrame];
	}
	
	public int getWidth() {
		return this.width;
	}
	
	public int getHeight() {
		return this.height;
	}
	
	public void initEffect(TrackEffect vfx) {
		this.glRenderer.initEffect(vfx);
	}
	
	public void disposeEffect(TrackEffect vfx) {
		this.glRenderer.disposeEffect(vfx);
	}
	
	public void makeCompositionDirty(Composition comp) {
		for(int i = 0; i < this.gpuCache.length; i++) {
			this.gpuCache[i].dirty = true;
		}
		
		this.glRenderer.makeCompositionDirty(comp);
		
		this.updateFrameIfNeeded();
	}
	
	private void updateFrameIfNeeded() {
		if(this.getCurrentDestCacheFrame().dirty) {
			this.doFrame(this.getCurrentDestCacheFrame().comp, this.getCurrentDestCacheFrame().frame_id);
		}
	}
	
	private int getCacheFor(Composition comp, int frame) {
		for(int i = 0; i < this.gpuCache.length; i++) {
			if(this.gpuCache[i].comp == comp && this.gpuCache[i].frame_id == frame && !this.gpuCache[i].dirty) return i;
		}
		
		return -1;
	}
	
	private int freeFrameCache(Composition comp, int frame) {
		int bestChoice = -1;
		
		for(int i = 0; i < this.gpuCache.length; i++) {
			CachedFrame f = this.gpuCache[i];
			
			if(bestChoice == -1) {
				bestChoice = i;
			} else {
				// Compare
				CachedFrame c = this.gpuCache[bestChoice];
				
				if(!c.dirty && f.dirty) {
					bestChoice = i;
				} else if (Math.abs(f.frame_id - frame) > Math.abs(c.frame_id - frame)) {
					bestChoice = i;
				}
			}
		}
		
		return bestChoice;
	}
	
	public void disposeTrack(Track track) {
		this.glRenderer.disposeTrack(track);
	}
	
	public void disposeEffect(TrackEffectInstance vfx) {
		this.glRenderer.disposeEffectInstance(vfx);
	}
	
	public void doFrame(Composition comp, int frameOffset) {
		this.destCacheFrame = this.getCacheFor(comp, frameOffset);
		if(this.destCacheFrame != -1) {
			this.notifyFrameReady(comp, frameOffset);
			return;
		}
		
		this.destCacheFrame = this.freeFrameCache(comp, frameOffset);
		this.gpuCache[this.destCacheFrame].frame_id = frameOffset;
		this.gpuCache[this.destCacheFrame].comp = comp;
		this.gpuCache[this.destCacheFrame].dirty = true;
		
		this.drawable.display();
		
		this.gpuCache[this.destCacheFrame].dirty = false;
		
		this.notifyFrameReady(comp, frameOffset);
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
}
