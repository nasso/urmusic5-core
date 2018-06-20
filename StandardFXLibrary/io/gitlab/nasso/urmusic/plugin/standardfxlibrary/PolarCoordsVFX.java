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

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import io.gitlab.nasso.urmusic.common.BoolValue;
import io.gitlab.nasso.urmusic.model.project.TrackEffect;
import io.gitlab.nasso.urmusic.model.project.VideoEffect;
import io.gitlab.nasso.urmusic.model.project.VideoEffectArgs;
import io.gitlab.nasso.urmusic.model.project.VideoEffectInstance;
import io.gitlab.nasso.urmusic.model.project.param.BooleanParam;
import io.gitlab.nasso.urmusic.model.renderer.video.NGLUtils;

public class PolarCoordsVFX extends TrackEffect implements VideoEffect {
	private static final String PNAME_polarToRect = "polarToRect";
	
	private NGLUtils glu = new NGLUtils("polar coords global", PolarCoordsVFX.class.getClassLoader());
	
	private int prog, quadVAO;
	private int loc_inputTex, loc_aspectRatio, loc_modePolarToRect;
	
	private class PolarCoordsVFXInstance extends TrackEffectInstance implements VideoEffectInstance {
		public void setupParameters() {
			this.addParameter(new BooleanParam(PNAME_polarToRect, BoolValue.FALSE));
		}
		
		public void setupVideo(GL3 gl) {
		}

		public void applyVideo(GL3 gl, VideoEffectArgs args) {
			boolean polarToRect = args.parameters.get(PNAME_polarToRect) == BoolValue.TRUE;
			
			gl.glUseProgram(PolarCoordsVFX.this.prog);
			PolarCoordsVFX.this.glu.uniformTexture(gl, PolarCoordsVFX.this.loc_inputTex, args.texInput, 0);
			gl.glUniform1f(PolarCoordsVFX.this.loc_aspectRatio, (float) args.width / args.height);
			gl.glUniform1i(PolarCoordsVFX.this.loc_modePolarToRect, polarToRect ? 1 : 0);
			
			gl.glBindVertexArray(PolarCoordsVFX.this.quadVAO);
			gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, 4);
		}

		public void disposeVideo(GL3 gl) {
			
		}
		
	}
	
	public TrackEffectInstance instance() {
		return new PolarCoordsVFXInstance();
	}

	public void globalVideoSetup(GL3 gl) {
		this.prog = this.glu.createProgram(gl, "fx/polar_coords/", "main_vert.vs", "main_frag.fs");
		
		this.loc_inputTex = gl.glGetUniformLocation(this.prog, "inputTex");
		this.loc_aspectRatio = gl.glGetUniformLocation(this.prog, "aspectRatio");
		this.loc_modePolarToRect = gl.glGetUniformLocation(this.prog, "modePolarToRect");
		
		this.quadVAO = this.glu.createFullQuadVAO(gl);
	}

	public void globalVideoDispose(GL3 gl) {
		this.glu.dispose(gl);
	}

	public void effectMain() {
		
	}

	public String getEffectClassID() {
		return "polar_coords";
	}
}
