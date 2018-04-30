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
package io.github.nasso.urmusic.model.renderer.video;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2ES3.*;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;

import io.github.nasso.urmusic.common.event.CompositionListener;
import io.github.nasso.urmusic.model.project.Composition;
import io.github.nasso.urmusic.model.project.Track;
import io.github.nasso.urmusic.model.project.TrackEffect;
import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.github.nasso.urmusic.model.project.VideoEffect;
import io.github.nasso.urmusic.model.project.VideoEffectArgs;
import io.github.nasso.urmusic.model.project.VideoEffectInstance;
import io.github.nasso.urmusic.model.project.param.EffectParam;

/**
 * Uses OpenGL 3
 * @author nasso
 */
public class GLRenderer implements GLEventListener, CompositionListener {
	private int cacheSize;
	private IntBuffer bufTex;
	
	public void setCacheSize(int size) {
		this.cacheSize = size;
		this.bufTex = Buffers.newDirectIntBuffer(size);
	}
	
	private final class CachedFramebuffer {
		private int width, height;
		
		public int fbo;
		public int textureAlt;
		
		public int[] texture;
		public boolean[] dirty;
		
		public CachedFramebuffer(Composition comp) {
			this.fbo = GLRenderer.this.glu.genFramebuffer(GLRenderer.this.gl);
			this.texture = new int[GLRenderer.this.cacheSize];
			this.dirty = new boolean[GLRenderer.this.cacheSize];
			
			this.makeAllDirty(true);
			
			this.width = comp.getWidth();
			this.height = comp.getHeight();
			
			this.textureAlt = GLRenderer.this.glu.genTexture(GLRenderer.this.gl);
			GLRenderer.this.glu.genTextures(GLRenderer.this.gl, GLRenderer.this.cacheSize, GLRenderer.this.bufTex);
			
			for(int i = 0; i <= GLRenderer.this.cacheSize; i++) {
				int t = i == GLRenderer.this.cacheSize ? this.textureAlt : GLRenderer.this.bufTex.get(i);
				
				GLRenderer.this.gl.glBindTexture(GL_TEXTURE_2D, t);
				GLRenderer.this.gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, this.width, this.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
				GLRenderer.this.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
				GLRenderer.this.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
				GLRenderer.this.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
				GLRenderer.this.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
			}
			
			GLRenderer.this.bufTex.get(this.texture);
			GLRenderer.this.bufTex.flip();
		}
		
		public void makeAllDirty(boolean dirty) {
			for(int i = 0; i < this.dirty.length; i++) this.dirty[i] = dirty;
		}

		public CachedFramebuffer update(Composition comp) {
			if(comp.getWidth() != this.width || comp.getHeight() != this.height) {
				this.width = comp.getWidth();
				this.height = comp.getHeight();
				
				for(int i = 0; i <= this.texture.length; i++) {
					GLRenderer.this.gl.glBindTexture(GL_TEXTURE_2D, i == this.texture.length ? this.textureAlt : this.texture[i]);
					GLRenderer.this.gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, this.width, this.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
				}
			}
			
			return this;
		}
		
		/**
		 * Swap the texture at the given index with the alt texture
		 * @param gl
		 * @param i
		 */
		public void swapAlt(GL3 gl, int i) {
			int alt = this.textureAlt;
			this.textureAlt = this.texture[i];
			this.texture[i] = alt;
			
			gl.glBindFramebuffer(GL_FRAMEBUFFER, this.fbo);
			gl.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, this.texture[i], 0);
		}

