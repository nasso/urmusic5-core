package io.github.nasso.urmusic.model.renderer.video;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2ES2.*;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import org.joml.Matrix4f;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.glsl.ShaderUtil;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import io.github.nasso.urmusic.common.DataUtils;
import io.github.nasso.urmusic.common.parsing.Token;
import io.github.nasso.urmusic.common.parsing.Tokenizer;

public class GLUtils {
	public static final boolean DEBUG = true;
	
	private static enum GLSLPreprocessorToken {
		PRE_INCLUDE,
		PRE_INCLUDE_LOCAL_PATH,
		PRE_INCLUDE_GLOBAL_PATH
	}
	
	private static final Tokenizer<GLSLPreprocessorToken> glslTokenizer = new Tokenizer<>();
	private static final String[] GLSL_FILE_EXTENSIONS = {
		"frag",
		"fs",
		"fshader",
		
		"geom",
		"gl",
		"glsl",
		"gs",
		"gshader",
		
		"vert",
		"vs",
		"vshader",
	};
	
	static {
		GLUtils.glslTokenizer.ignore("\\s");
		
		GLUtils.glslTokenizer.addToken("#include", GLSLPreprocessorToken.PRE_INCLUDE);
		GLUtils.glslTokenizer.addToken("<.*>", GLSLPreprocessorToken.PRE_INCLUDE_GLOBAL_PATH);
		GLUtils.glslTokenizer.addToken("\".*\"", GLSLPreprocessorToken.PRE_INCLUDE_LOCAL_PATH);
	}
	
	private final IntBuffer buf1a = Buffers.newDirectIntBuffer(1);
	private final IntBuffer buf1b = Buffers.newDirectIntBuffer(1);
	
	private FloatBuffer _mat4Buf = Buffers.newDirectFloatBuffer(4 * 4);
	
	private final TIntList buffers = new TIntArrayList();
	private final TIntList vaos = new TIntArrayList();
	private final TIntList textures = new TIntArrayList();
	private final TIntList renderbuffers = new TIntArrayList();
	private final TIntList framebuffers = new TIntArrayList();
	private final TIntList programs = new TIntArrayList();
	
	private final String name;
	
	public GLUtils(String name) {
		this.name = name;
	}
	
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
	
