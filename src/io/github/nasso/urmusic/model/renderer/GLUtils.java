package io.github.nasso.urmusic.model.renderer;


import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2ES2.*;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.glsl.ShaderUtil;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import io.github.nasso.urmusic.utils.DataUtils;

public class GLUtils {
	private final IntBuffer buf1a = IntBuffer.allocate(1);
	private final IntBuffer buf1b = IntBuffer.allocate(1);
	
	private final TIntList buffers = new TIntArrayList();
	private final TIntList vaos = new TIntArrayList();
	private final TIntList textures = new TIntArrayList();
	private final TIntList framebuffers = new TIntArrayList();
	private final TIntList programs = new TIntArrayList();
	
	public final int genBuffer(GL3 gl) {
		this.genBuffers(gl, 1, this.buf1a);
		return this.buf1a.get(0);
	}
	
	public final int genVertexArray(GL3 gl) {
		this.genVertexArrays(gl, 1, this.buf1a);
		return this.buf1a.get(0);
	}
	
	public final int genTexture(GL3 gl) {
		this.genTextures(gl, 1, this.buf1a);
		return this.buf1a.get(0);
	}
	
	public final int genFramebuffer(GL3 gl) {
		this.genFramebuffers(gl, 1, this.buf1a);
		return this.buf1a.get(0);
	}
	
	public final void genBuffers(GL3 gl, int count, IntBuffer dest) {
		gl.glGenBuffers(count, dest);
		for(int i = 0; i < count; i++) this.buffers.add(dest.get(i));
	}
	
	public final void genVertexArrays(GL3 gl, int count, IntBuffer dest) {
		gl.glGenVertexArrays(count, dest);
		for(int i = 0; i < count; i++) this.vaos.add(dest.get(i));
	}
	
	public final void genTextures(GL3 gl, int count, IntBuffer dest) {
		gl.glGenTextures(count, dest);
		for(int i = 0; i < count; i++) this.textures.add(dest.get(i));
	}
	
	public final void genFramebuffers(GL3 gl, int count, IntBuffer dest) {
		gl.glGenFramebuffers(count, dest);
		for(int i = 0; i < count; i++) this.framebuffers.add(dest.get(i));
	}
	
	public final int createFramebuffer(GL3 gl, int width, int height) {
		this.createFramebuffers(gl, 1, width, height, this.buf1a, this.buf1b);
		
		return this.buf1b.get(0);
	}
	
	public final void createFramebuffers(GL3 gl, int count, int width, int height, IntBuffer bufTex, IntBuffer bufFBO) {
		this.genTextures(gl, count, bufTex);
		this.genFramebuffers(gl, count, bufFBO);
		for(int i = 0; i < count; i++) {
			int tex = bufTex.get(i);
			int fbo = bufFBO.get(i);
			
			gl.glBindTexture(GL_TEXTURE_2D, tex);
			gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
			
			gl.glBindFramebuffer(GL_FRAMEBUFFER, fbo);
			gl.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, tex, 0);
		}
		
