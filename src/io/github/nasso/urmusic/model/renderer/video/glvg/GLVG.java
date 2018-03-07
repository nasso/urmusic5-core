package io.github.nasso.urmusic.model.renderer.video.glvg;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2ES2.*;
import static com.jogamp.opengl.GL2ES3.*;
import static com.jogamp.opengl.GL2GL3.*;

import java.nio.FloatBuffer;
import java.util.ArrayDeque;
import java.util.Deque;

import org.joml.Matrix3f;

import com.jogamp.opengl.GL3;

import io.github.nasso.urmusic.common.MathUtils;
import io.github.nasso.urmusic.common.RGBA32;
import io.github.nasso.urmusic.model.renderer.video.NGLUtils;

/**
 * nasso is coding like the W3C again...
 * https://www.w3.org/TR/2dcontext
 * 
 * @author nasso
 */
public class GLVG implements VGPathMethods {
	private static final boolean DEBUG = false;
	private static final int MSAA_SAMPLES = 16;
	private static final float ARC_EDGE_SIZE = 1;
	private static final int DEFAULT_BUFFER_SIZE = 32;
	
	// Buffers
	private FloatBuffer vertBuffer = FloatBuffer.allocate(GLVG.DEFAULT_BUFFER_SIZE * 32);
	private int nverts;
	
	// Context
	private NGLUtils glu = new NGLUtils("glvg");
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
	private int gl_prog_stencil_mat3_xform;
	private int gl_prog_stencil_vec2_surfaceSize;
	private int gl_prog_fill;
	private int gl_prog_fill_vec4_rectXYWH;
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
		
		this.gl_prog_stencil_mat3_xform = gl.glGetUniformLocation(this.gl_prog_stencil, "xform");
		this.gl_prog_stencil_vec2_surfaceSize = gl.glGetUniformLocation(this.gl_prog_stencil, "surfaceSize");
		
		this.gl_prog_fill_vec4_rectXYWH = gl.glGetUniformLocation(this.gl_prog_fill, "rectXYWH");
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

