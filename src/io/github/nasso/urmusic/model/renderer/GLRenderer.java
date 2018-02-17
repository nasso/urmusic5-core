package io.github.nasso.urmusic.model.renderer;

import static com.jogamp.opengl.GL.*;

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

/**
 * Uses OpenGL 3
 * @author nasso
 */
public class GLRenderer implements GLEventListener, CompositionListener {
	private static final class CachedFramebuffer {
		private static int cacheSize;
		private static IntBuffer bufTex;
		
		private int width, height;
		
		public int fbo;
		public int textureAlt;
		
		public int[] texture;
		public boolean[] dirty;
		
		public CachedFramebuffer(GL3 gl, GLUtils glu, Composition comp) {
			this.fbo = glu.genFramebuffer(gl);
			this.texture = new int[cacheSize];
			this.dirty = new boolean[cacheSize];
			
			this.makeAllDirty(true);
			
			this.width = comp.getWidth();
			this.height = comp.getHeight();
			
			this.textureAlt = glu.genTexture(gl);
			glu.genTextures(gl, cacheSize, bufTex);
			
			for(int i = 0; i <= cacheSize; i++) {
				int t = i == cacheSize ? this.textureAlt : bufTex.get(i);

				gl.glBindTexture(GL_TEXTURE_2D, t);
				gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, this.width, this.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
				gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
				gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
				gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
				gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
			}
			
			bufTex.get(this.texture);
			bufTex.flip();
		}
		
		public void makeAllDirty(boolean dirty) {
			for(int i = 0; i < this.dirty.length; i++) this.dirty[i] = dirty;
		}
		
		public static void setCacheSize(int size) {
			cacheSize = size;
			bufTex = Buffers.newDirectIntBuffer(size);
		}

