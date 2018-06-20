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

import org.joml.Vector4fc;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import io.gitlab.nasso.urmusic.common.BoolValue;
import io.gitlab.nasso.urmusic.common.RGBA32;
import io.gitlab.nasso.urmusic.model.project.TrackEffect;
import io.gitlab.nasso.urmusic.model.project.VideoEffect;
import io.gitlab.nasso.urmusic.model.project.VideoEffectArgs;
import io.gitlab.nasso.urmusic.model.project.VideoEffectInstance;
import io.gitlab.nasso.urmusic.model.project.param.BooleanParam;
import io.gitlab.nasso.urmusic.model.project.param.BoundsParam;
import io.gitlab.nasso.urmusic.model.project.param.OptionParam;
import io.gitlab.nasso.urmusic.model.project.param.RGBA32Param;
import io.gitlab.nasso.urmusic.model.renderer.video.NGLUtils;

public class RectangleMaskVFX extends TrackEffect implements VideoEffect {
	private static final String PNAME_color = "color";
	private static final String PNAME_bounds = "bounds";
	private static final String PNAME_blendingMode = "blendingMode";
	private static final String PNAME_invert = "invert";
	
	private NGLUtils glu = new NGLUtils("rectangle mask global", RectangleMaskVFX.class.getClassLoader());
	
	private int prog, quadVAO;
	private int loc_inputTex, loc_size, loc_color, loc_points, loc_blending, loc_invert;
	
	public class RectangleMaskVFXInstance extends TrackEffectInstance implements VideoEffectInstance  {
		public void setupParameters() {
			this.addParameter(new RGBA32Param(PNAME_color, 0xffffffff));
			this.addParameter(new BoundsParam(PNAME_bounds, -50, -50, 100, 100));
			this.addParameter(new OptionParam(PNAME_blendingMode, 0,
				"srcOver",
				"dstOver",
				"srcIn",
				"dstIn",
				"srcOut",
				"dstOut",
				"srcAtop",
				"dstAtop",
				"copy",
				"add",
				"xor"
			));
			this.addParameter(new BooleanParam(PNAME_invert, BoolValue.FALSE));
		}
		
		public void setupVideo(GL3 gl) {
		}
		
		public void applyVideo(GL3 gl, VideoEffectArgs args) {
			RGBA32 color = (RGBA32) args.parameters.get(PNAME_color);
			Vector4fc bounds = (Vector4fc) args.parameters.get(PNAME_bounds);
			int blending = (int) args.parameters.get(PNAME_blendingMode);
			boolean invert = args.parameters.get(PNAME_invert) == BoolValue.TRUE;
			
			gl.glUseProgram(RectangleMaskVFX.this.prog);
			RectangleMaskVFX.this.glu.uniformTexture(gl, RectangleMaskVFX.this.loc_inputTex, args.texInput, 0);
			
			gl.glUniform2f(RectangleMaskVFX.this.loc_size, args.width, args.height);
			gl.glUniform4f(RectangleMaskVFX.this.loc_color, color.getRedf(), color.getGreenf(), color.getBluef(), color.getAlphaf());
			gl.glUniform4f(RectangleMaskVFX.this.loc_points,
					bounds.x(),
					-bounds.y(),
					bounds.x() + bounds.z(),
					-bounds.y() - bounds.w()
			);
			gl.glUniform1i(RectangleMaskVFX.this.loc_blending, blending);
			gl.glUniform1i(RectangleMaskVFX.this.loc_invert, invert ? 1 : 0);
			
			gl.glBindVertexArray(RectangleMaskVFX.this.quadVAO);
			gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, 4);
		}
		
		public void disposeVideo(GL3 gl) {
		}
	}
	
	public TrackEffectInstance instance() {
		return new RectangleMaskVFXInstance();
	}
	
	public void globalVideoSetup(GL3 gl) {
		this.prog = this.glu.createProgram(gl, "fx/rectangle_mask/", "main_vert.vs", "main_frag.fs");
		
		this.loc_inputTex = gl.glGetUniformLocation(this.prog, "inputTex");
		this.loc_size = gl.glGetUniformLocation(this.prog, "colorSize");
		
		this.loc_color = gl.glGetUniformLocation(this.prog, "params.color");
		this.loc_points = gl.glGetUniformLocation(this.prog, "params.points");
		this.loc_blending = gl.glGetUniformLocation(this.prog, "params.blending");
		this.loc_invert = gl.glGetUniformLocation(this.prog, "params.invert");
		
		this.quadVAO = this.glu.createFullQuadVAO(gl);
	}
	
	public void globalVideoDispose(GL3 gl) {
		this.glu.dispose(gl);
	}
	
	public void effectMain() {
	}
	
	public String getEffectClassID() {
		return "rectangle_mask";
	}
}
