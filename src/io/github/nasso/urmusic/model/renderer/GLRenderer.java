package io.github.nasso.urmusic.model.renderer;

import static com.jogamp.opengl.GL.*;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		private static IntBuffer bufFBO;
		
		private int width, height;
		
		public int textureAlt;
		
		public int[] texture, fbo;
		public boolean[] dirty;
		
		private GLUtils glu;
		
		public CachedFramebuffer(GL3 gl, GLUtils glu, Composition comp) {
			this.glu = glu;
			
			this.texture = new int[cacheSize];
			this.fbo = new int[cacheSize];
			this.dirty = new boolean[cacheSize];
			
			this.makeAllDirty(true);
			
			this.width = comp.getWidth();
			this.height = comp.getHeight();
			
			gl.glBindTexture(GL_TEXTURE_2D, this.textureAlt = glu.genTexture(gl));
			gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, this.width, this.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
			
			this.glu.createFramebuffers(gl, cacheSize, this.width, this.height, bufTex, bufFBO);
			
			bufTex.get(this.texture);
			bufFBO.get(this.fbo);

			bufTex.flip();
			bufFBO.flip();
		}
		
		public void makeAllDirty(boolean dirty) {
			for(int i = 0; i < this.dirty.length; i++) this.dirty[i] = dirty;
		}
		
		public static void setCacheSize(int size) {
			cacheSize = size;
			bufTex = IntBuffer.allocate(size);
			bufFBO = IntBuffer.allocate(size);
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
		
		public void swapAlt(GL3 gl, int i) {
			int alt = this.textureAlt;
			this.textureAlt = this.texture[i];
			this.texture[i] = alt;

			gl.glBindFramebuffer(GL_FRAMEBUFFER, this.fbo[i]);
			gl.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, this.texture[i], 0);
		}
	}
	
	private Map<Composition, CachedFramebuffer> compFBOs = new HashMap<>();
	public final Renderer mainRenderer;
	private GL3 gl;
	private GLUtils glu;
	
	private EffectArgs fxArgs = new EffectArgs();
	
	private List<Composition> disposedCompositions = new ArrayList<>();
	
	public GLRenderer(Renderer renderer) {
		this.mainRenderer = renderer;
		this.glu = new GLUtils();
	}
	
	public GLProfile getProfile() {
		return GLProfile.getGL2GL3();
	}
	
	public void initEffect(TrackEffect vfx) {
		GLContext ctx = this.mainRenderer.drawable.getContext();
		ctx.makeCurrent();
		this.gl = ctx.getGL().getGL3();
		
		vfx.globalVideoSetup(this.gl);
		
		ctx.release();
	}
	
	public void disposeEffect(TrackEffect vfx) {
		GLContext ctx = this.mainRenderer.drawable.getContext();
		ctx.makeCurrent();
		this.gl = ctx.getGL().getGL3();
		
		vfx.globalVideoDispose(this.gl);
		
		ctx.release();
	}
	
	public void disposeTrack(Track vt) {
		GLContext ctx = this.mainRenderer.drawable.getContext();
		ctx.makeCurrent();
		this.gl = ctx.getGL().getGL3();
		
		for(int i = 0; i < vt.getEffectCount(); i++) {
			TrackEffectInstance vfx = vt.getEffect(i);
			
			vfx.disposeVideo(this.gl);
		}
		
		ctx.release();
	}
	
	public void disposeEffectInstance(TrackEffectInstance vfx) {
		GLContext ctx = this.mainRenderer.drawable.getContext();
		ctx.makeCurrent();
		this.gl = ctx.getGL().getGL3();
		
		vfx.disposeVideo(this.gl);
		
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
	}
	
	public void dispose(GLAutoDrawable drawable) {
		this.gl = drawable.getGL().getGL3();

		this.glu.dispose(this.gl);
	}
	
	public void display(GLAutoDrawable drawable) {
		this.gl = drawable.getGL().getGL3();
		
		CachedFrame dest = this.mainRenderer.getCurrentDestCacheFrame();
		int cacheIndex = dest.index_on_creation;
		
		this.renderComposition(dest.comp, dest.frame_id, cacheIndex);
		
		this.gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
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
		
		int destFBO = dest.fbo[cacheIndex];
		
		
		// Bind framebuffer
		this.gl.glBindFramebuffer(GL_FRAMEBUFFER, destFBO);
		this.gl.glViewport(0, 0, dest.width, dest.height);
		
		// -- Composition --
		List<Track> tracks = comp.getTimeline().getTracks();
		
		int lastTrackIndex = -1;
		for(int i = 0; i < tracks.size(); i++) {
			Track t = tracks.get(i);
			
			if(t.isEnabled() && t.isActiveAt(frame_id)) lastTrackIndex = i;
		}
		
		if(lastTrackIndex == -1) {
			this.gl.glClearColor(
				comp.getClearColor().getRedf(),
				comp.getClearColor().getGreenf(),
				comp.getClearColor().getBluef(),
				comp.getClearColor().getAlphaf()
			);
			this.gl.glClear(GL_COLOR_BUFFER_BIT);
			
			return;
		}
		
		this.gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		this.gl.glClear(GL_COLOR_BUFFER_BIT);
		
		for(int i = 0; i <= lastTrackIndex; i++) {
			Track t = tracks.get(i);
			
			if(!t.isEnabled() || !t.isActiveAt(frame_id)) continue;
			
			/*
			TODO: CompositeTrack rendering
			// We only care about video tracks
			if(t instanceof CompositeTrack) {
				this.renderComposition(((CompositeTrack) t).getComposition(), frame_id, cacheIndex);
				
				// Rebind framebuffer
				this.gl.glBindFramebuffer(GL_FRAMEBUFFER, destFBO);
				this.gl.glViewport(0, 0, dest.width, dest.height);
			}
			*/
			
			int lastEffectIndex = -1;
			for(int j = 0; j < t.getEffectCount(); j++) {
				TrackEffectInstance fx = t.getEffect(j);
				
				if(fx.isEnabled() && fx.getEffectClass().isVideoEffect()) lastEffectIndex = j;
			}
			
			boolean isLastTrack = lastTrackIndex == i;
			boolean noEffect = lastEffectIndex == -1;
			
			if(isLastTrack && noEffect) {
				this.gl.glClearColor(
					comp.getClearColor().getRedf(),
					comp.getClearColor().getGreenf(),
					comp.getClearColor().getBluef(),
					comp.getClearColor().getAlphaf()
				);
				
				this.gl.glClear(GL_COLOR_BUFFER_BIT);
			}
			
			for(int j = 0; j <= lastEffectIndex; j++) {
				TrackEffectInstance fx = t.getEffect(j);
				
				// We only care about video effects
				if(!fx.isEnabled() || !fx.getEffectClass().isVideoEffect()) continue;
				
				dest.swapAlt(this.gl, cacheIndex);

				if(isLastTrack && j == lastEffectIndex) { // if last track and last effect (aka next is final comp)
					this.gl.glClearColor(
						comp.getClearColor().getRedf(),
						comp.getClearColor().getGreenf(),
						comp.getClearColor().getBluef(),
						comp.getClearColor().getAlphaf()
					);
				} else
					this.gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
				
				this.gl.glClear(GL_COLOR_BUFFER_BIT);
				
				this.fxArgs.clear();
				this.fxArgs.width = dest.width;
				this.fxArgs.height = dest.height;
				this.fxArgs.frame = frame_id;
				this.fxArgs.texInput = dest.textureAlt;
				this.fxArgs.fboOutput = destFBO;
				
				if(!fx.hasSetupVideo()) fx.setupVideo(this.gl);
				fx.applyVideo(this.gl, this.fxArgs);
			}
		}
	}

	public int getCurrentDestFBO(Composition comp) {
		return this.getFBOFor(comp, this.mainRenderer.getCurrentDestCacheFrame().index_on_creation);
	}
	
	public int getCurrentDestTexture(Composition comp) {
		return this.getTexFor(comp, this.mainRenderer.getCurrentDestCacheFrame().index_on_creation);
	}
	
	public int getLastTextureFor(Composition comp, int frame) {
		return this.getTexFor(comp, this.mainRenderer.getLastCacheFor(comp, frame));
	}
	
	public int getTextureFor(Composition comp, int frame) {
		return this.getTexFor(comp, this.mainRenderer.getCacheFor(comp, frame));
	}
	
	private CachedFramebuffer getFramebufferFor(Composition comp) {
		return this.compFBOs.get(comp);
	}
	
	private int getFBOFor(Composition comp, int index) {
		if(index > this.mainRenderer.getCacheSize() || index < 0) return -1;
		
		return this.getFramebufferFor(comp).fbo[index];
	}
	
	private int getTexFor(Composition comp, int index) {
		if(index > this.mainRenderer.getCacheSize() || index < 0) return -1;
		
		return this.getFramebufferFor(comp).texture[index];
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