		public CachedFramebuffer update(GL3 gl, Composition comp) {
			if(comp.getWidth() != this.width || comp.getHeight() != this.height) {
				this.width = comp.getWidth();
				this.height = comp.getHeight();
				
				for(int i = 0; i <= this.texture.length; i++) {
					gl.glBindTexture(GL_TEXTURE_2D, i == this.texture.length ? this.textureAlt : this.texture[i]);
					gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, this.width, this.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
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
	}
	
	private final class TrackRenderTexture {
		public Composition comp;
		
		public int tex_id[] = new int[2];
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
		
		public TrackRenderTexture update() {
			for(int i = 0; i < this.tex_id.length; i++) {
				if(this.tex_id[i] != 0 && this.width == this.comp.getWidth() && this.height == this.comp.getHeight()) continue;
				
				if(this.tex_id[i] == 0) {
					this.tex_id[i] = GLRenderer.this.glu.genTexture(GLRenderer.this.gl);
					
					GLRenderer.this.gl.glBindTexture(GL_TEXTURE_2D, this.tex_id[i]);
					GLRenderer.this.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
					GLRenderer.this.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
					GLRenderer.this.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
					GLRenderer.this.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
				} else GLRenderer.this.gl.glBindTexture(GL_TEXTURE_2D, this.tex_id[i]);
				
				GLRenderer.this.gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, this.comp.getWidth(), this.comp.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
			}
			
			this.width = this.comp.getWidth();
			this.height = this.comp.getHeight();
			
			return this;
		}
	}
	
	private Map<Composition, CachedFramebuffer> compFBOs = new HashMap<>();
	private Map<Track, TrackRenderTexture> tracksTextures = new HashMap<>();
	public final Renderer mainRenderer;
	private GL3 gl;
	private GLUtils glu;
	
	private EffectArgs fxArgs = new EffectArgs();
	
	private List<Composition> disposedCompositions = new ArrayList<>();
	
	private int trackRenderingFBO;
	private List<Track> trackRenderList = new ArrayList<>();

	private int compose_prog, compose_quadVAO;
	private int compose_loc_inputTex, compose_loc_inputComp;
	
	public GLRenderer(Renderer renderer) {
		this.mainRenderer = renderer;
		this.glu = new GLUtils();
	}
	
	public GLProfile getProfile() {
		return GLProfile.getGL2GL3();
	}
	
	public void initEffect(TrackEffect fx) {
		GLContext ctx = this.mainRenderer.drawable.getContext();
		ctx.makeCurrent();
		this.gl = ctx.getGL().getGL3();
		
		fx.globalVideoSetup(this.gl);
		
		ctx.release();
	}
	
	public void disposeEffect(TrackEffect fx) {
		GLContext ctx = this.mainRenderer.drawable.getContext();
		ctx.makeCurrent();
		this.gl = ctx.getGL().getGL3();
		
		fx.globalVideoDispose(this.gl);
		
		ctx.release();
	}
	
	public void disposeTrack(Track t) {
		GLContext ctx = this.mainRenderer.drawable.getContext();
		ctx.makeCurrent();
		this.gl = ctx.getGL().getGL3();
		
		for(int i = 0; i < t.getEffectCount(); i++) {
			TrackEffectInstance vfx = t.getEffect(i);
			
			vfx.disposeVideo(this.gl);
		}
		
		TrackRenderTexture tex = this.tracksTextures.get(t);
		
		if(tex != null) {
			for(int i = 0; i < tex.tex_id.length; i++)
				this.glu.deleteTexture(this.gl, tex.tex_id[i]);
		}
		
		ctx.release();
	}
	
	public TrackRenderTexture getTrackTexture(Composition comp, Track t) {
		TrackRenderTexture tex = this.tracksTextures.get(t);
		
		if(tex == null)
			this.tracksTextures.put(t, tex = new TrackRenderTexture(comp));
		
		return tex.update();
	}
	
	public void disposeEffectInstance(TrackEffectInstance fx) {
		GLContext ctx = this.mainRenderer.drawable.getContext();
		ctx.makeCurrent();
		this.gl = ctx.getGL().getGL3();
		
		fx.disposeVideo(this.gl);
		
		ctx.release();
	}
	
	public void makeCompositionDirty(Composition comp) {
		if(this.compFBOs.containsKey(comp)) {
			this.compFBOs.get(comp).makeAllDirty(true);
		}
	}

	public void init(GLAutoDrawable drawable) {
		this.gl = drawable.getGL().getGL3();
		
		CachedFramebuffer.setCacheSize(this.mainRenderer.getCachedFrames().length);
		
		this.trackRenderingFBO = this.glu.genFramebuffer(this.gl);
		
		this.compose_prog = this.glu.createProgram(this.gl, "track_composer.vs", "track_composer.fs");
		this.compose_loc_inputTex = this.gl.glGetUniformLocation(this.compose_prog, "trackInput");
		this.compose_loc_inputComp = this.gl.glGetUniformLocation(this.compose_prog, "compOutput");
		
		this.compose_quadVAO = this.glu.genFullQuadVAO(this.gl);
		
		this.gl.glBindVertexArray(0);
		this.gl.glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	public void dispose(GLAutoDrawable drawable) {
		this.gl = drawable.getGL().getGL3();

		this.glu.dispose(this.gl);
	}
	
	public void display(GLAutoDrawable drawable) {
		this.gl = drawable.getGL().getGL3();
		
		CachedFrame dest = this.mainRenderer.getCurrentDestCacheFrame();
		
		this.renderComposition(dest.comp, dest.frame_id, dest.index);
		
		this.gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}
	
	private void prepareTrackRenderList(Composition comp, int frame_id) {
		this.trackRenderList.clear();
		
		List<Track> tracks = comp.getTimeline().getTracks();
		track_loop: for(int i = 0; i < tracks.size(); i++) {
			Track t = tracks.get(i);
			
			if(!t.isEnabled() || !t.isActiveAt(frame_id) || t.getEffectCount() == 0) continue;
			
			for(int j = 0; j < t.getEffectCount(); j++) {
				TrackEffectInstance fx = t.getEffect(j);
				
				if(fx.getEffectClass().isVideoEffect() && fx.isEnabled()) {
					this.trackRenderList.add(t);
					continue track_loop;
				}
			}
		}
	}
	
	// TODO: CompositeTrack rendering
	private void renderTrack(Composition comp, Track t, int frame_id) {
		TrackRenderTexture dest = this.getTrackTexture(comp, t);
		
		// We bind the framebuffer and the first dest buffer
		this.gl.glBindFramebuffer(GL_FRAMEBUFFER, this.trackRenderingFBO);
		this.gl.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, dest.getDestBuffer(), 0);
		
		// We clear the first dest buffer
		this.gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		this.gl.glClear(GL_COLOR_BUFFER_BIT);
		
		// Apply the effects
		for(int j = 0; j < t.getEffectCount(); j++) {
			TrackEffectInstance fx = t.getEffect(j);
			
			// We only care about enabled video effects
			if(!fx.isEnabled() || !fx.getEffectClass().isVideoEffect()) continue;
			
			// Init the effect first to make sure everything's oki
			if(!fx.hasSetupVideo()) fx.setupVideo(this.gl);
			
			// Swap dest/back buffers, to bring the previous dest buffer to the back and get a fresh usable dest buffer
			dest.swapBuffers();
			
			// Bind the framebuffer and the dest buffer
			this.gl.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, dest.getDestBuffer(), 0);
			
			// Clear the dest buffer
			this.gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
			this.gl.glClear(GL_COLOR_BUFFER_BIT);
			
			// Effect arguments
			this.fxArgs.clear();
			this.fxArgs.width = dest.width;
			this.fxArgs.height = dest.height;
			this.fxArgs.frame = frame_id;
			this.fxArgs.texInput = dest.getBackBuffer();
			this.fxArgs.fboOutput = this.trackRenderingFBO;
			
			// DO IT TO EM
			fx.applyVideo(this.gl, this.fxArgs);
		}
		
		// When we leave, the last rendering is on the dest buffer, so swap it one last time
		dest.swapBuffers();
	}
	
	private void renderAllTracks(Composition comp, int frame_id) {
		for(int i = 0; i < this.trackRenderList.size(); i++) {
			this.renderTrack(comp, this.trackRenderList.get(i), frame_id);
		}
	}
	
	private void renderComposition(Composition comp, int frame_id, int cacheIndex) {
		CachedFramebuffer dest;
		if(!this.compFBOs.containsKey(comp)) {
			dest = new CachedFramebuffer(this.gl, this.glu, comp);
			this.compFBOs.put(comp, dest);
			comp.addListener(this);
		} else {
			dest = this.getFramebufferFor(comp).update(this.gl, comp);
		}
		
		// Setup the viewport
		this.gl.glViewport(0, 0, dest.width, dest.height);
		
		// Disable blending before doing shit
		this.gl.glDisable(GL_BLEND);
		this.gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		// Render all the tracks
		this.prepareTrackRenderList(comp, frame_id);
		this.renderAllTracks(comp, frame_id);
		
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
			
			if(!t.isEnabled() || !t.isActiveAt(frame_id) || t.getEffectCount() == 0) continue;
			
			// Don't compose if there's no effect
			boolean noEffect = true;
			for(int j = 0; j < t.getEffectCount(); j++) {
				TrackEffectInstance fx = t.getEffect(j);
				
				noEffect &= !fx.getEffectClass().isVideoEffect() || !fx.isEnabled();
				
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
		this.mainRenderer.makeCompositionDirty(comp);
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
