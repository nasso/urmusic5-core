package io.github.nasso.urmusic.model.effect;

import static com.jogamp.opengl.GL.*;

import java.nio.file.Path;

import org.joml.Matrix4f;

import com.jogamp.opengl.GL3;

import io.github.nasso.urmusic.model.project.TrackEffect;
import io.github.nasso.urmusic.model.project.param.FileParam;
import io.github.nasso.urmusic.model.project.param.FloatParam;
import io.github.nasso.urmusic.model.project.param.OptionParam;
import io.github.nasso.urmusic.model.renderer.EffectArgs;
import io.github.nasso.urmusic.model.renderer.GLUtils;

public class ImageDisplayVFX extends TrackEffect {
	private GLUtils glu = new GLUtils();
	
	private int prog, quadVAO;
	private int loc_xform, loc_inputTex, loc_imageTex, loc_blending, loc_opacity;
	
	public class ImageDisplayVFXInstance extends TrackEffectInstance {
		private FileParam source = new FileParam("source");
		private OptionParam blendingMode = new OptionParam("blendingMode",
			"srcOver",
			"dstOver",
			"srcIn",
			"dstIn",
			"srcOut",
			"dstOut",
			"srcAtop",
			"dstAtop",
			"copy",
			"add",
			"xor"
		);
		private FloatParam opacity = new FloatParam("opacity", 1.0f, 0.01f, 0.0f, 1.0f);
		
		private Matrix4f xform = new Matrix4f();
		private Path lastSrc = null;
		private int tex;
		
		public ImageDisplayVFXInstance() {
			this.addParameter(this.source);
			this.addParameter(this.blendingMode);
			this.addParameter(this.opacity);
		}
		
		public void setupVideo(GL3 gl) {
			this.tex = ImageDisplayVFX.this.glu.genTexture(gl);
			gl.glBindTexture(GL_TEXTURE_2D, this.tex);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		}
		
		public void applyVideo(GL3 gl, EffectArgs args) {
			Path src = this.source.getValue(args.frame);
			int blendingMode = this.blendingMode.getValue(args.frame);
			float opacity = this.opacity.getValue(args.frame);
			
			if(src == null) {
				args.cancelled = true;
				return;
			}
			
			if(!src.equals(this.lastSrc)) {
				this.lastSrc = src.toAbsolutePath();
				
				ImageDisplayVFX.this.glu.loadImageToTexture(gl, this.tex, this.lastSrc.toString(), true);
			}
			
			this.xform.identity();
			
			gl.glUseProgram(ImageDisplayVFX.this.prog);
			ImageDisplayVFX.this.glu.uniformMatrix(gl, ImageDisplayVFX.this.loc_xform, this.xform);
			ImageDisplayVFX.this.glu.uniformTexture(gl, ImageDisplayVFX.this.loc_inputTex, args.texInput, 0);
			ImageDisplayVFX.this.glu.uniformTexture(gl, ImageDisplayVFX.this.loc_imageTex, this.tex, 1);
			gl.glUniform1i(ImageDisplayVFX.this.loc_blending, blendingMode);
			gl.glUniform1f(ImageDisplayVFX.this.loc_opacity, opacity);
			
			gl.glBindVertexArray(ImageDisplayVFX.this.quadVAO);
			gl.glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
		}
		
		public void disposeVideo(GL3 gl) {
			ImageDisplayVFX.this.glu.deleteTexture(gl, this.tex);
		}
	}
	
	public TrackEffectInstance instance() {
		return new ImageDisplayVFXInstance();
	}
	
	public void globalVideoSetup(GL3 gl) {
		this.prog = this.glu.createProgram(gl, "fx/image_display/", "main_vert.vs", "main_frag.fs");
		this.loc_xform = gl.glGetUniformLocation(this.prog, "xform");
		this.loc_inputTex = gl.glGetUniformLocation(this.prog, "inputTex");
		this.loc_imageTex = gl.glGetUniformLocation(this.prog, "imageTex");
		this.loc_blending = gl.glGetUniformLocation(this.prog, "blendingMode");
		this.loc_opacity = gl.glGetUniformLocation(this.prog, "opacity");
		
		this.quadVAO = this.glu.createFullQuadVAO(gl);
	}
	
	public void globalVideoDispose(GL3 gl) {
		this.glu.dispose(gl);
	}
	
	public void effectMain() {
		this.enableVideoEffect();
	}
	
	public String getEffectClassName() {
		return "image_display";
	}
}
