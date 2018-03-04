package io.github.nasso.urmusic.model.renderer.video.glvg;

import java.nio.FloatBuffer;
import java.util.ArrayDeque;
import java.util.Deque;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.GL3;

import io.github.nasso.urmusic.common.MathUtils;
import io.github.nasso.urmusic.common.RGBA32;
import io.github.nasso.urmusic.model.renderer.video.GLUtils;

/**
 * nasso is coding like the W3C again...
 * https://www.w3.org/TR/2dcontext
 * 
 * @author nasso
 */
public class GLVG implements VGPathMethods {
	private static final boolean DEBUG = true;
	private static final float ARC_EDGE_SIZE = 16;
	private static final int DEFAULT_BUFFER_SIZE = 32;
	
	// Buffers
	private FloatBuffer vertBuffer = FloatBuffer.allocate(GLVG.DEFAULT_BUFFER_SIZE * 32);
	private int nverts;
	
	// Context
	private GLUtils glu = new GLUtils();
	private GL3 gl;
	private int width, height;
	
	// GL
	private int gl_vbo_verts;
	private int gl_vao_path;
	private int gl_vao_quad;
	private int gl_prog_stencil;
	private int gl_prog_stencil_vec2_surfaceSize;
	private int gl_prog_fill;
	private int gl_prog_fill_vec4_fillRGBA;

	// State
	private Deque<VGState> stateStack = new ArrayDeque<>();
	
	public GLVG(GL3 gl) {
		this.gl_vbo_verts = this.glu.genBuffer(gl);
		this.gl_vao_path = this.glu.genVertexArray(gl);
		this.gl_vao_quad = this.glu.createFullQuadVAO(gl);
		this.gl_prog_stencil = this.glu.createProgram(gl, "glvg/", "stencil.vs", "stencil.fs");
		this.gl_prog_fill = this.glu.createProgram(gl, "glvg/", "fill.vs", "fill.fs");
		
		this.gl_prog_stencil_vec2_surfaceSize = gl.glGetUniformLocation(this.gl_prog_stencil, "surfaceSize");
		this.gl_prog_fill_vec4_fillRGBA = gl.glGetUniformLocation(this.gl_prog_fill, "fillRGBA");
		
		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, this.gl_vbo_verts);
		gl.glBufferData(GL.GL_ARRAY_BUFFER, this.vertBuffer.remaining() << 4, this.vertBuffer, GL2ES2.GL_STREAM_DRAW);
		
		gl.glBindVertexArray(this.gl_vao_path);
		gl.glEnableVertexAttribArray(0);
		gl.glVertexAttribPointer(0, 2, GL.GL_FLOAT, false, 0, 0);
		gl.glBindVertexArray(0);
		
