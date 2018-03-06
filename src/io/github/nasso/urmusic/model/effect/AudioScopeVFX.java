package io.github.nasso.urmusic.model.effect;

import org.joml.Vector2fc;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import io.github.nasso.urmusic.common.MathUtils;
import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.project.TrackEffect;
import io.github.nasso.urmusic.model.project.VideoEffect;
import io.github.nasso.urmusic.model.project.VideoEffectArgs;
import io.github.nasso.urmusic.model.project.VideoEffectInstance;
import io.github.nasso.urmusic.model.project.param.FloatParam;
import io.github.nasso.urmusic.model.project.param.IntParam;
import io.github.nasso.urmusic.model.project.param.Point2DParam;
import io.github.nasso.urmusic.model.renderer.video.glvg.GLVG;

public class AudioScopeVFX extends TrackEffect implements VideoEffect {
	private class AudioScopeVFXInstance extends TrackEffectInstance implements VideoEffectInstance {
		private Point2DParam startPoint = new Point2DParam("startPoint", -400, 0);
		private Point2DParam endPoint = new Point2DParam("endPoint", +400, 0);
		private FloatParam millisOffset = new FloatParam("millisOffset", 0.0f, 1.0f);
		private FloatParam duration = new FloatParam("duration", 200.0f, 1.0f);
		private FloatParam lineWidth = new FloatParam("lineWidth", 2.0f, 1.0f, 0.0f, Float.MAX_VALUE);
		private IntParam precision = new IntParam("precision", 512, 1, 2, 32768);

		private GLVG vg;
		
		private float[] audioData = new float[512];
		
		public AudioScopeVFXInstance() {
			this.addParameter(this.startPoint);
			this.addParameter(this.endPoint);
			this.addParameter(this.millisOffset);
			this.addParameter(this.duration);
			this.addParameter(this.lineWidth);
			this.addParameter(this.precision);
		}
		
		public void setupVideo(GL3 gl) {
			this.vg = new GLVG(gl);
		}
		
		public void applyVideo(GL3 gl, VideoEffectArgs args) {
			Vector2fc startPoint = this.startPoint.getValue(args.time);
			Vector2fc endPoint = this.endPoint.getValue(args.time);
			float millisOffset = this.millisOffset.getValue(args.time);
			float duration = this.duration.getValue(args.time);
			float lineWidth = this.lineWidth.getValue(args.time);
			int precision = this.precision.getValue(args.time);
			
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

	public String getEffectClassName() {
		return "audio_scope";
	}
}