		public void dispose() {
			GLRenderer.this.glu.deleteFramebuffer(GLRenderer.this.gl, this.fbo);
			GLRenderer.this.glu.deleteTexture(GLRenderer.this.gl, this.textureAlt);
			
			for(int i = 0; i < this.texture.length; i++)
				GLRenderer.this.glu.deleteTexture(GLRenderer.this.gl, this.texture[i]);
		}
	}
	
	private final class TrackRenderTexture {
		public Composition comp;
		
		public int tex_id[] = new int[2];
		public int depth_id = 0;
		public int width, height;
		
		public TrackRenderTexture(Composition comp) {
			this.comp = comp;
		}
		
		public void swapBuffers() {
			int temp = this.tex_id[0];
			
			for(int i = 0; i < this.tex_id.length - 1; i++)
				this.tex_id[i] = this.tex_id[i + 1];
			
			this.tex_id[this.tex_id.length - 1] = temp;
		}
		
		/**
		 * @return The dest buffer, aka where we're rendering stuff. You write to this one.
		 */
		public int getDestBuffer() {
			return this.tex_id[0];
		}
		
		/**
		 * @return The back buffer, aka the previous dest buffer, where we just rendered the previous stuff. You read from this one.
		 */
		public int getBackBuffer() {
			return this.tex_id[this.tex_id.length - 1];
		}
		
		public int getDepthBuffer() {
			return this.depth_id;
		}
		
		public TrackRenderTexture update() {
			boolean allocRenderBuffer = this.depth_id == 0 || this.width != this.comp.getWidth() || this.height != this.comp.getHeight();
			
			if(this.depth_id == 0) this.depth_id = GLRenderer.this.glu.genRenderbuffer(GLRenderer.this.gl);
			
			if(allocRenderBuffer) {
				GLRenderer.this.gl.glBindRenderbuffer(GL_RENDERBUFFER, this.depth_id);
				GLRenderer.this.gl.glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, this.comp.getWidth(), this.comp.getHeight());
			}
			
			for(int i = 0; i < this.tex_id.length; i++) {
				if(this.tex_id[i] != 0 && this.width == this.comp.getWidth() && this.height == this.comp.getHeight()) continue;
				
				if(this.tex_id[i] == 0) {
					this.tex_id[i] = GLRenderer.this.glu.genTexture(GLRenderer.this.gl);
					GLRenderer.this.gl.glBindTexture(GL_TEXTURE_2D, this.tex_id[i]);
					GLRenderer.this.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
					GLRenderer.this.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
					GLRenderer.this.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
					GLRenderer.this.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
				} else {
					GLRenderer.this.gl.glBindTexture(GL_TEXTURE_2D, this.tex_id[i]);
				}
				
				GLRenderer.this.gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, this.comp.getWidth(), this.comp.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
			}
			
			this.width = this.comp.getWidth();
			this.height = this.comp.getHeight();
			
			return this;
		}
	}

	private List<VideoEffectInstance> loadedEffectInstances = new ArrayList<>();
	private List<VideoEffect> loadedEffects = new ArrayList<>();
	private Map<Track, TrackRenderTexture> tracksTextures = new HashMap<>();
	private Map<Composition, CachedFramebuffer> compFBOs = new HashMap<>();
	
	public final VideoRenderer mainRenderer;
	private GL3 gl;
	private NGLUtils glu = new NGLUtils("gl renderer global");
	
	private VideoEffectArgs fxArgs = new VideoEffectArgs();
	
	private List<Composition> disposedCompositions = new ArrayList<>();
	
	private int trackRenderingFBO;
	private List<Track> trackRenderList = new ArrayList<>();

	private int compose_prog, compose_quadVAO;
	private int compose_loc_inputTex, compose_loc_inputComp;
	
	public GLRenderer(VideoRenderer renderer) {
		this.mainRenderer = renderer;
	}
	
	public GLProfile getProfile() {
		return GLProfile.getGL2GL3();
	}
	
	public void initEffect(TrackEffect fx) {
		if(!(fx instanceof VideoEffect)) return;
		VideoEffect vfx = (VideoEffect) fx;
		
		GLContext ctx = this.mainRenderer.drawable.getContext();
		ctx.makeCurrent();
		this.gl = ctx.getGL().getGL3();
		
		vfx.globalVideoSetup(this.gl);
		
		ctx.release();
		
		this.loadedEffects.add(vfx);
	}
	
	public void disposeEffect(TrackEffect fx) {
		if(!(fx instanceof VideoEffect)) return;
		VideoEffect vfx = (VideoEffect) fx;
		
		GLContext ctx = this.mainRenderer.drawable.getContext();
		ctx.makeCurrent();
		this.gl = ctx.getGL().getGL3();
		
		// Dispose all instances of this effect
		for(int i = 0; i < this.loadedEffectInstances.size();) {
			VideoEffectInstance inst = this.loadedEffectInstances.get(i);
			
			if(((TrackEffectInstance) inst).getEffectClass() == fx) {
				inst.disposeVideo(this.gl);
				this.loadedEffectInstances.remove(i);
			} else i++;
		}
		
		vfx.globalVideoDispose(this.gl);
		
		ctx.release();
		
		this.loadedEffects.remove(vfx);
	}
	
	public void disposeTrack(Track t) {
		GLContext ctx = this.mainRenderer.drawable.getContext();
		ctx.makeCurrent();
		this.gl = ctx.getGL().getGL3();
		
		for(int i = 0; i < t.getEffectCount(); i++) {
			TrackEffectInstance fx = t.getEffect(i);
			
			if(!(fx instanceof VideoEffectInstance)) continue;
			VideoEffectInstance vfx = (VideoEffectInstance) fx;
			
			vfx.disposeVideo(this.gl);
			this.loadedEffectInstances.remove(vfx);
		}
		
		TrackRenderTexture tex = this.tracksTextures.remove(t);
		
		if(tex != null) {
			for(int i = 0; i < tex.tex_id.length; i++)
				this.glu.deleteTexture(this.gl, tex.tex_id[i]);
		}
		
		ctx.release();
	}
	
	public void disposeComposition(Composition comp) {
		GLContext ctx = this.mainRenderer.drawable.getContext();
		ctx.makeCurrent();
		this.gl = ctx.getGL().getGL3();
		
		this.compFBOs.remove(comp).dispose();
		
		ctx.release();
	}
	
	public TrackRenderTexture getTrackTexture(Composition comp, Track t) {
		TrackRenderTexture tex = this.tracksTextures.get(t);
		
		if(tex == null)
			this.tracksTextures.put(t, tex = new TrackRenderTexture(comp));
		
		return tex.update();
	}
	
	public void disposeEffectInstance(TrackEffectInstance fx) {
		if(!(fx instanceof VideoEffectInstance)) return;
		VideoEffectInstance vfx = (VideoEffectInstance) fx;
		
		GLContext ctx = this.mainRenderer.drawable.getContext();
		ctx.makeCurrent();
		this.gl = ctx.getGL().getGL3();
		
		vfx.disposeVideo(this.gl);
		
		ctx.release();

		this.loadedEffectInstances.remove(vfx);
	}
	
	public void makeCompositionDirty(Composition comp) {
		if(this.compFBOs.containsKey(comp)) {
			this.compFBOs.get(comp).makeAllDirty(true);
		}
	}

	public void init(GLAutoDrawable drawable) {
		this.gl = drawable.getGL().getGL3();
		
		this.setCacheSize(this.mainRenderer.getCachedFrames().length);
		
		this.trackRenderingFBO = this.glu.genFramebuffer(this.gl);
		
		this.compose_prog = this.glu.createProgram(this.gl, "track_composer.vs", "track_composer.fs");
		this.compose_loc_inputTex = this.gl.glGetUniformLocation(this.compose_prog, "trackInput");
		this.compose_loc_inputComp = this.gl.glGetUniformLocation(this.compose_prog, "compOutput");
		
		this.compose_quadVAO = this.glu.createFullQuadVAO(this.gl);
		
		this.gl.glBindVertexArray(0);
		this.gl.glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	public void dispose(GLAutoDrawable drawable) {
		this.gl = drawable.getGL().getGL3();
		
		while(!this.loadedEffectInstances.isEmpty()) this.loadedEffectInstances.remove(0).disposeVideo(this.gl);
		while(!this.loadedEffects.isEmpty()) this.loadedEffects.remove(0).globalVideoDispose(this.gl);
		
		for(Track t : this.tracksTextures.keySet()) {
			TrackRenderTexture tex = this.tracksTextures.remove(t);
			
			if(tex != null) {
				for(int i = 0; i < tex.tex_id.length; i++)
					this.glu.deleteTexture(this.gl, tex.tex_id[i]);
			}
		}
		this.tracksTextures.clear();
		
		for(Composition c : this.compFBOs.keySet()) {
			this.compFBOs.get(c).dispose();
		}
		
		this.glu.dispose(this.gl);
	}
	
	public void display(GLAutoDrawable drawable) {
		this.gl = drawable.getGL().getGL3();
		
		// Dispose compositions that needs to be disposed
		for(Composition c : this.disposedCompositions)
			this.disposeComposition(c);
		this.disposedCompositions.clear();
		
		CachedFrame dest = this.mainRenderer.getCurrentDestCacheFrame();
		
		this.renderComposition(dest.comp, dest.frame_pos / dest.comp.getTimeline().getFramerate(), dest.index);
		
		this.gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}
	
	private void prepareTrackRenderList(Composition comp, float time) {
		this.trackRenderList.clear();
		
		List<Track> tracks = comp.getTimeline().getTracks();
		track_loop: for(int i = 0; i < tracks.size(); i++) {
			Track t = tracks.get(i);
			
			if(!t.isEnabled() || !t.isActiveAt(time) || t.getEffectCount() == 0) continue;
			
			for(int j = 0; j < t.getEffectCount(); j++) {
				TrackEffectInstance fx = t.getEffect(j);
				
				if(fx instanceof VideoEffectInstance && fx.isEnabled()) {
					this.trackRenderList.add(t);
					continue track_loop;
				}
			}
		}
	}
	
	// TODO: CompositeTrack rendering
	private void renderTrack(Composition comp, Track t, float time) {
		TrackRenderTexture dest = this.getTrackTexture(comp, t);
		
		// Clear the args before starting
		this.fxArgs.clear();
		
		// Apply the effects
		for(int j = 0; j < t.getEffectCount(); j++) {
			TrackEffectInstance fx = t.getEffect(j);
			
			// We only care about enabled video effects
			if(!fx.isEnabled() || !(fx instanceof VideoEffectInstance)) continue;
			VideoEffectInstance vfx = (VideoEffectInstance) fx;
			
			// Init the effect first if it's not loaded already
			if(!this.loadedEffectInstances.contains(vfx)) {
				vfx.setupVideo(this.gl);
				this.loadedEffectInstances.add(vfx);
			}
			
			// Don't swap buffers if the previous effect did nothing
			if(!this.fxArgs.cancelled) {
				// Bind the framebuffer and the dest buffer
				this.gl.glBindFramebuffer(GL_FRAMEBUFFER, this.trackRenderingFBO);
				
				if(j == 0) {
					this.gl.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, dest.getDestBuffer(), 0);
					this.gl.glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, dest.getDepthBuffer());

					this.gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
					this.gl.glClearStencil(0x00);
					this.gl.glClearDepth(1.0);
					this.gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
				}
				
				// Swap dest/back buffers, to bring the previous dest buffer to the back and get a fresh usable dest buffer
				dest.swapBuffers();
				
				GLRenderer.this.gl.glBindTexture(GL_TEXTURE_2D, dest.getBackBuffer());
				GLRenderer.this.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
				GLRenderer.this.gl.glGenerateMipmap(GL_TEXTURE_2D);
				
				this.gl.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, dest.getDestBuffer(), 0);
				this.gl.glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, dest.getDepthBuffer());
			}
			
			// Effect arguments
			this.fxArgs.clear();
			this.fxArgs.width = dest.width;
			this.fxArgs.height = dest.height;
			this.fxArgs.time = time;
			this.fxArgs.texInput = dest.getBackBuffer();
			this.fxArgs.fboOutput = this.trackRenderingFBO;

			// Update the parameters
			for(int i = 0; i < fx.getParameterCount(); i++) {
				EffectParam<?> param = fx.getParameter(i);
				this.fxArgs.parameters.put(param.getID(), param.getValue(this.fxArgs.time));
			}
			
			// Script execution!
			fx.getScript().update(this.fxArgs.parameters, this.fxArgs.time, this.fxArgs.width, this.fxArgs.height);
			
			// Clear the dest buffer
			this.gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
			this.gl.glClearStencil(0x00);
			this.gl.glClearDepth(1.0);
			this.gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
			
			// Bind the framebuffer we're gonna render to
			this.gl.glBindFramebuffer(GL_FRAMEBUFFER, this.trackRenderingFBO);
			
			// DO IT TO EM
			vfx.applyVideo(this.gl, this.fxArgs);
		}
		
		// When we leave, and if the last effect actually had an effect, the last rendering is on the dest buffer.
		// So we have to swap it to get the final result on the back buffer.
		if(!this.fxArgs.cancelled) {
			dest.swapBuffers();
			
			GLRenderer.this.gl.glBindTexture(GL_TEXTURE_2D, dest.getBackBuffer());
			GLRenderer.this.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
			GLRenderer.this.gl.glGenerateMipmap(GL_TEXTURE_2D);
		}
	}
	
	private void renderAllTracks(Composition comp, float time) {
		for(int i = 0; i < this.trackRenderList.size(); i++) {
			this.renderTrack(comp, this.trackRenderList.get(i), time);
		}
	}
	
	private void renderComposition(Composition comp, float time, int cacheIndex) {
		CachedFramebuffer dest;
		if(!this.compFBOs.containsKey(comp)) {
			dest = new CachedFramebuffer(comp);
			this.compFBOs.put(comp, dest);
			comp.addListener(this);
		} else {
			dest = this.getFramebufferFor(comp).update(comp);
		}
		
		// Setup the viewport
		this.gl.glViewport(0, 0, dest.width, dest.height);
		
		// Disable blending before doing shit
		this.gl.glDisable(GL_BLEND);
		this.gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		// Render all the tracks
		this.prepareTrackRenderList(comp, time);
		this.renderAllTracks(comp, time);
		
		// Compose them, on the dest framebuffer
		this.gl.glBindFramebuffer(GL_FRAMEBUFFER, dest.fbo);
		this.gl.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, dest.texture[cacheIndex], 0);
		
		this.gl.glClearColor(
			comp.getClearColor().getRedf(),
			comp.getClearColor().getGreenf(),
			comp.getClearColor().getBluef(),
			comp.getClearColor().getAlphaf()
		);
		this.gl.glClear(GL_COLOR_BUFFER_BIT);
		
		this.gl.glUseProgram(this.compose_prog);
		this.gl.glBindVertexArray(this.compose_quadVAO);
		
		List<Track> tracks = comp.getTimeline().getTracks();
		for(int i = 0; i < tracks.size(); i++) {
			Track t = tracks.get(i);
			
			if(!t.isEnabled() || !t.isActiveAt(time) || t.getEffectCount() == 0) continue;
			
			// Don't compose if there's no effect
			boolean noEffect = true;
			for(int j = 0; j < t.getEffectCount(); j++) {
				TrackEffectInstance fx = t.getEffect(j);
				
				noEffect &= !(fx instanceof VideoEffectInstance) || !fx.isEnabled();
				
				if(!noEffect) break;
			}
			if(noEffect) continue;
			
			// We render to the alt texture and then swap it to the used cache slot
			this.gl.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, dest.textureAlt, 0);
			
			this.gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
			this.gl.glClear(GL_COLOR_BUFFER_BIT);
			
			// We use the current cache slot as input
			this.glu.uniformTexture(this.gl, this.compose_loc_inputComp, dest.texture[cacheIndex], 1);
			this.glu.uniformTexture(this.gl, this.compose_loc_inputTex, this.getTrackTexture(comp, t).getBackBuffer(), 0);
			this.gl.glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
			
			// We then swap the alt texture we just rendered to with the cache slot
			dest.swapAlt(this.gl, cacheIndex);
		}
	}

	public int getCurrentDestTexture(Composition comp) {
		return this.getTextureForCacheIndex(comp, this.mainRenderer.getCurrentDestCacheFrame().index);
	}
	
	public int getLastTextureFor(Composition comp, int frame) {
		return this.getTextureForCacheIndex(comp, this.mainRenderer.getLastCacheFor(comp, frame));
	}
	
	public int getTextureForFrame(Composition comp, int frame) {
		return this.getTextureForCacheIndex(comp, this.mainRenderer.getCacheFor(comp, frame));
	}
	private int getTextureForCacheIndex(Composition comp, int index) {
		if(index > this.mainRenderer.getCacheSize() || index < 0) return -1;
		
		return this.getFramebufferFor(comp).texture[index];
	}
	
	private CachedFramebuffer getFramebufferFor(Composition comp) {
		return this.compFBOs.get(comp);
	}
	
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) { }

	public void clearColorChanged(Composition comp) {
	}

	public void lengthChanged(Composition comp) {
	}

	public void framerateChanged(Composition comp) {
	}

	public void resize(Composition comp) {
	}

	public void dispose(Composition comp) {
		this.disposedCompositions.add(comp);
	}
}
