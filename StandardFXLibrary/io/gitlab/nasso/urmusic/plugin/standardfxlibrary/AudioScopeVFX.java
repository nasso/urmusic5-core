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

import io.gitlab.nasso.urmusic.common.MathUtils;
import io.gitlab.nasso.urmusic.model.UrmusicModel;
import io.gitlab.nasso.urmusic.model.project.TrackEffect;
import io.gitlab.nasso.urmusic.model.project.VideoEffect;
import io.gitlab.nasso.urmusic.model.project.VideoEffectArgs;
import io.gitlab.nasso.urmusic.model.project.VideoEffectInstance;
import io.gitlab.nasso.urmusic.model.project.param.FloatParam;
import io.gitlab.nasso.urmusic.model.project.param.IntParam;
import io.gitlab.nasso.urmusic.model.project.param.Point2DParam;
import io.gitlab.nasso.urmusic.model.renderer.video.glvg.GLVG;

public class AudioScopeVFX extends TrackEffect implements VideoEffect {
	private static final String PNAME_startPoint = "startPoint";
	private static final String PNAME_endPoint = "endPoint";
	private static final String PNAME_millisOffset = "millisOffset";
	private static final String PNAME_duration = "duration";
	private static final String PNAME_lineWidth = "lineWidth";
	private static final String PNAME_precision = "precision";
	
	private class AudioScopeVFXInstance extends TrackEffectInstance implements VideoEffectInstance {
		private GLVG vg;
		
		private float[] audioData = new float[512];
		
		public void setupParameters() {
			this.addParameter(new Point2DParam(PNAME_startPoint, -400, 0));
			this.addParameter(new Point2DParam(PNAME_endPoint, +400, 0));
			this.addParameter(new FloatParam(PNAME_millisOffset, 0.0f, 1.0f));
			this.addParameter(new FloatParam(PNAME_duration, 200.0f, 1.0f));
			this.addParameter(new FloatParam(PNAME_lineWidth, 2.0f, 1.0f, 0.0f, Float.MAX_VALUE));
			this.addParameter(new IntParam(PNAME_precision, 512, 1, 2, 32768));
		}
		
		public void setupVideo(GL3 gl) {
			this.vg = new GLVG(gl);
		}
		
		public void applyVideo(GL3 gl, VideoEffectArgs args) {
			Vector2fc startPoint = (Vector2fc) args.parameters.get(PNAME_startPoint);
			Vector2fc endPoint = (Vector2fc) args.parameters.get(PNAME_endPoint);
			float millisOffset = (float) args.parameters.get(PNAME_millisOffset);
			float duration = (float) args.parameters.get(PNAME_duration);
			float lineWidth = (float) args.parameters.get(PNAME_lineWidth);
			int precision = (int) args.parameters.get(PNAME_precision);
			
			if(precision != this.audioData.length)
				this.audioData = new float[precision];
			
			UrmusicModel.getAudioRenderer().getSamples(args.time + millisOffset / 1000.0f, duration / 1000.0f, this.audioData);
			
			gl.glClear(GL.GL_COLOR_BUFFER_BIT);
			
			this.vg.begin(gl, args.width, args.height);
			this.vg.setLineWidth(lineWidth);
			this.vg.setStrokeColor(0xFFFFFFFF);
			
			this.vg.beginPath();
			for(int i = 0; i < precision; i++) {
				float p = (float) i / precision;
				
				float x = MathUtils.lerp(startPoint.x(), endPoint.x(), p);
				float y = MathUtils.lerp(startPoint.y(), endPoint.y(), p) + this.audioData[i] * 200;
				
				this.vg.lineTo(x, y);
			}
			this.vg.stroke();
			
			this.vg.end(args.fboOutput);
		}

		public void disposeVideo(GL3 gl) {
			this.vg.dispose(gl);
		}
	}
	
	public void globalVideoSetup(GL3 gl) {
	}

	public void globalVideoDispose(GL3 gl) {
	}

	public TrackEffectInstance instance() {
		return new AudioScopeVFXInstance();
	}

	public void effectMain() {
	}

	public String getEffectClassID() {
		return "audio_scope";
	}
}
