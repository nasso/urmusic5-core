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
	private int loc_inputTex, loc_size, loc_color, loc_originInOutRadius, loc_inOutFade;
	
	public class CircleMaskVFXInstance extends TrackEffectInstance {
		private Point2DParam position = new Point2DParam("position", 0, 0);
		private RGBA32Param color = new RGBA32Param("color", 0xFFFFFFFF);
		private FloatParam innerRadius = new FloatParam("innerRadius", 0.0f, 1.0f);
		private FloatParam outerRadius = new FloatParam("outerRadius", 400.0f, 1.0f);
		private FloatParam innerFade = new FloatParam("innerFade", 0.0f, 1.0f);
		private FloatParam outerFade = new FloatParam("outerFade", 400.0f, 1.0f);
		
		public CircleMaskVFXInstance() {
			this.addParameter(this.position);
			this.addParameter(this.color);
			this.addParameter(this.innerRadius);
			this.addParameter(this.outerRadius);
			this.addParameter(this.innerFade);
			this.addParameter(this.outerFade);
		}
		
		public void setupVideo(GL3 gl) {
			
		}
		
		public void applyVideo(GL3 gl, EffectArgs args) {
			// Retrieve params
			Vector2fc position = this.position.getValue(args.frame);
			RGBA32 color = this.color.getValue(args.frame);
			float innerRadius = this.innerRadius.getValue(args.frame);
			float outerRadius = this.outerRadius.getValue(args.frame);
			float innerFade = this.innerFade.getValue(args.frame);
			float outerFade = this.outerFade.getValue(args.frame);
			
			gl.glUseProgram(CircleMaskVFX.this.quadProg);
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
			
			gl.glBindVertexArray(CircleMaskVFX.this.quadVAO);
			gl.glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
		}
		
		public void disposeVideo(GL3 gl) {
		}
	}
	
	public void globalVideoSetup(GL3 gl) {
		this.quadProg = this.glu.createProgram(gl, "fx/circle_mask/vert_main.vs", "fx/circle_mask/frag_main.fs");
		this.loc_inputTex = gl.glGetUniformLocation(this.quadProg, "inputTex");
		this.loc_size = gl.glGetUniformLocation(this.quadProg, "colorSize");
		
		this.loc_color = gl.glGetUniformLocation(this.quadProg, "params.color");
		this.loc_originInOutRadius = gl.glGetUniformLocation(this.quadProg, "params.originInOutRadius");
		this.loc_inOutFade = gl.glGetUniformLocation(this.quadProg, "params.inOutFade");
		
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
