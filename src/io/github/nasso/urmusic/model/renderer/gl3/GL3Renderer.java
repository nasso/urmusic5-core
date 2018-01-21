package io.github.nasso.urmusic.model.renderer.gl3;

import static com.jogamp.opengl.GL.*;

import java.nio.IntBuffer;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;

import io.github.nasso.urmusic.model.renderer.CachedFrame;
import io.github.nasso.urmusic.model.renderer.GLRenderer;
import io.github.nasso.urmusic.model.renderer.Renderer;

public class GL3Renderer extends GLRenderer {
	private int[] cachedTextures, cachedFBOs;
	
	private GL3 gl;
	
	public GL3Renderer(Renderer renderer) {
		super(renderer);
	}
	
	public void setContext(GL ctx) {
		this.gl = ctx.getGL3();
	}
	
	public GL getContext() {
		return this.gl;
	}

	protected GLProfile getProfile() {
		return GLProfile.getGL2GL3();
	}

	public void init() {
		CachedFrame[] cachedFrames = this.mainRenderer.getCachedFrames();
		IntBuffer bufTex = IntBuffer.allocate(cachedFrames.length);
		IntBuffer bufFBO = IntBuffer.allocate(cachedFrames.length);
		
		GL3Utils.createFramebuffers(this.gl, cachedFrames.length, this.mainRenderer.getWidth(), this.mainRenderer.getHeight(), bufTex, bufFBO);
		
		this.cachedTextures = new int[cachedFrames.length];
		this.cachedFBOs = new int[cachedFrames.length];
		
		bufTex.get(this.cachedTextures);
		bufFBO.get(this.cachedFBOs);
		
		this.gl.glClearColor(0, 0, 1, 1);
	}

	public void display() {
		int cacheIndex = this.mainRenderer.getDestCacheFrame().index_on_creation;

		this.gl.glClearColor(0, 1, 1, 1);
		this.gl.glBindFramebuffer(GL_FRAMEBUFFER, this.cachedFBOs[cacheIndex]);
		this.gl.glClear(GL_COLOR_BUFFER_BIT);
	}

	public void dispose() {
		GL3Utils.dispose(this.gl);
	}

	public int getCurrentFBO() {
		return this.cachedFBOs[this.mainRenderer.getDestCacheFrame().index_on_creation];
	}
	
	public int getCurrentTexture() {
		return this.cachedTextures[this.mainRenderer.getDestCacheFrame().index_on_creation];
	}
	
	public GLEventListener createPreviewRenderer() {
		return new GL3PreviewRenderer(this);
	}
}
