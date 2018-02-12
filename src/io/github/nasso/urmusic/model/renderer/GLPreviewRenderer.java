package io.github.nasso.urmusic.model.renderer;

import static com.jogamp.opengl.GL.*;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;

import io.github.nasso.urmusic.model.UrmusicModel;

public class GLPreviewRenderer implements GLEventListener {
	private final GLRenderer parent;
	
	private int quadProg, quadVAO, quadProgTextureLocation, quadProgScaleLocation;
	
	private GLUtils glu;
	
	public GLPreviewRenderer(GLRenderer main) {
		this.parent = main;
		this.glu = new GLUtils();
	}

	public void init(GLAutoDrawable drawable) {
		GL3 gl = drawable.getGL().getGL3();
		
		this.quadProg = this.glu.createProgram(gl, "full_quad.vs", "full_quad.fs");
		gl.glUseProgram(this.quadProg);
		this.quadProgTextureLocation = gl.glGetUniformLocation(this.quadProg, "color");
		this.quadProgScaleLocation = gl.glGetUniformLocation(this.quadProg, "scale");
		
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
		
		gl.glClearColor(1, 1, 1, 1);
		gl.glClear(GL_COLOR_BUFFER_BIT);
		
		gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
		
		gl.glUseProgram(this.quadProg);
		
		// Calc ratio
		float sw = drawable.getSurfaceWidth();
		float sh = drawable.getSurfaceHeight();
		float rw = UrmusicModel.getFocusedComposition().getWidth();
		float rh = UrmusicModel.getFocusedComposition().getHeight();
		float s = Math.min(sw / rw, sh / rh);
		float w = rw * s, h = rh * s;
		
		gl.glViewport(0, 0, (int) sw, (int) sh);
		
		gl.glUniform2f(this.quadProgScaleLocation, w / sw, h / sh);
		this.glu.uniformTexture(gl, this.quadProgTextureLocation, tex, 0);
		
		gl.glBindVertexArray(this.quadVAO);
		gl.glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		// GL3 gl = drawable.getGL().getGL3();
	}
}
