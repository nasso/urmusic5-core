package io.github.nasso.urmusic.model.effect;

import org.joml.Matrix4f;
import org.joml.Vector2fc;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import io.github.nasso.urmusic.common.MathUtils;
import io.github.nasso.urmusic.model.project.TrackEffect;
import io.github.nasso.urmusic.model.project.VideoEffect;
import io.github.nasso.urmusic.model.project.VideoEffectArgs;
import io.github.nasso.urmusic.model.project.VideoEffectInstance;
import io.github.nasso.urmusic.model.project.param.FloatParam;
import io.github.nasso.urmusic.model.project.param.Point2DParam;
import io.github.nasso.urmusic.model.project.param.Vector2DParam;
import io.github.nasso.urmusic.model.renderer.video.GLUtils;

public class AffineTransformVFX extends TrackEffect implements VideoEffect {
	private GLUtils glu = new GLUtils("affine transform global");
	
	private int prog, quadVAO;
	private int loc_inputTex, loc_xform, loc_opacity;
	
	public class AffineTransformVFXInstance extends TrackEffectInstance implements VideoEffectInstance {
		private Point2DParam translation = new Point2DParam("translation", 0, 0);
		private FloatParam rotation = new FloatParam("rotation", 0.0f);
		private Vector2DParam scale = new Vector2DParam("scale", 1.0f, 1.0f, 0.01f, 0.01f);
		private FloatParam opacity = new FloatParam("opacity", 1.0f, 0.01f, 0.0f, 1.0f);
		
		private Matrix4f xform = new Matrix4f();
		
		public AffineTransformVFXInstance() {
			this.addParameter(this.translation);
			this.addParameter(this.rotation);
			this.addParameter(this.scale);
			this.addParameter(this.opacity);
		}
		
		public void setupVideo(GL3 gl) {
		}

		public void applyVideo(GL3 gl, VideoEffectArgs args) {
			Vector2fc translation = this.translation.getValue(args.time);
			float rotation = this.rotation.getValue(args.time);
			Vector2fc scale = this.scale.getValue(args.time);
			float opacity = this.opacity.getValue(args.time);
			
			this.xform.identity();
			this.xform.translate(translation.x() / args.width * 2f, -translation.y() / args.height * 2f, 0.0f);
			this.xform.scale(1f / args.width, 1f / args.height, 1f);
			this.xform.rotateZ(rotation / 180.0f * MathUtils.PI);
			this.xform.scale(scale.x() * args.width, scale.y() * args.height, 1.0f);
			
			gl.glUseProgram(AffineTransformVFX.this.prog);
			AffineTransformVFX.this.glu.uniformTexture(gl, AffineTransformVFX.this.loc_inputTex, args.texInput, 0);
			
			AffineTransformVFX.this.glu.uniformMatrix(gl, AffineTransformVFX.this.loc_xform, this.xform);
			gl.glUniform1f(AffineTransformVFX.this.loc_opacity, opacity);
			
			gl.glBindVertexArray(AffineTransformVFX.this.quadVAO);
			gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, 4);
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
		
		this.quadVAO = this.glu.createFullQuadVAO(gl);
	}

	public void globalVideoDispose(GL3 gl) {
		this.glu.dispose(gl);
	}

	public void effectMain() {
	}
	
	public String getEffectClassName() {
		return "affine_transform";
	}
}
