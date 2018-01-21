package io.github.nasso.urmusic.model.renderer.gl3;

import static com.jogamp.opengl.GL.*;

import java.nio.FloatBuffer;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;

public class GL3PreviewRenderer implements GLEventListener {
	private final GL3Renderer parent;
	
	private int quadProg, quadVAO, quadProgTextureLocation, quadProgScaleLocation;
	
	public GL3PreviewRenderer(GL3Renderer main) {
		this.parent = main;
	}

	public void init(GLAutoDrawable drawable) {
		GL3 gl = drawable.getGL().getGL3();
		
		this.quadProg = GL3Utils.createProgram(gl, "full_quad.vs", "full_quad.fs");
		gl.glUseProgram(this.quadProg);
		this.quadProgTextureLocation = gl.glGetUniformLocation(this.quadProg, "color");
		this.quadProgScaleLocation = gl.glGetUniformLocation(this.quadProg, "scale");
		
		int quadPos = GL3Utils.genBuffer(gl);
		gl.glBindBuffer(GL_ARRAY_BUFFER, quadPos);
		gl.glBufferData(GL_ARRAY_BUFFER, 8 * 32, FloatBuffer.wrap(new float[] {
				-1, -1,
				1, -1,
				-1, 1,
				1, 1
		}), GL_STATIC_DRAW);
		
		this.quadVAO = GL3Utils.genVertexArray(gl);
		gl.glBindVertexArray(this.quadVAO);
		gl.glEnableVertexAttribArray(0);
		gl.glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);
		
		gl.glBindVertexArray(0);
		gl.glUseProgram(0);
	}

	public void dispose(GLAutoDrawable drawable) {
		// GL3 gl = drawable.getGL().getGL3();
	}

	public void display(GLAutoDrawable drawable) {
		GL3 gl = drawable.getGL().getGL3();
		
		gl.glClearColor(0, 0, 0, 1);
		gl.glClear(GL_COLOR_BUFFER_BIT);
		
		gl.glUseProgram(this.quadProg);
		
		// Calc ratio
		float sw = drawable.getSurfaceWidth();
		float sh = drawable.getSurfaceHeight();
		float rw = this.parent.mainRenderer.getWidth();
		float rh = this.parent.mainRenderer.getHeight();
		float s = Math.min(sw / rw, sh / rh);
		float w = rw * s, h = rh * s;
		
		gl.glUniform2f(this.quadProgScaleLocation, w / sw, h / sh);
		GL3Utils.uniformTexture(gl, this.quadProgTextureLocation, this.parent.getCurrentTexture(), 0);
		
		gl.glBindVertexArray(this.quadVAO);
		gl.glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		// GL3 gl = drawable.getGL().getGL3();
	}
}