		gl.glBindTexture(GL_TEXTURE_2D, 0);
		gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}
	
	public final int createProgram(GL3 gl, String vsName, String fsName) {
		int vs = gl.glCreateShader(GL_VERTEX_SHADER);
		int fs = gl.glCreateShader(GL_FRAGMENT_SHADER);
		
		try {
			gl.glShaderSource(vs, 1, new String[] {
				DataUtils.readFile("res/shaders/gl3/" + vsName, true)
			}, null);
			
			gl.glShaderSource(fs, 1, new String[] {
				DataUtils.readFile("res/shaders/gl3/" + fsName, true)
			}, null);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		gl.glCompileShader(vs);
		gl.glCompileShader(fs);
		
		gl.glGetShaderiv(vs, GL_COMPILE_STATUS, this.buf1a);
		if(this.buf1a.get(0) == GL_FALSE) {
			System.err.println("Compilation error in " + vsName + ":\n" + ShaderUtil.getShaderInfoLog(gl, vs));
			gl.glDeleteShader(vs);
			gl.glDeleteShader(fs);
			return 0;
		}
		
		gl.glGetShaderiv(fs, GL_COMPILE_STATUS, this.buf1a);
		if(this.buf1a.get(0) == GL_FALSE) {
			System.err.println("Compilation error in " + fsName + ":\n" + ShaderUtil.getShaderInfoLog(gl, fs));
			gl.glDeleteShader(vs);
			gl.glDeleteShader(fs);
			return 0;
		}
		
		int progID = gl.glCreateProgram();
		gl.glAttachShader(progID, vs);
		gl.glAttachShader(progID, fs);
		gl.glLinkProgram(progID);

		gl.glGetProgramiv(vs, GL_COMPILE_STATUS, this.buf1a);
		if(this.buf1a.get(0) == GL_FALSE) {
			System.err.println(ShaderUtil.getProgramInfoLog(gl, progID));
			gl.glDeleteProgram(progID);
			gl.glDeleteShader(vs);
			gl.glDeleteShader(fs);
			return 0;
		}
		
		gl.glDetachShader(progID, vs);
		gl.glDetachShader(progID, fs);
		
		gl.glDeleteShader(vs);
		gl.glDeleteShader(fs);
		
		return progID;
	}
	
	public final void uniformTexture(GL3 gl, int loc, int texID, int texUnit) {
		gl.glUniform1i(loc, texUnit + 1);
		gl.glActiveTexture(GL_TEXTURE0 + texUnit + 1);
		gl.glBindTexture(GL_TEXTURE_2D, texID);
		gl.glActiveTexture(GL_TEXTURE0);
	}
	
	public final void deleteBuffer(GL3 gl, int buf) {
		if(!this.buffers.contains(buf)) return;
		
		this.buf1a.put(0, buf);
		gl.glDeleteBuffers(0, this.buf1a);
		
		this.buffers.remove(this.buffers.indexOf(buf));
	}
	
	public final void deleteVAO(GL3 gl, int vao) {
		if(!this.vaos.contains(vao)) return;
		
		this.buf1a.put(0, vao);
		gl.glDeleteVertexArrays(0, this.buf1a);
		
		this.vaos.remove(this.vaos.indexOf(vao));
	}
	
	public final void deleteTexture(GL3 gl, int tex) {
		if(!this.textures.contains(tex)) return;
		
		this.buf1a.put(0, tex);
		gl.glDeleteTextures(0, this.buf1a);
		
		this.textures.remove(this.textures.indexOf(tex));
	}
	
	public final void deleteFramebuffer(GL3 gl, int fbo) {
		if(!this.framebuffers.contains(fbo)) return;
		
		this.buf1a.put(0, fbo);
		gl.glDeleteFramebuffers(0, this.buf1a);
		
		this.framebuffers.remove(this.framebuffers.indexOf(fbo));
	}
	
	public final void deleteProgram(GL3 gl, int prog) {
		if(!this.programs.contains(prog)) return;
		
		gl.glDeleteProgram(prog);
		
		this.programs.remove(this.programs.indexOf(prog));
	}
	
	public final void dispose(GL3 gl) {
		int maxSize = 0;
		if(maxSize < this.buffers.size()) maxSize = this.buffers.size();
		if(maxSize < this.vaos.size()) maxSize = this.vaos.size();
		if(maxSize < this.textures.size()) maxSize = this.textures.size();
		if(maxSize < this.framebuffers.size()) maxSize = this.framebuffers.size();
		
		IntBuffer buf = IntBuffer.allocate(maxSize);
		
		this.buffers.forEach((i) -> {
			buf.put(i);
			return true;
		});
		buf.flip();
		gl.glDeleteBuffers(this.buffers.size(), buf);
		
		buf.limit(buf.capacity());
		
		this.vaos.forEach((i) -> {
			buf.put(i);
			return true;
		});
		buf.flip();
		gl.glDeleteVertexArrays(this.vaos.size(), buf);
		
		buf.limit(buf.capacity());
		
		this.textures.forEach((i) -> {
			buf.put(i);
			return true;
		});
		buf.flip();
		gl.glDeleteTextures(this.textures.size(), buf);

		buf.limit(buf.capacity());
		
		this.framebuffers.forEach((i) -> {
			buf.put(i);
			return true;
		});
		buf.flip();
		gl.glDeleteFramebuffers(this.framebuffers.size(), buf);
		
		this.programs.forEach((i) -> {
			gl.glDeleteProgram(i);
			return true;
		});
		
		this.buffers.clear();
		this.vaos.clear();
		this.textures.clear();
		this.framebuffers.clear();
	}
	
	public GLUtils() { }
	
	// Misc utils
	
	/**
	 * Generates a VAO containing 1 VBO of 2D positions for a full screen quad. The vertices are ordered so a render can be done simply by doing:
	 * <pre>
	 *     gl.glBindVertexArray(this.quadVAO);
	 *     gl.glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
	 * </pre>
	 * The simplest vertex shader you can have with this would be:
	 * <pre>
	 *     #version 330 core
	 *     
	 *     in vec2 position_quad;
	 *     
	 *     void main() {
	 *         gl_Position = vec4(position_quad, 0.0, 1.0);
	 *     }
	 * </pre>
	 * The generated VAO and VBO are left bound (to <code>GL_ARRAY_BUFFER</code> for the VBO).
	 * 
	 * @param gl
	 * @return The VAO
	 */
	public final int genFullQuadVAO(GL3 gl) {
		int quadPos = this.genBuffer(gl);
		gl.glBindBuffer(GL_ARRAY_BUFFER, quadPos);
		gl.glBufferData(GL_ARRAY_BUFFER, 8 * 32, FloatBuffer.wrap(new float[] {
				-1, -1,
				1, -1,
				-1, 1,
				1, 1
		}), GL_STATIC_DRAW);
		
		int quadVAO = this.genVertexArray(gl);
		gl.glBindVertexArray(quadVAO);
		gl.glEnableVertexAttribArray(0);
		gl.glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);
		
		return quadVAO;
	}
}
