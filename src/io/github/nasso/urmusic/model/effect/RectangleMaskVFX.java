package io.github.nasso.urmusic.model.effect;

import org.joml.Vector4fc;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import io.github.nasso.urmusic.common.BoolValue;
import io.github.nasso.urmusic.common.RGBA32;
import io.github.nasso.urmusic.model.project.TrackEffect;
import io.github.nasso.urmusic.model.project.VideoEffect;
import io.github.nasso.urmusic.model.project.VideoEffectArgs;
import io.github.nasso.urmusic.model.project.VideoEffectInstance;
import io.github.nasso.urmusic.model.project.param.BooleanParam;
import io.github.nasso.urmusic.model.project.param.BoundsParam;
import io.github.nasso.urmusic.model.project.param.OptionParam;
import io.github.nasso.urmusic.model.project.param.RGBA32Param;
import io.github.nasso.urmusic.model.renderer.video.NGLUtils;

public class RectangleMaskVFX extends TrackEffect implements VideoEffect {
	private NGLUtils glu = new NGLUtils("rectangle mask global");
	
	private int prog, quadVAO;
	private int loc_inputTex, loc_size, loc_color, loc_points, loc_blending, loc_invert;
	
	public class RectangleMaskVFXInstance extends TrackEffectInstance implements VideoEffectInstance  {
		private RGBA32Param color = new RGBA32Param("color", 0xffffffff);
		private BoundsParam bounds = new BoundsParam("bounds", -50, -50, 100, 100);
		private OptionParam blendingMode = new OptionParam("blendingMode", 0,
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
		private BooleanParam invert = new BooleanParam("invert", BoolValue.FALSE);
		
		public RectangleMaskVFXInstance() {
			this.addParameter(this.color);
			this.addParameter(this.bounds);
			this.addParameter(this.blendingMode);
			this.addParameter(this.invert);
		}
		
		public void setupVideo(GL3 gl) {
		}
		
		public void applyVideo(GL3 gl, VideoEffectArgs args) {
			RGBA32 color = this.color.getValue(args.time);
			Vector4fc bounds = this.bounds.getValue(args.time);
			int blending = this.blendingMode.getValue(args.time);
			BoolValue invert = this.invert.getValue(args.time);
			
			gl.glUseProgram(RectangleMaskVFX.this.prog);
			RectangleMaskVFX.this.glu.uniformTexture(gl, RectangleMaskVFX.this.loc_inputTex, args.texInput, 0);
			
			gl.glUniform2f(RectangleMaskVFX.this.loc_size, args.width, args.height);
			gl.glUniform4f(RectangleMaskVFX.this.loc_color, color.getRedf(), color.getGreenf(), color.getBluef(), color.getAlphaf());
			gl.glUniform4f(RectangleMaskVFX.this.loc_points,
					bounds.x(),
					-bounds.y(),
					bounds.x() + bounds.z(),
					-bounds.y() - bounds.w()
			);
			gl.glUniform1i(RectangleMaskVFX.this.loc_blending, blending);
			gl.glUniform1i(RectangleMaskVFX.this.loc_invert, invert == BoolValue.TRUE ? 1 : 0);
			
			gl.glBindVertexArray(RectangleMaskVFX.this.quadVAO);
			gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, 4);
		}
		
		public void disposeVideo(GL3 gl) {
		}
	}
	
	public TrackEffectInstance instance() {
		return new RectangleMaskVFXInstance();
	}
	
	public void globalVideoSetup(GL3 gl) {
		this.prog = this.glu.createProgram(gl, "fx/rectangle_mask/", "main_vert.vs", "main_frag.fs");
		
		this.loc_inputTex = gl.glGetUniformLocation(this.prog, "inputTex");
		this.loc_size = gl.glGetUniformLocation(this.prog, "colorSize");
		
		this.loc_color = gl.glGetUniformLocation(this.prog, "params.color");
		this.loc_points = gl.glGetUniformLocation(this.prog, "params.points");
		this.loc_blending = gl.glGetUniformLocation(this.prog, "params.blending");
		this.loc_invert = gl.glGetUniformLocation(this.prog, "params.invert");
		
		this.quadVAO = this.glu.createFullQuadVAO(gl);
	}
	
	public void globalVideoDispose(GL3 gl) {
		this.glu.dispose(gl);
	}
	
	public void effectMain() {
	}
	
	public String getEffectClassName() {
		return "rectangle_mask";
	}
}
