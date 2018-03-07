package io.github.nasso.urmusic.model.renderer.video.glvg;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2ES2.*;
import static com.jogamp.opengl.GL2ES3.*;
import static com.jogamp.opengl.GL2GL3.*;

import java.nio.FloatBuffer;
import java.util.ArrayDeque;
import java.util.Deque;

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
	private static final boolean DEBUG = false;
	private static final int MSAA_SAMPLES = 16;
	private static final float ARC_EDGE_SIZE = 16;
	private static final int DEFAULT_BUFFER_SIZE = 32;
	
	// Buffers
	private FloatBuffer vertBuffer = FloatBuffer.allocate(GLVG.DEFAULT_BUFFER_SIZE * 32);
	private int nverts;
	
	// Context
	private GLUtils glu = new GLUtils("glvg");
	private GL3 gl;
	private int width = -1, height = -1;
	
	// GL
	private int gl_fbo_dest;
	private int gl_tex_dest_color;
	private int gl_rbo_dest_depth;
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
		this.gl_fbo_dest = this.glu.genFramebuffer(gl);
		this.gl_tex_dest_color = this.glu.genTexture(gl);
		this.gl_rbo_dest_depth = this.glu.genRenderbuffer(gl);
		this.gl_vbo_verts = this.glu.genBuffer(gl);
		this.gl_vao_path = this.glu.genVertexArray(gl);
		this.gl_vao_quad = this.glu.createFullQuadVAO(gl);
		this.gl_prog_stencil = this.glu.createProgram(gl, "glvg/", "stencil.vs", "stencil.fs");
		this.gl_prog_fill = this.glu.createProgram(gl, "glvg/", "fill.vs", "fill.fs");
		
		this.gl_prog_stencil_vec2_surfaceSize = gl.glGetUniformLocation(this.gl_prog_stencil, "surfaceSize");
		this.gl_prog_fill_vec4_fillRGBA = gl.glGetUniformLocation(this.gl_prog_fill, "fillRGBA");
		
		this.glu.dumpError(gl, "get uniform locations");
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, this.gl_vbo_verts);
		gl.glBufferData(GL_ARRAY_BUFFER, this.vertBuffer.remaining() << 4, this.vertBuffer, GL_STREAM_DRAW);
		
		this.glu.dumpError(gl, "buffer data init");
		
		gl.glBindVertexArray(this.gl_vao_path);
		gl.glEnableVertexAttribArray(0);
		gl.glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);
		gl.glBindVertexArray(0);
		
		this.glu.dumpError(gl, "vao creation");
		
		this.stateStack.push(new VGState());
	}
	
	public void dispose(GL3 gl) {
		this.glu.dispose(gl);
	}
	
	private VGState state() {
		return this.stateStack.peek();
	}
	
	// Frame control
	/**
	 * You should wrap the rendering code between {@link GLVG#begin(GL3, int, int)} and {@link GLVG#end(int)},
	 * but you shouldn't do any other OpenGL rendering/state changes between those calls, because this actually
	 * binds another framebuffer (and that's why {@link GLVG#end(int)} needs the output FBO).
	 * 
	 * @param gl
	 * @param width
	 * @param height
	 */
	public void begin(GL3 gl, int width, int height) {
		this.gl = gl;
		
		gl.glBindFramebuffer(GL_FRAMEBUFFER, this.gl_fbo_dest);
		
		if(this.width != width || this.height != height) {
			gl.glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, this.gl_tex_dest_color);
			gl.glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, MSAA_SAMPLES, GL_RGBA, width, height, true);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
			
			gl.glBindRenderbuffer(GL_RENDERBUFFER, this.gl_rbo_dest_depth);
			gl.glRenderbufferStorageMultisample(GL_RENDERBUFFER, MSAA_SAMPLES, GL_DEPTH24_STENCIL8, width, height);

			if(this.width == -1 || this.height == -1) {
				gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D_MULTISAMPLE, this.gl_tex_dest_color, 0);
				gl.glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, this.gl_rbo_dest_depth);
			}
			
			gl.glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, 0);
			gl.glBindRenderbuffer(GL_RENDERBUFFER, 0);
			
			this.width = width;
			this.height = height;
		}
		
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
	}
	
	public void end(int destFBO) {
		this.gl.glBindFramebuffer(GL_READ_FRAMEBUFFER, this.gl_fbo_dest);
		this.gl.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, destFBO);
		this.gl.glBlitFramebuffer(0, 0, this.width, this.height, 0, 0, this.width, this.height, GL_COLOR_BUFFER_BIT, GL_NEAREST);

		this.gl.glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
		this.gl.glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
		this.gl.glBindFramebuffer(GL_FRAMEBUFFER, destFBO);
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
	
	// Drawing paths
	public void beginPath() {
		this.state().path.clear();
	}
	
	public void fill() {
		VGPath path = this.state().path.getPath();
		
		// Clear the stencil buffer
		this.gl.glEnable(GL_STENCIL_TEST);
		this.gl.glClear(GL_STENCIL_BUFFER_BIT);
		
		// Render to stencil
		this.stencilInvertMode();
		for(int i = 0; i < path.subPaths.size(); i++) {
			// Draw each sub paths separately
			this.loadSubPathVertices(path.subPaths.get(i));
			this.triangleFan();
		}
		this.normalMode();
		
		// Fill
		this.stencilFill(this.state().fillStyle);
		
		this.gl.glDisable(GL_STENCIL_TEST);
	}
	
	public void stroke() {
		VGPath trace = this.state().path.getPath().trace(this.state().lineWidth, this.state().lineCaps, this.state().lineJoins);

		// Clear the stencil buffer
		this.gl.glEnable(GL_STENCIL_TEST);
		this.gl.glClear(GL_STENCIL_BUFFER_BIT);
		
		this.stencilReplaceMode();
		for(int i = 0; i < trace.subPaths.size(); i++) {
			// Draw each sub paths separately
			this.loadSubPathVertices(trace.subPaths.get(i));
			this.triangleStrip();
		}
		this.normalMode();
		
		// Fill
		this.stencilFill(this.state().strokeStyle);
		
		this.gl.glDisable(GL_STENCIL_TEST);
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
	private void loadSubPathVertices(VGSubPath path) {
		this.nverts = path.points.size();
		
		int bufSize = this.nverts * 2;
		if(this.vertBuffer.capacity() < bufSize) {
			// Allocate more spACE
			this.vertBuffer = FloatBuffer.allocate(bufSize);
		}
		
		this.vertBuffer.clear();
		for(int i = 0; i < path.points.size(); i++) {
			VGPoint p = path.points.get(i);
			
			this.vertBuffer.put(p.x);
			this.vertBuffer.put(p.y);
		}
		
		this.vertBuffer.flip();
		
		this.gl.glBindBuffer(GL_ARRAY_BUFFER, this.gl_vbo_verts);
		this.gl.glBufferData(GL_ARRAY_BUFFER, this.vertBuffer.remaining() << 4, this.vertBuffer, GL_STREAM_DRAW);
	}
	
	// Used for normal polygons
	private void stencilInvertMode() {
		this.gl.glColorMask(false, false, false, false);
		this.gl.glStencilMask(0xFF);
		this.gl.glStencilFunc(GL_ALWAYS, 1, 0xFF);
		this.gl.glStencilOp(GL_INVERT, GL_INVERT, GL_INVERT);
		
		this.gl.glUseProgram(this.gl_prog_stencil);
		this.gl.glUniform2f(this.gl_prog_stencil_vec2_surfaceSize, this.width, this.height);
		
		this.gl.glBindVertexArray(this.gl_vao_path);
	}
	
	private void stencilReplaceMode() {
		this.gl.glColorMask(false, false, false, false);
		this.gl.glStencilMask(0xFF);
		this.gl.glStencilFunc(GL_ALWAYS, 1, 0xFF);
		this.gl.glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);
		
		this.gl.glUseProgram(this.gl_prog_stencil);
		this.gl.glUniform2f(this.gl_prog_stencil_vec2_surfaceSize, this.width, this.height);
		
		this.gl.glBindVertexArray(this.gl_vao_path);
	}
	
	private void normalMode() {
		// Restore state
		this.gl.glColorMask(true, true, true, true);
		this.gl.glStencilMask(0xFF);
		this.gl.glStencilFunc(GL_NOTEQUAL, 0, 0xFF);
		this.gl.glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
	}
	
	private void triangleFan() {
		this.gl.glDrawArrays(GL_TRIANGLE_FAN, 0, this.nverts);
	}
	
	private void triangleStrip() {
		this.gl.glDrawArrays(GL_TRIANGLE_STRIP, 0, this.nverts);
	}
	
	private void stencilFill(RGBA32 rgba) {
		// -- Filling pass
		this.gl.glUseProgram(this.gl_prog_fill);
		this.gl.glUniform4f(this.gl_prog_fill_vec4_fillRGBA, rgba.getRedf(), rgba.getGreenf(), rgba.getBluef(), rgba.getAlphaf());
		
		this.gl.glBindVertexArray(this.gl_vao_quad);
		this.gl.glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
		
		if(GLVG.DEBUG) {
			this.gl.glDisable(GL_STENCIL_TEST);
			
			this.gl.glUseProgram(this.gl_prog_stencil);
			this.gl.glUniform2f(this.gl_prog_stencil_vec2_surfaceSize, this.width, this.height);

			this.gl.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
			
			this.gl.glBindVertexArray(this.gl_vao_path);
			this.gl.glDrawArrays(GL_TRIANGLE_STRIP, 0, this.nverts);
			
			this.gl.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
			
			this.gl.glEnable(GL_STENCIL_TEST);
			return;
		}
	}
	
	static int getArcSteps(float radius) {
		// I have no idea how that works but this thing magically gives us how many steps are needed in a full circle for it to have edges of approximately ARC_EDGE_SIZE
		return Math.max((int) Math.round(MathUtils.PI_2 / Math.acos(-(GLVG.ARC_EDGE_SIZE * GLVG.ARC_EDGE_SIZE) / (2 * radius * radius) + 1)), 4);
	}
}
