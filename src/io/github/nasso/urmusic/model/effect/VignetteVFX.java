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
	private int loc_colorTex, loc_vignetteColor, loc_parameters;
	
	public class VignetteVFXInstance extends TrackEffectInstance {
		private FloatParam distance = new FloatParam(1.2f);
		private FloatParam penumbra = new FloatParam(0.75f);
		private RGBA32Param outerColor = new RGBA32Param(0x000000FF);
		
		public VignetteVFXInstance() {
			this.addControl("distance", this.distance);
			this.addControl("penumbra", this.penumbra);
			this.addControl("outerColor", this.outerColor);
		}
		
		public void setupVideo(GL3 gl) {
			
		}

		public void applyVideo(GL3 gl, EffectArgs args) {
			// Retrieve params
			float dist = this.distance.getValue(args.frame);
			float penumbra = this.penumbra.getValue(args.frame);
			RGBA32 outerColor = this.outerColor.getValue(args.frame);
			
			gl.glUseProgram(VignetteVFX.this.quadProg);
			VignetteVFX.this.glu.uniformTexture(gl, VignetteVFX.this.loc_colorTex, args.texInput, 0);
			
			gl.glUniform4f(VignetteVFX.this.loc_vignetteColor, outerColor.getRedf(), outerColor.getGreenf(), outerColor.getBluef(), outerColor.getAlphaf());
			gl.glUniform2f(VignetteVFX.this.loc_parameters, dist, penumbra);
			
			gl.glBindVertexArray(VignetteVFX.this.quadVAO);
			gl.glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
		}
		
		public void disposeVideo(GL3 gl) {
		}
	}
	
	public void globalVideoSetup(GL3 gl) {
		this.quadProg = this.glu.createProgram(gl, "fx/vignette/vignette.vs", "fx/vignette/vignette.fs");
		this.loc_colorTex = gl.glGetUniformLocation(this.quadProg, "color");
		this.loc_vignetteColor = gl.glGetUniformLocation(this.quadProg, "vignetteColor");
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
