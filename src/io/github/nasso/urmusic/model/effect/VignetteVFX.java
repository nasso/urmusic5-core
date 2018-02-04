package io.github.nasso.urmusic.model.effect;

import static com.jogamp.opengl.GL.*;

import com.jogamp.opengl.GL3;

import io.github.nasso.urmusic.model.project.TrackEffect;
import io.github.nasso.urmusic.model.project.control.FloatParam;
import io.github.nasso.urmusic.model.project.control.RGBA32Param;
import io.github.nasso.urmusic.model.renderer.EffectArgs;
import io.github.nasso.urmusic.model.renderer.GLUtils;
import io.github.nasso.urmusic.utils.RGBA32;

public class VignetteVFX extends TrackEffect {
	public static final VignetteVFX FX = new VignetteVFX();
	
	private GLUtils glu = new GLUtils();
	
	private int quadProg, quadVAO;
	private int loc_inputTex, loc_size, loc_outerColor, loc_innerColor, loc_parameters;
	
	public class VignetteVFXInstance extends TrackEffectInstance {
		private RGBA32Param outerColor = new RGBA32Param("outerColor", 0x000000FF);
		private RGBA32Param innerColor = new RGBA32Param("innerColor", 0x00000000);
		private FloatParam penumbra = new FloatParam("penumbra", 200.0f);
		private FloatParam distance = new FloatParam("distance", 1500.0f);
		
		public VignetteVFXInstance() {
			this.addParameter(this.outerColor);
			this.addParameter(this.innerColor);
			this.addParameter(this.distance);
			this.addParameter(this.penumbra);
		}
		
		public void setupVideo(GL3 gl) {
			
		}

		public void applyVideo(GL3 gl, EffectArgs args) {
			// Retrieve params
			float dist = this.distance.getValue(args.frame);
			float penumbra = this.penumbra.getValue(args.frame);
			RGBA32 outerColor = this.outerColor.getValue(args.frame);
			RGBA32 innerColor = this.innerColor.getValue(args.frame);
			
			gl.glUseProgram(VignetteVFX.this.quadProg);
			VignetteVFX.this.glu.uniformTexture(gl, VignetteVFX.this.loc_inputTex, args.texInput, 0);
			
			gl.glUniform2f(VignetteVFX.this.loc_size, args.width, args.height);
			gl.glUniform4f(VignetteVFX.this.loc_outerColor, outerColor.getRedf(), outerColor.getGreenf(), outerColor.getBluef(), outerColor.getAlphaf());
			gl.glUniform4f(VignetteVFX.this.loc_innerColor, innerColor.getRedf(), innerColor.getGreenf(), innerColor.getBluef(), innerColor.getAlphaf());
			gl.glUniform4f(VignetteVFX.this.loc_parameters,
					args.width / 2f,
					args.height / 2f,
					dist,
					penumbra
			);
			
			gl.glBindVertexArray(VignetteVFX.this.quadVAO);
			gl.glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
		}
		
		public void disposeVideo(GL3 gl) {
		}
	}
	
	public void globalVideoSetup(GL3 gl) {
		this.quadProg = this.glu.createProgram(gl, "fx/vignette/vignette.vs", "fx/vignette/vignette.fs");
		this.loc_inputTex = gl.glGetUniformLocation(this.quadProg, "inputTex");
		this.loc_size = gl.glGetUniformLocation(this.quadProg, "colorSize");
		this.loc_outerColor = gl.glGetUniformLocation(this.quadProg, "outerColor");
		this.loc_innerColor = gl.glGetUniformLocation(this.quadProg, "innerColor");
		this.loc_parameters = gl.glGetUniformLocation(this.quadProg, "parameters");
		
		this.quadVAO = this.glu.genFullQuadVAO(gl);
	}

	public void globalVideoDispose(GL3 gl) {
		this.glu.dispose(gl);
	}

	public TrackEffectInstance instance() {
		return new VignetteVFXInstance();
	}

	public void effectMain() {
		this.setVideoEffect();
	}

	public String getEffectClassName() {
		return "vignette";
	}
}
