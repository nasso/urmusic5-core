package io.github.nasso.urmusic.model.effect;

import static com.jogamp.opengl.GL.*;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Vector2fc;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL3;

import io.github.nasso.urmusic.model.project.TrackEffect;
import io.github.nasso.urmusic.model.project.param.FloatParam;
import io.github.nasso.urmusic.model.project.param.Point2DParam;
import io.github.nasso.urmusic.model.project.param.Vector2DParam;
import io.github.nasso.urmusic.model.renderer.EffectArgs;
import io.github.nasso.urmusic.model.renderer.GLUtils;

public class AffineTransformVFX extends TrackEffect {
	private GLUtils glu = new GLUtils();
	
	private int prog, quadVAO;
	private int loc_inputTex, loc_xform, loc_opacity;
	
	public class AffineTransformVFXInstance extends TrackEffectInstance {
		private Point2DParam translation = new Point2DParam("translation", 0, 0);
		private Vector2DParam scale = new Vector2DParam("scale", 1.0f, 1.0f, 0.01f, 0.01f);
		private FloatParam opacity = new FloatParam("opacity", 1.0f, 0.01f, 0.0f, 1.0f);
		
		private FloatBuffer _mat4Buf = Buffers.newDirectFloatBuffer(4 * 4);
		private Matrix4f _mat4 = new Matrix4f();
		
		public AffineTransformVFXInstance() {
			this.addParameter(this.translation);
			this.addParameter(this.scale);
			this.addParameter(this.opacity);
		}
		
		public void setupVideo(GL3 gl) {
		}

		public void applyVideo(GL3 gl, EffectArgs args) {		
			Vector2fc translation = this.translation.getValue(args.frame);
			Vector2fc scale = this.scale.getValue(args.frame);
			float opacity = this.opacity.getValue(args.frame);
			
			this._mat4.identity();
			this._mat4.translate(translation.x() / args.width * 2f, -translation.y() / args.height * 2f, 0.0f);
			// (rot here)
			this._mat4.scale(scale.x(), scale.y(), 1.0f);
			this._mat4.get(this._mat4Buf);
			
			gl.glUseProgram(AffineTransformVFX.this.prog);
			AffineTransformVFX.this.glu.uniformTexture(gl, AffineTransformVFX.this.loc_inputTex, args.texInput, 0);
			
			gl.glUniformMatrix4fv(AffineTransformVFX.this.loc_xform, this._mat4Buf.remaining() >> 4, false, this._mat4Buf);
			gl.glUniform1f(AffineTransformVFX.this.loc_opacity, opacity);
			
			gl.glBindVertexArray(AffineTransformVFX.this.quadVAO);
			gl.glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
		}

		public void disposeVideo(GL3 gl) {
			
		}
	}
	
	public TrackEffectInstance instance() {
		return new AffineTransformVFXInstance();
	}
	
	public void globalVideoSetup(GL3 gl) {
		this.prog = this.glu.createProgram(gl, "fx/affine_transform/", "vert_main.vs", "frag_main.fs");

		this.loc_inputTex = gl.glGetUniformLocation(this.prog, "inputTex");
		this.loc_xform = gl.glGetUniformLocation(this.prog, "xform");
		this.loc_opacity = gl.glGetUniformLocation(this.prog, "opacity");
		
		this.quadVAO = this.glu.genFullQuadVAO(gl);
	}

	public void globalVideoDispose(GL3 gl) {
		this.glu.dispose(gl);
	}

	public void effectMain() {
		this.enableVideoEffect();
	}
	
	public String getEffectClassName() {
		return "affine_transform";
	}
}
