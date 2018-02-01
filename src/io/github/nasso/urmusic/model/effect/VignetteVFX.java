package io.github.nasso.urmusic.model.effect;

import static com.jogamp.opengl.GL.*;

import com.jogamp.opengl.GL3;

import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.project.TrackEffect;
import io.github.nasso.urmusic.model.project.control.FloatParam;
import io.github.nasso.urmusic.model.renderer.GLUtils;

public class VignetteVFX extends TrackEffect {
	public static final VignetteVFX FX = new VignetteVFX();
	
	private GLUtils glu = new GLUtils();
	
	private int quadProg, quadVAO;
	private int loc_colorTex, loc_vignetteColor, loc_parameters;
	
	public class VignetteVFXInstance extends TrackEffectInstance {
		private FloatParam dist = new FloatParam(1.2f);
		private FloatParam penumbra = new FloatParam(0.75f);
		
		public VignetteVFXInstance() {
			this.addControlParameter(this.dist);
			this.addControlParameter(this.penumbra);
		}
		
		public void setupVideo(GL3 gl) {
			
		}

		public void applyVideo(GL3 gl, int texInput, int fboOutput) {
			int frame = UrmusicModel.getRenderer().getCurrentDestCacheFrame().frame_id;
			
			gl.glUseProgram(VignetteVFX.this.quadProg);
			VignetteVFX.this.glu.uniformTexture(gl, VignetteVFX.this.loc_colorTex, texInput, 0);
			
			gl.glUniform4f(VignetteVFX.this.loc_vignetteColor, 0, 0, 0, 1);
			gl.glUniform2f(VignetteVFX.this.loc_parameters, this.dist.getValue(frame), this.penumbra.getValue(frame));
			
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
}
