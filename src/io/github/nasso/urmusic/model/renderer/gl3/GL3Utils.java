package io.github.nasso.urmusic.model.renderer.gl3;


import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2ES2.*;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.glsl.ShaderUtil;

import io.github.nasso.urmusic.utils.DataUtils;

public class GL3Utils {
	private static final IntBuffer buf1a = IntBuffer.allocate(1);
	private static final IntBuffer buf1b = IntBuffer.allocate(1);
	
	private static final List<Integer> buffers = new ArrayList<Integer>();
	private static final List<Integer> vaos = new ArrayList<Integer>();
	private static final List<Integer> textures = new ArrayList<Integer>();
	private static final List<Integer> framebuffers = new ArrayList<Integer>();
	
	public static final int genBuffer(GL3 gl) {
		genBuffers(gl, 1, buf1a);
		return buf1a.get(0);
	}
	
	public static final int genVertexArray(GL3 gl) {
		genVertexArrays(gl, 1, buf1a);
		return buf1a.get(0);
	}
	
	public static final int genTexture(GL3 gl) {
		genTextures(gl, 1, buf1a);
		return buf1a.get(0);
	}
	
	public static final int genFramebuffer(GL3 gl) {
		genFramebuffers(gl, 1, buf1a);
		return buf1a.get(0);
	}
	
	public static final void genBuffers(GL3 gl, int count, IntBuffer dest) {
		gl.glGenBuffers(count, dest);
		for(int i = 0; i < count; i++) buffers.add(dest.get(i));
	}
	
	public static final void genVertexArrays(GL3 gl, int count, IntBuffer dest) {
		gl.glGenVertexArrays(count, dest);
		for(int i = 0; i < count; i++) vaos.add(dest.get(i));
	}
	
	public static final void genTextures(GL3 gl, int count, IntBuffer dest) {
		gl.glGenTextures(count, dest);
		for(int i = 0; i < count; i++) textures.add(dest.get(i));
	}
	
	public static final void genFramebuffers(GL3 gl, int count, IntBuffer dest) {
		gl.glGenFramebuffers(count, dest);
		for(int i = 0; i < count; i++) framebuffers.add(dest.get(i));
	}
	
	public static final int createFramebuffer(GL3 gl, int width, int height) {
		createFramebuffers(gl, 1, width, height, buf1a, buf1b);
		
		return buf1b.get(0);
	}
	
	public static final void createFramebuffers(GL3 gl, int count, int width, int height, IntBuffer bufTex, IntBuffer bufFBO) {
		genTextures(gl, count, bufTex);
		genFramebuffers(gl, count, bufFBO);
		for(int i = 0; i < count; i++) {
			int tex = bufTex.get(i);
			int fbo = bufFBO.get(i);
			
			gl.glBindTexture(GL_TEXTURE_2D, tex);
			gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, null);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
			
			gl.glBindFramebuffer(GL_FRAMEBUFFER, fbo);
			gl.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, tex, 0);
		}
		
		gl.glBindTexture(GL_TEXTURE_2D, 0);
		gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}
	
	public static final int createProgram(GL3 gl, String vsName, String fsName) {
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
		
		gl.glGetShaderiv(vs, GL_COMPILE_STATUS, buf1a);
		if(buf1a.get(0) == GL_FALSE) {
			System.err.println("Compilation error in " + vsName + ":\n" + ShaderUtil.getShaderInfoLog(gl, vs));
			gl.glDeleteShader(vs);
			gl.glDeleteShader(fs);
			return 0;
		}
		
		gl.glGetShaderiv(fs, GL_COMPILE_STATUS, buf1a);
		if(buf1a.get(0) == GL_FALSE) {
			System.err.println("Compilation error in " + fsName + ":\n" + ShaderUtil.getShaderInfoLog(gl, fs));
			gl.glDeleteShader(vs);
			gl.glDeleteShader(fs);
			return 0;
		}
		
		int progID = gl.glCreateProgram();
		gl.glAttachShader(progID, vs);
		gl.glAttachShader(progID, fs);
		gl.glLinkProgram(progID);

		gl.glGetProgramiv(vs, GL_COMPILE_STATUS, buf1a);
		if(buf1a.get(0) == GL_FALSE) {
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
	
	public static final void uniformTexture(GL3 gl, int loc, int texID, int texUnit) {
		gl.glUniform1i(loc, texUnit + 1);
		gl.glActiveTexture(GL_TEXTURE0 + texUnit + 1);
		gl.glBindTexture(GL_TEXTURE_2D, texID);
		gl.glActiveTexture(GL_TEXTURE0);
	}
	
	public static final void dispose(GL3 gl) {
		int maxSize = 0;
		if(maxSize < buffers.size()) maxSize = buffers.size();
		if(maxSize < vaos.size()) maxSize = vaos.size();
		if(maxSize < textures.size()) maxSize = textures.size();
		if(maxSize < framebuffers.size()) maxSize = framebuffers.size();
		
		IntBuffer buf = IntBuffer.allocate(maxSize);
		
		buffers.forEach((i) -> buf.put(i));
		buf.flip();
		gl.glDeleteBuffers(buffers.size(), buf);
		
		buf.limit(buf.capacity());
		
		vaos.forEach((i) -> buf.put(i));
		buf.flip();
		gl.glDeleteVertexArrays(vaos.size(), buf);
		
		buf.limit(buf.capacity());
		
		textures.forEach((i) -> buf.put(i));
		buf.flip();
		gl.glDeleteTextures(textures.size(), buf);

		buf.limit(buf.capacity());
		
		framebuffers.forEach((i) -> buf.put(i));
		buf.flip();
		gl.glDeleteFramebuffers(framebuffers.size(), buf);
	}
	
	private GL3Utils() { }
}