		this.stateStack.push(new VGState());
	}
	
	public void dispose(GL3 gl) {
		this.glu.dispose(gl);
	}
	
	private VGState state() {
		return this.stateStack.peek();
	}
	
	// Frame control
	public void begin(GL3 gl, int width, int height) {
		this.gl = gl;
		this.width = width;
		this.height = height;
	}
	
	// State stuff
	public void save() {
		this.stateStack.push(this.state().clone());
	}
	
	public void restore() {
		if(this.stateStack.size() > 1)
			this.stateStack.pop();
	}
	
	public void setFillColor(int rgba) {
		this.state().fillStyle.setRGBA(rgba);
	}
	
	public void setStrokeColor(int rgba) {
		this.state().strokeStyle.setRGBA(rgba);
	}
	
	public void setLineWidth(float val) {
		this.state().lineWidth = val;
	}
	
	public void setMiterLimit(float val) {
		this.state().miterLimit = val;
	}
	
	public void setGlobalAlpha(float val) {
		this.state().globalAlpha = val;
	}
	
	public void setLineCaps(VGLineCap caps) {
		this.state().lineCaps = caps;
	}
	
	public void setLineJoins(VGLineJoin joins) {
		this.state().lineJoins = joins;
	}
	
	// Rectangle stuff
	public void clearRect(int x, int y, int w, int h) {
		
	}
	
	public void fillRect(int x, int y, int w, int h) {
		this.save();
		
		this.beginPath();
		this.rect(x, y, w, h);
		this.fill();
		
		this.restore();
	}
	
	public void strokeRect(int x, int y, int w, int h) {
		this.save();
		
		this.beginPath();
		this.rect(x, y, w, h);
		this.stroke();
		
		this.restore();
	}
	
	// Drawing paths
	public void beginPath() {
		this.state().path.clear();
	}
	
	public void fill() {
		this.loadPathVertices(this.state().path.getPath());
		this.stencilFill(this.state().fillStyle);
	}
	
	public void stroke() {
		this.loadPathVertices(this.state().path.getPath().trace(this.state().lineWidth, this.state().lineCaps, this.state().lineJoins));
		this.stencilStroke(this.state().strokeStyle);
	}
	
	// CanvasPathMethods
	public void moveTo(float x, float y) {
		this.state().path.moveTo(x, y);
	}
	
	public void closePath() {
		this.state().path.closePath();
	}
	
	public void lineTo(float x, float y) {
		this.state().path.lineTo(x, y);
	}
	
	// Actual rendering
	private void loadPathVertices(VGPath path) {
		this.nverts = 0;
		for(int i = 0; i < path.subPaths.size(); i++) {
			VGSubPath sub = path.subPaths.get(i);
			this.nverts += sub.points.size();
		}
		
		int bufSize = this.nverts * 2;
		if(this.vertBuffer.capacity() < bufSize) {
			// Allocate more spACE
			this.vertBuffer = FloatBuffer.allocate(bufSize);
		}
		
		this.vertBuffer.clear();
		for(int i = 0; i < path.subPaths.size(); i++) {
			VGSubPath sub = path.subPaths.get(i);
			
			for(int j = 0; j < sub.points.size(); j++) {
				VGPoint p = sub.points.get(j);
				
				this.vertBuffer.put(p.x);
				this.vertBuffer.put(p.y);
			}
		}
		
		this.vertBuffer.flip();
		
		this.gl.glBindBuffer(GL.GL_ARRAY_BUFFER, this.gl_vbo_verts);
		this.gl.glBufferData(GL.GL_ARRAY_BUFFER, this.vertBuffer.remaining() << 4, this.vertBuffer, GL2ES2.GL_STREAM_DRAW);
	}
	
	private void stencilFill(RGBA32 rgba) {
		// -- Stencil pass
		// Clear the stencil buffer
		this.gl.glEnable(GL.GL_STENCIL_TEST);
		this.gl.glClear(GL.GL_STENCIL_BUFFER_BIT);
		
		// Stencil invert mode
		this.gl.glColorMask(false, false, false, false);
		this.gl.glStencilMask(0xFF);
		this.gl.glStencilFunc(GL.GL_ALWAYS, 1, 0xFF);
		this.gl.glStencilOp(GL.GL_INVERT, GL.GL_INVERT, GL.GL_INVERT);
		
		this.gl.glUseProgram(this.gl_prog_stencil);
		this.gl.glUniform2f(this.gl_prog_stencil_vec2_surfaceSize, this.width, this.height);
		
		this.gl.glBindVertexArray(this.gl_vao_path);
		this.gl.glDrawArrays(GL.GL_TRIANGLE_FAN, 0, this.nverts);
		
		// Restore state
		this.gl.glColorMask(true, true, true, true);
		this.gl.glStencilMask(0xFF);
		this.gl.glStencilFunc(GL.GL_NOTEQUAL, 0, 0xFF);
		this.gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_KEEP);
		
		// -- Filling pass
		this.gl.glUseProgram(this.gl_prog_fill);
		this.gl.glUniform4f(this.gl_prog_fill_vec4_fillRGBA, rgba.getRedf(), rgba.getGreenf(), rgba.getBluef(), rgba.getAlphaf());
		
		this.gl.glBindVertexArray(this.gl_vao_quad);
		this.gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, 4);
		
		this.gl.glDisable(GL.GL_STENCIL_TEST);
		
		if(GLVG.DEBUG) {
			this.gl.glUseProgram(this.gl_prog_stencil);
			this.gl.glUniform2f(this.gl_prog_stencil_vec2_surfaceSize, this.width, this.height);

			this.gl.glBindVertexArray(this.gl_vao_path);
			this.gl.glDrawArrays(GL.GL_LINE_LOOP, 0, this.nverts);
			return;
		}
	}
	
	// Used for strokes ONLY (the thing you get from VGPath::trace())
	// Notable differences are the use of GL_REPLACE instead of GL_INVERT in
	// stencil pass and GL_TRIANGLE_STRIP because of the layout differences in the path description
	private void stencilStroke(RGBA32 rgba) {
		// -- Stencil pass
		// Clear the stencil buffer
		this.gl.glEnable(GL.GL_STENCIL_TEST);
		this.gl.glClear(GL.GL_STENCIL_BUFFER_BIT);
		
		// Stencil invert mode
		this.gl.glColorMask(false, false, false, false);
		this.gl.glStencilMask(0xFF);
		this.gl.glStencilFunc(GL.GL_ALWAYS, 1, 0xFF);
		this.gl.glStencilOp(GL.GL_REPLACE, GL.GL_REPLACE, GL.GL_REPLACE);
		
		this.gl.glUseProgram(this.gl_prog_stencil);
		this.gl.glUniform2f(this.gl_prog_stencil_vec2_surfaceSize, this.width, this.height);
		
		this.gl.glBindVertexArray(this.gl_vao_path);
		this.gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, this.nverts);
		
		// Restore state
		this.gl.glColorMask(true, true, true, true);
		this.gl.glStencilMask(0xFF);
		this.gl.glStencilFunc(GL.GL_NOTEQUAL, 0, 0xFF);
		this.gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_KEEP);
		
		// -- Filling pass
		this.gl.glUseProgram(this.gl_prog_fill);
		this.gl.glUniform4f(this.gl_prog_fill_vec4_fillRGBA, rgba.getRedf(), rgba.getGreenf(), rgba.getBluef(), rgba.getAlphaf());
		
		this.gl.glBindVertexArray(this.gl_vao_quad);
		this.gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, 4);
		
		this.gl.glDisable(GL.GL_STENCIL_TEST);
		
		if(GLVG.DEBUG) {
			this.gl.glUseProgram(this.gl_prog_stencil);
			this.gl.glUniform2f(this.gl_prog_stencil_vec2_surfaceSize, this.width, this.height);

			this.gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_LINE);
			
			this.gl.glBindVertexArray(this.gl_vao_path);
			this.gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, this.nverts);
			
			this.gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2GL3.GL_FILL);
			return;
		}
	}
	
	static int getArcSteps(float radius) {
		// I have no idea how that works but this thing magically gives us how many steps are needed in a full circle for it to have edges of approximately ARC_EDGE_SIZE
		return Math.max((int) Math.round(MathUtils.PI_2 / Math.acos(-(GLVG.ARC_EDGE_SIZE * GLVG.ARC_EDGE_SIZE) / (2 * radius * radius) + 1)), 4);
	}
}
