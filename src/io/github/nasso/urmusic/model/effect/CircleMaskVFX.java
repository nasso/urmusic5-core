package io.github.nasso.urmusic.model.effect;

import org.joml.Vector2fc;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import io.github.nasso.urmusic.common.BoolValue;
import io.github.nasso.urmusic.common.RGBA32;
import io.github.nasso.urmusic.model.project.TrackEffect;
import io.github.nasso.urmusic.model.project.VideoEffect;
import io.github.nasso.urmusic.model.project.VideoEffectArgs;
import io.github.nasso.urmusic.model.project.VideoEffectInstance;
import io.github.nasso.urmusic.model.project.param.BooleanParam;
import io.github.nasso.urmusic.model.project.param.FloatParam;
import io.github.nasso.urmusic.model.project.param.OptionParam;
import io.github.nasso.urmusic.model.project.param.Point2DParam;
import io.github.nasso.urmusic.model.project.param.RGBA32Param;
import io.github.nasso.urmusic.model.renderer.video.NGLUtils;

public class CircleMaskVFX extends TrackEffect implements VideoEffect {
	private static final String PNAME_position = "position";
	private static final String PNAME_color = "color";
	private static final String PNAME_outerRadius = "outerRadius";
	private static final String PNAME_outerFade = "outerFade";
	private static final String PNAME_innerRadius = "innerRadius";
	private static final String PNAME_innerFade = "innerFade";
	private static final String PNAME_blendingMode = "blendingMode";
	private static final String PNAME_invert = "invert";
	
	private NGLUtils glu = new NGLUtils("circle mask global");
	
	private int prog, quadVAO;
	private int loc_inputTex, loc_size, loc_color, loc_originInOutRadius, loc_inOutFade, loc_blending, loc_invert;
	
	public class CircleMaskVFXInstance extends TrackEffectInstance implements VideoEffectInstance  {
		public void setupParameters() {
			this.addParameter(new Point2DParam(PNAME_position, 0, 0));
			this.addParameter(new RGBA32Param(PNAME_color, 0xFFFFFFFF));
			this.addParameter(new FloatParam(PNAME_outerRadius, 200.0f, 1.0f, 0.0f, Float.MAX_VALUE));
			this.addParameter(new FloatParam(PNAME_outerFade, 1.0f, 1.0f, 0.0f, Float.MAX_VALUE));
			this.addParameter(new FloatParam(PNAME_innerRadius, 0.0f, 1.0f, 0.0f, Float.MAX_VALUE));
			this.addParameter(new FloatParam(PNAME_innerFade, 0.0f, 1.0f, 0.0f, Float.MAX_VALUE));
			this.addParameter(new OptionParam(PNAME_blendingMode, 0,
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
			));
			this.addParameter(new BooleanParam(PNAME_invert, BoolValue.FALSE));
		}
		
		public void setupVideo(GL3 gl) {
			
		}
		
		public void applyVideo(GL3 gl, VideoEffectArgs args) {
			// Retrieve params
			Vector2fc position = (Vector2fc) args.parameters.get(PNAME_position);
			RGBA32 color = (RGBA32) args.parameters.get(PNAME_color);
			float innerRadius = (float) args.parameters.get(PNAME_innerRadius);
			float outerRadius = (float) args.parameters.get(PNAME_outerRadius);
			float innerFade = (float) args.parameters.get(PNAME_innerFade);
			float outerFade = (float) args.parameters.get(PNAME_outerFade);
			int blending = (int) args.parameters.get(PNAME_blendingMode);
			boolean invert = args.parameters.get(PNAME_invert) == BoolValue.TRUE;
			
			gl.glUseProgram(CircleMaskVFX.this.prog);
			CircleMaskVFX.this.glu.uniformTexture(gl, CircleMaskVFX.this.loc_inputTex, args.texInput, 0);
			
			gl.glUniform2f(CircleMaskVFX.this.loc_size, args.width, args.height);
			gl.glUniform4f(CircleMaskVFX.this.loc_color, color.getRedf(), color.getGreenf(), color.getBluef(), color.getAlphaf());
			gl.glUniform4f(CircleMaskVFX.this.loc_originInOutRadius,
					position.x(),
					-position.y(),
					innerRadius,
					outerRadius
			);
			gl.glUniform2f(CircleMaskVFX.this.loc_inOutFade,
					innerFade,
					outerFade
			);
			gl.glUniform1i(CircleMaskVFX.this.loc_blending, blending);
			gl.glUniform1i(CircleMaskVFX.this.loc_invert, invert ? 1 : 0);
			
			gl.glBindVertexArray(CircleMaskVFX.this.quadVAO);
			gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, 4);
		}
		
		public void disposeVideo(GL3 gl) {
		}
	}
	
	public void globalVideoSetup(GL3 gl) {
		this.prog = this.glu.createProgram(gl, "fx/circle_mask/", "main_vert.vs", "main_frag.fs");
		
		this.loc_inputTex = gl.glGetUniformLocation(this.prog, "inputTex");
		this.loc_size = gl.glGetUniformLocation(this.prog, "colorSize");
		
		this.loc_color = gl.glGetUniformLocation(this.prog, "params.color");
		this.loc_originInOutRadius = gl.glGetUniformLocation(this.prog, "params.originInOutRadius");
		this.loc_inOutFade = gl.glGetUniformLocation(this.prog, "params.inOutFade");
		this.loc_blending = gl.glGetUniformLocation(this.prog, "params.blending");
		this.loc_invert = gl.glGetUniformLocation(this.prog, "params.invert");
		
		this.quadVAO = this.glu.createFullQuadVAO(gl);
	}

	public void globalVideoDispose(GL3 gl) {
		this.glu.dispose(gl);
	}

	public TrackEffectInstance instance() {
		return new CircleMaskVFXInstance();
	}

	public void effectMain() {
	}

	public String getEffectClassID() {
		return "urm.circle_mask";
	}
}
