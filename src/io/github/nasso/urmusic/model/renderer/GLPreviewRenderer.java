package io.github.nasso.urmusic.model.renderer;

import static com.jogamp.opengl.GL.*;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;

import io.github.nasso.urmusic.model.UrmusicModel;

public class GLPreviewRenderer implements GLEventListener {
	public static enum ViewMode {
		CUSTOM,
		FIT,
		FIT_MAX,
		ORIGINAL
	}
	
	private ViewMode viewMode = ViewMode.FIT;
	
	private final GLRenderer parent;
	
	private FloatBuffer _buf = Buffers.newDirectFloatBuffer(16);
	private Matrix4f _mat4 = new Matrix4f();
	
	private int quadProg, quadVAO, quadProgTextureLocation, loc_xform;
	
	private GLUtils glu;
	
	private float camX = 0.0f, camY = 0.0f, camZoom = 1.0f;
	
	public GLPreviewRenderer(GLRenderer main) {
		this.parent = main;
		this.glu = new GLUtils();
	}

	public ViewMode getViewMode() {
		return this.viewMode;
	}

	public void setViewMode(ViewMode viewMode) {
		if(this.viewMode == viewMode) return;
		
		this.viewMode = viewMode;
	}
	
	public void updateCamera(float camX, float camY, float camZoom) {
		this.camX = camX;
		this.camY = camY;
		this.camZoom = camZoom;
	}

	public void init(GLAutoDrawable drawable) {
		GL3 gl = drawable.getGL().getGL3();
		
		this.quadProg = this.glu.createProgram(gl, "full_quad.vs", "full_quad.fs");
		gl.glUseProgram(this.quadProg);
		this.quadProgTextureLocation = gl.glGetUniformLocation(this.quadProg, "color");
		this.loc_xform = gl.glGetUniformLocation(this.quadProg, "xform");
		
		this.quadVAO = this.glu.genFullQuadVAO(gl);
		
		gl.glUseProgram(0);
	}

	public void dispose(GLAutoDrawable drawable) {
		GL3 gl = drawable.getGL().getGL3();
		
		this.glu.dispose(gl);
	}

	public void display(GLAutoDrawable drawable) {
		GL3 gl = drawable.getGL().getGL3();
		
		int tex = this.parent.getLastTextureFor(UrmusicModel.getFocusedComposition(), UrmusicModel.getFrameCursor());
		if(!gl.glIsTexture(tex)) return;
		
		// Calc ratio
		float sw = drawable.getSurfaceWidth();
		float sh = drawable.getSurfaceHeight();
		float rw = UrmusicModel.getFocusedComposition().getWidth();
		float rh = UrmusicModel.getFocusedComposition().getHeight();
		float s;
		
		if(this.viewMode == ViewMode.FIT_MAX) s = Math.min(Math.min(sw / rw, sh / rh), 1.0f);
		else if(this.viewMode == ViewMode.ORIGINAL) s = 1.0f;
		else s = Math.min(sw / rw, sh / rh);
		
		float w = rw * s, h = rh * s;
		
		this._mat4.identity();
		this._mat4.scale(w / sw, h / sh, 1.0f);
		this._mat4.translate(-this.camX / w * 2, -this.camY / h * 2, 0.0f);
		this._mat4.scale(this.camZoom);
		this._mat4.get(this._buf);
		
		gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
		gl.glViewport(0, 0, (int) sw, (int) sh);
		
		gl.glClearColor(0.9f, 0.9f, 0.9f, 1);
		gl.glClear(GL_COLOR_BUFFER_BIT);
		
		gl.glUseProgram(this.quadProg);
		
		gl.glUniformMatrix4fv(this.loc_xform, this._buf.remaining() >> 4, false, this._buf);
		this.glu.uniformTexture(gl, this.quadProgTextureLocation, tex, 0);
		
		gl.glBindVertexArray(this.quadVAO);
		gl.glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		// GL3 gl = drawable.getGL().getGL3();
	}
}