		this.gl.glEnable(GL_STENCIL_TEST);
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
	}
	
	public void end(int destFBO) {
		this.gl.glDisable(GL_STENCIL_TEST);
		
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
	
	// Transformations
	public void scale(float x, float y) {
		this.state().transform.scale(x, y, 1);
	}
	
	public void rotate(float angle) {
		this.state().transform.rotateZ(angle);
	}
	
	public void translate(float x, float y) {
		this.transform(1, 0, 0, 1, x, y); // ree joml why you no have dat
	}
	
	public void transform(float a, float b, float c, float d, float e, float f) {
		Matrix3f xform = this.state().transform;
		
		// We're not creating a matrix object just for that smh
		// Let's calculate the transformation ourselves
        float nm00 = xform.m00 * a + xform.m10 * b;
        float nm01 = xform.m01 * a + xform.m11 * b;
        float nm02 = xform.m02 * a + xform.m12 * b;
        float nm10 = xform.m00 * c + xform.m10 * d;
        float nm11 = xform.m01 * c + xform.m11 * d;
        float nm12 = xform.m02 * c + xform.m12 * d;
        float nm20 = xform.m00 * e + xform.m10 * f + xform.m20;
        float nm21 = xform.m01 * e + xform.m11 * f + xform.m21;
        float nm22 = xform.m02 * e + xform.m12 * f + xform.m22;
        xform.m00 = nm00;
        xform.m01 = nm01;
        xform.m02 = nm02;
        xform.m10 = nm10;
        xform.m11 = nm11;
        xform.m12 = nm12;
        xform.m20 = nm20;
        xform.m21 = nm21;
        xform.m22 = nm22;
	}
	
	public void setTransform(float a, float b, float c, float d, float e, float f) {
		Matrix3f xform = this.state().transform;
		xform.m00 = a;
		xform.m01 = c;
		xform.m02 = e;
		xform.m10 = b;
		xform.m11 = d;
		xform.m12 = f;
		xform.m20 = 0;
		xform.m21 = 0;
		xform.m22 = 1;
	}
	
	// Rectangles
	public void clearRect(float x, float y, float w, float h) {
		float sx = w / this.width;
		float sy = h / this.height;
		
		this.solidRect(0, 0, 0, 0,
				x / this.width * 2.0f + sx,
				y / this.height * 2.0f + sy,
				sx, sy);
	}
	
	// Drawing paths
	public void beginPath() {
		this.state().path.clear();
	}
	
	public void fill() {
		VGPath path = this.state().path.getPath();
		
		// Clear the stencil buffer
		this.gl.glClear(GL_STENCIL_BUFFER_BIT);
		
		// Render to stencil
		this.gl.glColorMask(false, false, false, false);
		this.gl.glStencilFunc(GL_ALWAYS, 1, 0xFF);
		this.gl.glStencilOpSeparate(GL_FRONT, GL_KEEP, GL_KEEP, GL_INCR_WRAP);
		this.gl.glStencilOpSeparate(GL_BACK, GL_KEEP, GL_KEEP, GL_DECR_WRAP);
		
		this.gl.glUseProgram(this.gl_prog_stencil);
		this.glu.uniformMatrix(this.gl, this.gl_prog_stencil_mat3_xform, this.state().transform);
		this.gl.glUniform2f(this.gl_prog_stencil_vec2_surfaceSize, this.width, this.height);
		
		this.gl.glBindVertexArray(this.gl_vao_path);
		
		for(int i = 0; i < path.subPaths.size(); i++) {
			// Draw each sub paths separately
			this.loadSubPathVertices(path.subPaths.get(i));
			this.gl.glDrawArrays(GL_TRIANGLE_FAN, 0, this.nverts);
		}
		
		// Fill
		this.stencilFill(this.state().fillStyle, this.state().globalAlpha);
	}
	
	public void stroke() {
		VGPath trace = this.state().path.getPath().trace(this.state().lineWidth, this.state().lineCaps, this.state().lineJoins);

		// Clear the stencil buffer
		this.gl.glColorMask(false, false, false, false);
		this.gl.glStencilFunc(GL_ALWAYS, 1, 0xFF);
		this.gl.glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
		
		this.gl.glUseProgram(this.gl_prog_stencil);
		this.glu.uniformMatrix(this.gl, this.gl_prog_stencil_mat3_xform, this.state().transform);
		this.gl.glUniform2f(this.gl_prog_stencil_vec2_surfaceSize, this.width, this.height);
		
		this.gl.glBindVertexArray(this.gl_vao_path);
		
		for(int i = 0; i < trace.subPaths.size(); i++) {
			// Draw each sub paths separately
			this.loadSubPathVertices(trace.subPaths.get(i));
			this.gl.glDrawArrays(GL_TRIANGLE_STRIP, 0, this.nverts);
		}
		
		// Fill
		this.stencilFill(this.state().strokeStyle, this.state().globalAlpha);
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
		if(path.closed) this.nverts++;
		
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
		
		if(path.closed) {
			this.vertBuffer.put(path.firstPoint().x);
			this.vertBuffer.put(path.firstPoint().y);
		}
		
		this.vertBuffer.flip();
		
		this.gl.glBindBuffer(GL_ARRAY_BUFFER, this.gl_vbo_verts);
		this.gl.glBufferData(GL_ARRAY_BUFFER, this.vertBuffer.remaining() << 4, this.vertBuffer, GL_STREAM_DRAW);
	}
	
	// -- Filling pass
	private void solidRect(float r, float g, float b, float a, float x, float y, float width, float height) {
		this.gl.glDisable(GL_STENCIL_TEST);
		
		this.gl.glUseProgram(this.gl_prog_fill);
		this.gl.glUniform4f(this.gl_prog_fill_vec4_rectXYWH, x, y, width, height);
		this.gl.glUniform4f(this.gl_prog_fill_vec4_fillRGBA, r, g, b, a);
		
		this.gl.glBindVertexArray(this.gl_vao_quad);
		this.gl.glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
		
		this.gl.glEnable(GL_STENCIL_TEST);
	}
	
	private void stencilFill(RGBA32 rgba, float alphaMult) {
		this.gl.glColorMask(true, true, true, true);
		this.gl.glStencilFunc(GL_NOTEQUAL, 0, 0xFF);
		this.gl.glStencilOp(GL_ZERO, GL_ZERO, GL_ZERO);
		
		this.gl.glEnable(GL_BLEND);
		this.gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		this.gl.glUseProgram(this.gl_prog_fill);
		this.gl.glUniform4f(this.gl_prog_fill_vec4_rectXYWH, 0, 0, 1, 1);
		this.gl.glUniform4f(this.gl_prog_fill_vec4_fillRGBA, rgba.getRedf(), rgba.getGreenf(), rgba.getBluef(), rgba.getAlphaf() * alphaMult);
		
		this.gl.glBindVertexArray(this.gl_vao_quad);
		this.gl.glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
		
		this.gl.glDisable(GL_BLEND);
		
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
