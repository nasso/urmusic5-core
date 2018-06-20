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

import org.joml.Vector2fc;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import io.gitlab.nasso.urmusic.common.BoolValue;
import io.gitlab.nasso.urmusic.model.project.TrackEffect;
import io.gitlab.nasso.urmusic.model.project.VideoEffect;
import io.gitlab.nasso.urmusic.model.project.VideoEffectArgs;
import io.gitlab.nasso.urmusic.model.project.VideoEffectInstance;
import io.gitlab.nasso.urmusic.model.project.param.BooleanParam;
import io.gitlab.nasso.urmusic.model.project.param.Point2DParam;
import io.gitlab.nasso.urmusic.model.renderer.video.NGLUtils;

public class MirrorVFX extends TrackEffect implements VideoEffect {
	private static final String PNAME_edge0 = "edge0";
	private static final String PNAME_edge1 = "edge1";
	private static final String PNAME_invert = "invert";
	
	private NGLUtils glu = new NGLUtils("mirror effect global", MirrorVFX.class.getClassLoader());
	
	private int prog, quadVAO;
	private int loc_inputTex, loc_edge;
	
	public class MirrorVFXInstance extends TrackEffectInstance implements VideoEffectInstance  {
		public void setupParameters() {
			this.addParameter(new Point2DParam(PNAME_edge0, 0, 100));
			this.addParameter(new Point2DParam(PNAME_edge1, 0, -100));
			this.addParameter(new BooleanParam(PNAME_invert, BoolValue.FALSE));
		}
		
		public void setupVideo(GL3 gl) {
		}
		
		public void applyVideo(GL3 gl, VideoEffectArgs args) {
			Vector2fc edge0 = (Vector2fc) args.parameters.get(PNAME_edge0);
			Vector2fc edge1 = (Vector2fc) args.parameters.get(PNAME_edge1);
			boolean invert = args.parameters.get(PNAME_invert) == BoolValue.TRUE;
			
			gl.glUseProgram(MirrorVFX.this.prog);
			MirrorVFX.this.glu.uniformTexture(gl, MirrorVFX.this.loc_inputTex, args.texInput, 0);
			
			float e0x = (+edge0.x() / (args.width /2)) * 0.5f + 0.5f;
			float e0y = (-edge0.y() / (args.height/2)) * 0.5f + 0.5f;
			float e1x = (+edge1.x() / (args.width /2)) * 0.5f + 0.5f;
			float e1y = (-edge1.y() / (args.height/2)) * 0.5f + 0.5f;
			
			if(invert) gl.glUniform4f(MirrorVFX.this.loc_edge, e1x, e1y, e0x, e0y);
			else gl.glUniform4f(MirrorVFX.this.loc_edge, e0x, e0y, e1x, e1y);
			
			gl.glBindVertexArray(MirrorVFX.this.quadVAO);
			gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, 4);
		}
		
		public void disposeVideo(GL3 gl) {
		}
	}
	
	public TrackEffectInstance instance() {
		return new MirrorVFXInstance();
	}
	
	public void globalVideoSetup(GL3 gl) {
		this.prog = this.glu.createProgram(gl, "fx/mirror/", "main_vert.vs", "main_frag.fs");
		
		this.loc_inputTex = gl.glGetUniformLocation(this.prog, "inputTex");
		this.loc_edge = gl.glGetUniformLocation(this.prog, "edge");
		
		this.quadVAO = this.glu.createFullQuadVAO(gl);
	}
	
	public void globalVideoDispose(GL3 gl) {
		this.glu.dispose(gl);
	}
	
	public void effectMain() {
	}
	
	public String getEffectClassID() {
		return "mirror";
	}
}
