package io.github.nasso.urmusic.model.renderer.video;

import java.nio.FloatBuffer;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;

public class GLPreviewRenderer implements GLEventListener {
	private final GLPreviewer previewer;
	
	private FloatBuffer _buf = Buffers.newDirectFloatBuffer(16);
	
	private int quadProg, quadVAO, quadProgTextureLocation, loc_xform;
	
	private GLUtils glu;
	
	public GLPreviewRenderer(GLPreviewer previewer) {
		this.previewer = previewer;
		this.glu = new GLUtils();
	}

	public void init(GLAutoDrawable drawable) {
		GL3 gl = drawable.getGL().getGL3();
		
		this.quadProg = this.glu.createProgram(gl, "full_quad.vs", "full_quad.fs");
		gl.glUseProgram(this.quadProg);
		this.quadProgTextureLocation = gl.glGetUniformLocation(this.quadProg, "inputTex");
		this.loc_xform = gl.glGetUniformLocation(this.quadProg, "xform");
		
		this.quadVAO = this.glu.createFullQuadVAO(gl);
		
		gl.glUseProgram(0);
	}

	public void dispose(GLAutoDrawable drawable) {
		GL3 gl = drawable.getGL().getGL3();
		
		this.glu.dispose(gl);
	}

	public void display(GLAutoDrawable drawable) {
		GL3 gl = drawable.getGL().getGL3();
		
		int tex = this.previewer.getPreviewTexture();
		if(!gl.glIsTexture(tex)) return;

		float sw = drawable.getSurfaceWidth();
		float sh = drawable.getSurfaceHeight();
		
		this.previewer.calcPreviewTransform(sw, sh).get(this._buf);
		
		gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);
		gl.glViewport(0, 0, (int) sw, (int) sh);
		
		gl.glClearColor(0.9f, 0.9f, 0.9f, 1);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		
		gl.glUseProgram(this.quadProg);
		
		gl.glUniformMatrix4fv(this.loc_xform, this._buf.remaining() >> 4, false, this._buf);
		this.glu.uniformTexture(gl, this.quadProgTextureLocation, tex, 0);
		
		gl.glBindVertexArray(this.quadVAO);
		gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, 4);
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		// GL3 gl = drawable.getGL().getGL3();
	}
}
