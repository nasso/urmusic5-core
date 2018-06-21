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
package io.gitlab.nasso.urmusic.model.effect;

import org.joml.Vector2fc;

import com.jogamp.opengl.GL3;

import io.gitlab.nasso.urmusic.model.project.VideoEffect;
import io.gitlab.nasso.urmusic.model.project.VideoEffectArgs;
import io.gitlab.nasso.urmusic.model.project.param.Point2DParam;
import io.gitlab.nasso.urmusic.model.renderer.video.glvg.GLVG;
import io.gitlab.nasso.urmusic.model.renderer.video.glvg.VGLineCap;

public class TestVFX extends VideoEffect {
	private class TestVFXInstance extends VideoEffectInstance {
		Vector2fc[] points;
		private GLVG vg;
		
		public void setupParameters() {
			this.points = new Vector2fc[8];
			
			for(int i = 0; i < this.points.length; i++) {
				float p = (float) i / this.points.length;
				
				float cs = (float) (Math.cos(p * Math.PI * 2) * 200);
				float sn = (float) (Math.sin(p * Math.PI * 2) * 200);
				
				this.addParameter(new Point2DParam("pt" + i, cs, sn));
			}
		}
		
		public void setupVideo(GL3 gl) {
			this.vg = new GLVG(gl);
			
			this.vg.setFillColor(0x0000FFFF);
			this.vg.setStrokeColor(0xFF0000FF);
			
			this.vg.setLineWidth(8);
			this.vg.setLineCaps(VGLineCap.ROUND);
		}

		public void applyVideo(GL3 gl, VideoEffectArgs args) {
			for(int i = 0; i < this.points.length; i++) {
				this.points[i] = (Vector2fc) args.parameters.get("pt" + i);
			}
			
			this.vg.begin(gl, args.width, args.height);

			this.vg.setLineWidth(64);

			this.vg.beginPath();
			for(int i = 0; i < this.points.length; i++) {
				Vector2fc p = this.points[i];
				
				this.vg.lineTo(p.x(), -p.y());
			}
			this.vg.closePath();
			// this.vg.fill();
			this.vg.stroke();
			
			this.vg.end(args.fboOutput);
		}

		public void disposeVideo(GL3 gl) {
		}
	}
	
	public void globalVideoSetup(GL3 gl) {
	}

	public void globalVideoDispose(GL3 gl) {
	}

	public VideoEffectInstance instance() {
		return new TestVFXInstance();
	}

	public void effectMain() {
	}

	public String getEffectClassID() {
		return "urm.test";
	}
}
