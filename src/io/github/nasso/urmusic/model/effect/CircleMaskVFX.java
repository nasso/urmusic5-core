package io.github.nasso.urmusic.model.effect;

import static com.jogamp.opengl.GL.*;

import org.joml.Vector2fc;

import com.jogamp.opengl.GL3;

import io.github.nasso.urmusic.model.project.TrackEffect;
import io.github.nasso.urmusic.model.project.control.FloatParam;
import io.github.nasso.urmusic.model.project.control.Point2DParam;
import io.github.nasso.urmusic.model.project.control.RGBA32Param;
import io.github.nasso.urmusic.model.renderer.EffectArgs;
import io.github.nasso.urmusic.model.renderer.GLUtils;
import io.github.nasso.urmusic.utils.RGBA32;

public class CircleMaskVFX extends TrackEffect {
	public static final CircleMaskVFX FX = new CircleMaskVFX();
	
	private GLUtils glu = new GLUtils();
	
	private int quadProg, quadVAO;
	private int loc_inputTex, loc_size, loc_outerColor, loc_innerColor, loc_parameters;
	
	public class CircleMaskVFXInstance extends TrackEffectInstance {
		private Point2DParam position = new Point2DParam("position", 0, 0);
		private RGBA32Param outerColor = new RGBA32Param("outerColor", 0x00000000);
		private RGBA32Param innerColor = new RGBA32Param("innerColor", 0xFFFFFFFF);
		private FloatParam penumbra = new FloatParam("penumbra", 5.0f, 1.0f);
		private FloatParam radius = new FloatParam("radius", 500.0f, 1.0f);
		
		public CircleMaskVFXInstance() {
			this.addParameter(this.position);
			this.addParameter(this.outerColor);
			this.addParameter(this.innerColor);
			this.addParameter(this.radius);
			this.addParameter(this.penumbra);
		}
		
		public void setupVideo(GL3 gl) {
			
		}
		
		public void applyVideo(GL3 gl, EffectArgs args) {
			// Retrieve params
			Vector2fc position = this.position.getValue(args.frame);
			float radius = this.radius.getValue(args.frame);
			float penumbra = this.penumbra.getValue(args.frame);
			RGBA32 outerColor = this.outerColor.getValue(args.frame);
			RGBA32 innerColor = this.innerColor.getValue(args.frame);
			
			gl.glUseProgram(CircleMaskVFX.this.quadProg);
			CircleMaskVFX.this.glu.uniformTexture(gl, CircleMaskVFX.this.loc_inputTex, args.texInput, 0);
			
			gl.glUniform2f(CircleMaskVFX.this.loc_size, args.width, args.height);
			gl.glUniform4f(CircleMaskVFX.this.loc_outerColor, outerColor.getRedf(), outerColor.getGreenf(), outerColor.getBluef(), outerColor.getAlphaf());
			gl.glUniform4f(CircleMaskVFX.this.loc_innerColor, innerColor.getRedf(), innerColor.getGreenf(), innerColor.getBluef(), innerColor.getAlphaf());
			gl.glUniform4f(CircleMaskVFX.this.loc_parameters,
					position.x(),
					-position.y(),
					radius,
					penumbra
			);
			
			gl.glBindVertexArray(CircleMaskVFX.this.quadVAO);
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
		return new CircleMaskVFXInstance();
	}

	public void effectMain() {
		this.setVideoEffect();
	}

	public String getEffectClassName() {
		return "circle_mask";
	}
}