	public final int genRenderbuffer(GL3 gl) {
		this.genRenderbuffers(gl, 1, this.buf1a);
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
	
	public final void genRenderbuffers(GL3 gl, int count, IntBuffer dest) {
		gl.glGenRenderbuffers(count, dest);
		for(int i = 0; i < count; i++) this.renderbuffers.add(dest.get(i));
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
	
	private final void readGLSLSourceLines(String wdir, String shaderPath, StringBuilder lines) throws IOException {
		String filePath = wdir + shaderPath;
		
		InputStream in = DataUtils.getFileInputStream(filePath.toString(), true);
		
		// If it's still null, try to guess the missing extension
		for(int i = 0; i < GLUtils.GLSL_FILE_EXTENSIONS.length && in == null; i++)
			in = DataUtils.getFileInputStream(filePath.toString() + "." + GLUtils.GLSL_FILE_EXTENSIONS[i], true);
		
		// If we couldn't guess the extension, welp rip m8 guess ur file doesn't exist or u have some weird extension
		if(in == null) {
			System.err.println("Can't find shader source: " + filePath);
			return;
		}
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		
		String line;
		while((line = reader.readLine()) != null) {
			if(line.startsWith("#include ")) {
				List<Token<GLSLPreprocessorToken>> tokens = GLUtils.glslTokenizer.tokenize(line);
				
				if(tokens.size() >= 2 && tokens.get(0).getType() == GLSLPreprocessorToken.PRE_INCLUDE) {
					Token<GLSLPreprocessorToken> val = tokens.get(1);
					
					String dir = wdir;
					if(val.getType() == GLSLPreprocessorToken.PRE_INCLUDE_GLOBAL_PATH) dir = "res/shaders/gl3/include/";
					
					String path = val.getValue();
					path = path.substring(1, path.length() - 1);
					
					this.readGLSLSourceLines(dir, path, lines);
					continue;
				}
			}
			
			lines.append(line).append('\n');
		}
		
		reader.close();
		in.close();
	}
	
	public final String loadGLSLSource(String wdir, String shaderName) throws IOException {
		StringBuilder builder = new StringBuilder();
		
		this.readGLSLSourceLines("res/shaders/gl3/" + wdir, shaderName, builder);
		
		return builder.toString();
	}
	
	public final int createProgram(GL3 gl, String vsName, String fsName) {
		return this.createProgram(gl, "", vsName, fsName);
	}
	
	/**
	 * @param gl
	 * @param dir Sub-directory where the shaders are. <strong>Must end with a slash ("<tt>/</tt>")</strong>.
	 * @param vsName
	 * @param fsName
	 * @return
	 */
	public final int createProgram(GL3 gl, String dir, String vsName, String fsName) {
		int vs = gl.glCreateShader(GL_VERTEX_SHADER);
		int fs = gl.glCreateShader(GL_FRAGMENT_SHADER);
		
		try {
			gl.glShaderSource(vs, 1, new String[] {
				this.loadGLSLSource(dir, vsName)
			}, null);
			
			gl.glShaderSource(fs, 1, new String[] {
				this.loadGLSLSource(dir, fsName)
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
		
		gl.glGetProgramiv(progID, GL_LINK_STATUS, this.buf1a);
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
		
		this.dumpError(gl, "createProgram");
		
		return progID;
	}
	
	public final void uniformTexture(GL3 gl, int loc, int texID, int texUnit) {
		gl.glUniform1i(loc, texUnit + 1);
		gl.glActiveTexture(GL_TEXTURE0 + texUnit + 1);
		gl.glBindTexture(GL_TEXTURE_2D, texID);
		gl.glActiveTexture(GL_TEXTURE0);
	}
	
	public final void uniformMatrix(GL3 gl, int loc, Matrix4f mat4) {
		mat4.get(this._mat4Buf);
		gl.glUniformMatrix4fv(loc, this._mat4Buf.remaining() >> 4, false, this._mat4Buf);
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
	
	// Misc utils
	public final void dumpError(GL3 gl, String tag) {
		if(!DEBUG) return;
		
		int err;
		while((err = gl.glGetError()) != GL_NO_ERROR) {
			switch(err) {
				case GL_INVALID_ENUM:
					System.out.println("opengl error for '" + this.name + "' at '" + tag + "': GL_INVALID_ENUM");
					break;
				case GL_INVALID_VALUE:
					System.out.println("opengl error for '" + this.name + "' at '" + tag + "': GL_INVALID_VALUE");
					break;
				case GL_INVALID_OPERATION:
					System.out.println("opengl error for '" + this.name + "' at '" + tag + "': GL_INVALID_OPERATION");
					break;
				case GL_INVALID_FRAMEBUFFER_OPERATION:
					System.out.println("opengl error for '" + this.name + "' at '" + tag + "': GL_INVALID_FRAMEBUFFER_OPERATION");
					break;
				case GL_OUT_OF_MEMORY:
					System.out.println("opengl error for '" + this.name + "' at '" + tag + "': GL_OUT_OF_MEMORY");
					break;
			}
		}
	}
	
	/**
	 * Loads an image to a gl texture, optionnaly generating mip maps at the end.
	 */
	public final void loadImageToTexture(GL3 gl, int tex, BufferedImage img, boolean genMipmaps) {
		int width = img.getWidth();
		int height = img.getHeight();
		
		ByteBuffer data = Buffers.newDirectByteBuffer(width * height * 4);

		for(int i = 0; i < width * height; i++) {
			int rgb = img.getRGB(i % width, i / width);
			
			data.put((byte) ((rgb >> 16) & 0xFF));
			data.put((byte) ((rgb >> 8 ) & 0xFF));
			data.put((byte) ((rgb >> 0 ) & 0xFF));
			data.put((byte) ((rgb >> 24) & 0xFF));
		}
		
		data.flip();
		
		gl.glBindTexture(GL_TEXTURE_2D, tex);
		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, data);
		if(genMipmaps) gl.glGenerateMipmap(GL_TEXTURE_2D);
	}
	
	/**
	 * Generates a VAO containing 1 VBO of 2D positions for a full screen quad. The vertices are ordered so a render can be done simply by doing:
	 * <pre>
	 *     gl.glBindVertexArray(this.quadVAO);
	 *     gl.glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);</pre>
	 * The simplest vertex shader you can have with this would be:
	 * <pre>
	 *     #version 330 core
	 *     
	 *     in vec2 position_quad;
	 *     
	 *     void main() {
	 *         gl_Position = vec4(position_quad, 0.0, 1.0);
	 *     }</pre>
	 * The generated VAO and VBO are left bound (to <code>GL_ARRAY_BUFFER</code> for the VBO).
	 * 
	 * @param gl
	 * @return The VAO
	 */
	public final int createFullQuadVAO(GL3 gl) {
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
