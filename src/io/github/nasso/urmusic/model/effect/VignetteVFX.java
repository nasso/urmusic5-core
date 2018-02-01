package io.github.nasso.urmusic.model.effect;

import static com.jogamp.opengl.GL.*;

import com.jogamp.opengl.GL3;

import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.project.TrackEffect;
import io.github.nasso.urmusic.model.project.control.FloatParam;
import io.github.nasso.urmusic.model.project.control.RGBA32Param;
import io.github.nasso.urmusic.model.renderer.GLUtils;
import io.github.nasso.urmusic.utils.MutableRGBA32;
import io.github.nasso.urmusic.utils.RGBA32;

public class VignetteVFX extends TrackEffect {
	public static final VignetteVFX FX = new VignetteVFX();
	
	private GLUtils glu = new GLUtils();
	
	private int quadProg, quadVAO;
	private int loc_colorTex, loc_vignetteColor, loc_parameters;
	
	public class VignetteVFXInstance extends TrackEffectInstance {
		private FloatParam dist = new FloatParam(1.2f);
		private FloatParam penumbra = new FloatParam(0.75f);
		private RGBA32Param outerColor = new RGBA32Param(0x000000FF);
		
		public VignetteVFXInstance() {
			this.addControlParameter(this.dist);
			this.addControlParameter(this.penumbra);
			this.addControlParameter(this.outerColor);
			
			this.outerColor.addKeyFrame(0, new MutableRGBA32(0x000000FF));
			this.outerColor.addKeyFrame(50, new MutableRGBA32(0xFF0000FF));
			this.outerColor.addKeyFrame(100, new MutableRGBA32(0x00FF00FF));
			this.outerColor.addKeyFrame(150, new MutableRGBA32(0x0000FFFF));
			this.outerColor.addKeyFrame(200, new MutableRGBA32(0xFFFFFFFF));
		}
		
		public void setupVideo(GL3 gl) {
			
		}

		public void applyVideo(GL3 gl, int texInput, int fboOutput) {
			int frame = UrmusicModel.getRenderer().getCurrentDestCacheFrame().frame_id;
			
			gl.glUseProgram(VignetteVFX.this.quadProg);
			VignetteVFX.this.glu.uniformTexture(gl, VignetteVFX.this.loc_colorTex, texInput, 0);
			
			RGBA32 outerColor = this.outerColor.getValue(frame);
			gl.glUniform4f(VignetteVFX.this.loc_vignetteColor, outerColor.getRedf(), outerColor.getGreenf(), outerColor.getBluef(), outerColor.getAlphaf());
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
