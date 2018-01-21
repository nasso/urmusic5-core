package io.github.nasso.urmusic.model.renderer;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.awt.GLJPanel;

import io.github.nasso.urmusic.model.renderer.gl3.GL3Renderer;

public class Renderer {
	public static enum Backend {
		GL3
	}

	private GLAutoDrawable drawable;
	private final int width, height;

	private CachedFrame[] gpuCache;
	private GLRenderer glRenderer;
	
	private GLCapabilities glCaps, glCapsPreview;
	
	public Renderer(int width, int height, int gpuCacheSizeMB, Backend backend) {
		this.width = width;
		this.height = height;
		
		long frameSizeByte = width * height * 3;
		long gpuCacheSizeByte = gpuCacheSizeMB * 1_000_000l;
		int maxFrameCount = (int) (gpuCacheSizeByte / frameSizeByte);
		
		this.gpuCache = new CachedFrame[maxFrameCount];
		
		for(int i = 0; i < maxFrameCount; i++) this.gpuCache[i] = new CachedFrame(i);
		
		// JOGL init
		switch(backend) {
			case GL3:
				this.glRenderer = new GL3Renderer(this);
				break;
		}

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
	
	public CachedFrame getDestCacheFrame() {
		return this.gpuCache[0];
	}
	
	public int getWidth() {
		return this.width;
	}
	
	public int getHeight() {
		return this.height;
	}
	
	public void renderFrame(int frameOffset) {
		this.shiftCachedFrames();
		this.gpuCache[0].frame_id = frameOffset;
		
		this.drawable.display();
	}
	
	public void dispose() {
		this.drawable.destroy();
	}
	
	private void shiftCachedFrames() {
		CachedFrame f = this.gpuCache[this.gpuCache.length - 1];
		
		for(int i = this.gpuCache.length - 1; i > 0; i--) {
			this.gpuCache[i] = this.gpuCache[i - 1];
		}
		
		this.gpuCache[0] = f;
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
}
