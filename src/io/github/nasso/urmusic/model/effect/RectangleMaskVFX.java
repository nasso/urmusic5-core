package io.github.nasso.urmusic.model.effect;

import static com.jogamp.opengl.GL.*;

import org.joml.Vector2fc;

import com.jogamp.opengl.GL3;

import io.github.nasso.urmusic.common.BoolValue;
import io.github.nasso.urmusic.common.RGBA32;
import io.github.nasso.urmusic.model.project.TrackEffect;
import io.github.nasso.urmusic.model.project.param.BooleanParam;
import io.github.nasso.urmusic.model.project.param.OptionParam;
import io.github.nasso.urmusic.model.project.param.Point2DParam;
import io.github.nasso.urmusic.model.project.param.RGBA32Param;
import io.github.nasso.urmusic.model.renderer.EffectArgs;
import io.github.nasso.urmusic.model.renderer.GLUtils;

public class RectangleMaskVFX extends TrackEffect {
	private GLUtils glu = new GLUtils();
	
	private int prog, quadVAO;
	private int loc_inputTex, loc_size, loc_color, loc_points, loc_blending, loc_invert;
	
	public class RectangleMaskVFXInstance extends TrackEffectInstance {
		private RGBA32Param color = new RGBA32Param("color", 0xffffffff);
		private Point2DParam pointA = new Point2DParam("pointA", -50.0f, -50.0f);
		private Point2DParam pointB = new Point2DParam("pointB", +50.0f, +50.0f);
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
		private BooleanParam invert = new BooleanParam("invert", BoolValue.FALSE);
		
		public RectangleMaskVFXInstance() {
			this.addParameter(this.color);
			this.addParameter(this.pointA);
			this.addParameter(this.pointB);
			this.addParameter(this.blendingMode);
			this.addParameter(this.invert);
		}
		
		public void setupVideo(GL3 gl) {
		}
		
		public void applyVideo(GL3 gl, EffectArgs args) {
			RGBA32 color = this.color.getValue(args.frame);
			Vector2fc pointA = this.pointA.getValue(args.frame);
			Vector2fc pointB = this.pointB.getValue(args.frame);
			int blending = this.blendingMode.getValue(args.frame);
			BoolValue invert = this.invert.getValue(args.frame);
			
			gl.glUseProgram(RectangleMaskVFX.this.prog);
			RectangleMaskVFX.this.glu.uniformTexture(gl, RectangleMaskVFX.this.loc_inputTex, args.texInput, 0);
			
			gl.glUniform2f(RectangleMaskVFX.this.loc_size, args.width, args.height);
			gl.glUniform4f(RectangleMaskVFX.this.loc_color, color.getRedf(), color.getGreenf(), color.getBluef(), color.getAlphaf());
			gl.glUniform4f(RectangleMaskVFX.this.loc_points,
					pointA.x(),
					-pointA.y(),
					pointB.x(),
					-pointB.y()
			);
			gl.glUniform1i(RectangleMaskVFX.this.loc_blending, blending);
			gl.glUniform1i(RectangleMaskVFX.this.loc_invert, invert == BoolValue.TRUE ? 1 : 0);
			
			gl.glBindVertexArray(RectangleMaskVFX.this.quadVAO);
			gl.glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
		}
		
		public void disposeVideo(GL3 gl) {
		}
	}
	
	public TrackEffectInstance instance() {
		return new RectangleMaskVFXInstance();
	}
	
	public void globalVideoSetup(GL3 gl) {
		this.prog = this.glu.createProgram(gl, "fx/rectangle_mask/", "vert_main.vs", "frag_main.fs");
		
		this.loc_inputTex = gl.glGetUniformLocation(this.prog, "inputTex");
		this.loc_size = gl.glGetUniformLocation(this.prog, "colorSize");
		
		this.loc_color = gl.glGetUniformLocation(this.prog, "params.color");
		this.loc_points = gl.glGetUniformLocation(this.prog, "params.points");
		this.loc_blending = gl.glGetUniformLocation(this.prog, "params.blending");
		this.loc_invert = gl.glGetUniformLocation(this.prog, "params.invert");
		
		this.quadVAO = this.glu.genFullQuadVAO(gl);
	}
	
	public void globalVideoDispose(GL3 gl) {
	}
	
	public void effectMain() {
		this.enableVideoEffect();
	}
	
	public String getEffectClassName() {
		return "rectangle_mask";
	}
}
