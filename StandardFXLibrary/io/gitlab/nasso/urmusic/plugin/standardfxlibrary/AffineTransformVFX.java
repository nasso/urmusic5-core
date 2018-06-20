/*******************************************************************************
 * urmusic - The Free and Open Source Music Visualizer Tool
 * Copyright (C) 2018  nasso (https://github.com/nasso)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * Contact "nasso": nassomails -at- gmail dot com
 ******************************************************************************/
package io.gitlab.nasso.urmusic.plugin.standardfxlibrary;

import org.joml.Matrix4f;
import org.joml.Vector2fc;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import io.gitlab.nasso.urmusic.common.MathUtils;
import io.gitlab.nasso.urmusic.model.project.TrackEffect;
import io.gitlab.nasso.urmusic.model.project.VideoEffect;
import io.gitlab.nasso.urmusic.model.project.VideoEffectArgs;
import io.gitlab.nasso.urmusic.model.project.VideoEffectInstance;
import io.gitlab.nasso.urmusic.model.project.param.FloatParam;
import io.gitlab.nasso.urmusic.model.project.param.Point2DParam;
import io.gitlab.nasso.urmusic.model.project.param.Vector2DParam;
import io.gitlab.nasso.urmusic.model.renderer.video.NGLUtils;

public class AffineTransformVFX extends TrackEffect implements VideoEffect {
	private static final String PNAME_translation = "translation";
	private static final String PNAME_rotation = "rotation";
	private static final String PNAME_scale = "scale";
	private static final String PNAME_opacity = "opacity";
	
	private NGLUtils glu = new NGLUtils("affine transform global", AffineTransformVFX.class.getClassLoader());
	
	private int prog, quadVAO;
	private int loc_inputTex, loc_xform, loc_opacity;
	
	public class AffineTransformVFXInstance extends TrackEffectInstance implements VideoEffectInstance {
		private Matrix4f xform = new Matrix4f();
		
		public void setupParameters() {
			this.addParameter(new Point2DParam(PNAME_translation, 0, 0));
			this.addParameter(new FloatParam(PNAME_rotation, 0.0f));
			this.addParameter(new Vector2DParam(PNAME_scale, 1.0f, 1.0f, 0.01f, 0.01f));
			this.addParameter(new FloatParam(PNAME_opacity, 1.0f, 0.01f, 0.0f, 1.0f));
		}
		
		public void setupVideo(GL3 gl) {
		}

		public void applyVideo(GL3 gl, VideoEffectArgs args) {
			Vector2fc translation = (Vector2fc) args.parameters.get(PNAME_translation);
			float rotation = (float) args.parameters.get(PNAME_rotation);
			Vector2fc scale = (Vector2fc) args.parameters.get(PNAME_scale);
			float opacity = (float) args.parameters.get(PNAME_opacity);
			
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
		this.prog = this.glu.createProgram(gl, "fx/affine_transform/", "main_vert.vs", "main_frag.fs");

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
	
	public String getEffectClassID() {
		return "affine_transform";
	}
}
