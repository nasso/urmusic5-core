package io.github.nasso.urmusic.model.effect;

import org.joml.Vector2fc;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import io.github.nasso.urmusic.common.FFTContext;
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

public class AudioSpectrumVFX extends TrackEffect implements VideoEffect {
	public static final int FFT_SIZE = 16384;
	
	private class AudioSpectrumVFXInstance extends TrackEffectInstance implements VideoEffectInstance {
		private Point2DParam startPoint = new Point2DParam("startPoint", -400, 0);
		private Point2DParam endPoint = new Point2DParam("endPoint", +400, 0);
		private FloatParam millisOffset = new FloatParam("millisOffset", 0.0f, 1.0f);
		private FloatParam duration = new FloatParam("duration", 200.0f, 1.0f, Float.MIN_VALUE, Float.MAX_VALUE);
		private FloatParam minDecibel = new FloatParam("minDecibel", -50.0f, 1.0f);
		private FloatParam maxDecibel = new FloatParam("maxDecibel", -20.0f, 1.0f);
		private FloatParam minFreq = new FloatParam("minFreq", 0.0f, 0.1f, 0.0f, 100.0f); // %
		private FloatParam maxFreq = new FloatParam("maxFreq", 0.5f, 0.1f, 0.0f, 100.0f); // %
		private FloatParam height = new FloatParam("height", 200.0f, 1.0f);
		private FloatParam exponent = new FloatParam("exponent", 2.0f, 1.0f, 0.0f, Float.MAX_VALUE);
		private FloatParam lineWidth = new FloatParam("lineWidth", 2.0f, 1.0f, 0.0f, Float.MAX_VALUE);
		private IntParam lineCount = new IntParam("lineCount", 256, 1, 1, Integer.MAX_VALUE);

		private GLVG vg;
		
		private FFTContext fft = new FFTContext(AudioSpectrumVFX.FFT_SIZE);
		private float[] audioData = new float[AudioSpectrumVFX.FFT_SIZE];
		
		public AudioSpectrumVFXInstance() {
			this.addParameter(this.startPoint);
			this.addParameter(this.endPoint);
			this.addParameter(this.millisOffset);
			this.addParameter(this.duration);
			this.addParameter(this.minDecibel);
			this.addParameter(this.maxDecibel);
			this.addParameter(this.minFreq);
			this.addParameter(this.maxFreq);
			this.addParameter(this.height);
			this.addParameter(this.exponent);
			this.addParameter(this.lineWidth);
			this.addParameter(this.lineCount);
		}
		
		public void setupVideo(GL3 gl) {
			this.vg = new GLVG(gl);
		}
		
		public void applyVideo(GL3 gl, VideoEffectArgs args) {
			Vector2fc startPoint = this.startPoint.getValue(args.time);
			Vector2fc endPoint = this.endPoint.getValue(args.time);
			float millisOffset = this.millisOffset.getValue(args.time);
			float duration = this.duration.getValue(args.time);
			float minDecibel = this.minDecibel.getValue(args.time);
			float maxDecibel = this.maxDecibel.getValue(args.time);
			float minFreq = this.minFreq.getValue(args.time) / 100.0f; // %
			float maxFreq = this.maxFreq.getValue(args.time) / 100.0f; // %
			float height = this.height.getValue(args.time);
			float exponent = this.exponent.getValue(args.time);
			float lineWidth = this.lineWidth.getValue(args.time);
			int lineCount = this.lineCount.getValue(args.time);
			
			{ // In a block so "min" and "max" aren't annoying later if we need to name vars like that
				float min = Math.min(minDecibel, maxDecibel);
				float max = Math.max(minDecibel, maxDecibel);
				
				minDecibel = min;
				maxDecibel = max;
			}
			
			float dx = endPoint.x() - startPoint.x();
			float dy = endPoint.y() - startPoint.y();
			float distInv = (float) (1.0 / Math.sqrt(dx * dx + dy * dy));
			float expandX = (endPoint.y() - startPoint.y()) * distInv * height;
			float expandY = (startPoint.x() - endPoint.x()) * distInv * height;
			
			UrmusicModel.getAudioRenderer().getSamples(args.time + millisOffset / 1000.0f, duration / 1000.0f, this.audioData);
			MathUtils.applyBlackmanWindow(this.audioData, this.audioData.length);
			this.fft.fft(this.audioData, true);
			
			gl.glClear(GL.GL_COLOR_BUFFER_BIT);
			
			this.vg.begin(gl, args.width, args.height);
			this.vg.setLineWidth(lineWidth);
			this.vg.setFillColor(0xFFFFFFFF);
			
			this.vg.beginPath();
			this.vg.moveTo(startPoint.x(), -startPoint.y());
			for(int i = 0; i < lineCount; i++) {
				float p = (float) i / (lineCount - 1);
				
				// [0..1] frequency amplitude
				float freqVal = MathUtils.clamp(Math.max((MathUtils.getValue(this.audioData, MathUtils.lerp(minFreq, maxFreq, p) * this.audioData.length, true) - minDecibel), 0.0f) / (maxDecibel - minDecibel), 0.0f, 1.0f);
				freqVal = (float) Math.pow(freqVal, exponent);
				
				float x = MathUtils.lerp(startPoint.x(), endPoint.x(), p) - freqVal * expandX;
				float y = -MathUtils.lerp(startPoint.y(), endPoint.y(), p) + freqVal * expandY;
				
				this.vg.lineTo(x, y);
			}
			this.vg.lineTo(endPoint.x(), -endPoint.y());
			this.vg.fill();
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
		return new AudioSpectrumVFXInstance();
	}

	public void effectMain() {
	}

	public String getEffectClassName() {
		return "audio_spectrum";
	}
}
