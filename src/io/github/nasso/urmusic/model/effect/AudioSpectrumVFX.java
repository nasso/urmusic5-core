package io.github.nasso.urmusic.model.effect;

import org.joml.Vector2fc;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import io.github.nasso.urmusic.common.BoolValue;
import io.github.nasso.urmusic.common.FFTContext;
import io.github.nasso.urmusic.common.MathUtils;
import io.github.nasso.urmusic.common.RGBA32;
import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.project.TrackEffect;
import io.github.nasso.urmusic.model.project.VideoEffect;
import io.github.nasso.urmusic.model.project.VideoEffectArgs;
import io.github.nasso.urmusic.model.project.VideoEffectInstance;
import io.github.nasso.urmusic.model.project.param.BooleanParam;
import io.github.nasso.urmusic.model.project.param.FloatParam;
import io.github.nasso.urmusic.model.project.param.IntParam;
import io.github.nasso.urmusic.model.project.param.OptionParam;
import io.github.nasso.urmusic.model.project.param.Point2DParam;
import io.github.nasso.urmusic.model.project.param.RGBA32Param;
import io.github.nasso.urmusic.model.renderer.video.glvg.GLVG;

public class AudioSpectrumVFX extends TrackEffect implements VideoEffect {
	private static final String PNAME_mode = "mode";
	private static final String PNAME_color = "color";
	private static final String PNAME_faceA = "faceA";
	private static final String PNAME_faceB = "faceB";
	private static final String PNAME_polar = "polar";
	private static final String PNAME_startPoint = "startPoint";
	private static final String PNAME_endPoint = "endPoint";
	private static final String PNAME_millisOffset = "millisOffset";
	private static final String PNAME_duration = "duration";
	private static final String PNAME_minDecibel = "minDecibel";
	private static final String PNAME_maxDecibel = "maxDecibel";
	private static final String PNAME_minFreq = "minFreq";
	private static final String PNAME_maxFreq = "maxFreq";
	private static final String PNAME_height = "height";
	private static final String PNAME_minHeight = "minHeight";
	private static final String PNAME_exponent = "exponent";
	private static final String PNAME_size = "size";
	private static final String PNAME_count = "count";
	
	public static final int FFT_SIZE = 1 << 14;
	
	private class AudioSpectrumVFXInstance extends TrackEffectInstance implements VideoEffectInstance {
		private VideoEffectArgs args;
		private int mode;
		private RGBA32 color;
		private Vector2fc startPoint;
		private Vector2fc endPoint;
		private boolean faceA;
		private boolean faceB;
		private boolean polar;
		private float millisOffset;
		private float duration;
		private float minDecibel;
		private float maxDecibel;
		private float minFreq;
		private float maxFreq;
		private float height;
		private float minHeight;
		private float exponent;
		private float size;
		private int count;
		
		private GLVG vg;
		
		private FFTContext fft = new FFTContext(AudioSpectrumVFX.FFT_SIZE);
		private float[] audioData = new float[AudioSpectrumVFX.FFT_SIZE];
		private float[] xy = new float[2];
		
		public void setupParameters() {
			this.addParameter(new OptionParam(PNAME_mode, 1, "outline", "lines", "fill", "dots"));
			this.addParameter(new RGBA32Param(PNAME_color, 0xFFFFFFFF));
			this.addParameter(new BooleanParam(PNAME_faceA, BoolValue.TRUE));
			this.addParameter(new BooleanParam(PNAME_faceB, BoolValue.TRUE));
			this.addParameter(new BooleanParam(PNAME_polar, BoolValue.FALSE));
			this.addParameter(new Point2DParam(PNAME_startPoint, -500, 150));
			this.addParameter(new Point2DParam(PNAME_endPoint, +500, 150));
			this.addParameter(new FloatParam(PNAME_millisOffset, 0.0f, 1.0f));
			this.addParameter(new FloatParam(PNAME_duration, 200.0f, 1.0f, Float.MIN_VALUE, Float.MAX_VALUE));
			this.addParameter(new FloatParam(PNAME_minDecibel, -50.0f, 1.0f));
			this.addParameter(new FloatParam(PNAME_maxDecibel, -20.0f, 1.0f));
			this.addParameter(new FloatParam(PNAME_minFreq, 0.0f, 10.0f, 0.0f, Float.MAX_VALUE));
			this.addParameter(new FloatParam(PNAME_maxFreq, 140.0f, 10.0f, 0.0f, Float.MAX_VALUE));
			this.addParameter(new FloatParam(PNAME_height, 200.0f, 1.0f));
			this.addParameter(new FloatParam(PNAME_minHeight, 2.0f, 1.0f, 0.0f, Float.MAX_VALUE));
			this.addParameter(new FloatParam(PNAME_exponent, 2.0f, 1.0f, 0.0f, Float.MAX_VALUE));
			this.addParameter(new FloatParam(PNAME_size, 2.0f, 1.0f, 0.0f, Float.MAX_VALUE));
			this.addParameter(new IntParam(PNAME_count, 128, 1, 1, Integer.MAX_VALUE));
		}
		
		public void setupVideo(GL3 gl) {
			this.vg = new GLVG(gl);
		}
		
		private void xyToPolar() {
			float a = this.xy[0] / this.args.width * MathUtils.PI_2 + MathUtils.HALF_PI;
			float r = this.xy[1];
			
			this.xy[0] = MathUtils.cosf(a) * r;
			this.xy[1] = MathUtils.sinf(a) * r;
		}
		
		private void traceOutline() {
			float dx = this.endPoint.x() - this.startPoint.x();
			float dy = this.endPoint.y() - this.startPoint.y();
			float distInv = (float) (1.0 / Math.sqrt(dx * dx + dy * dy));
			float expandX = (dy) * distInv * this.height;
			float expandY = (-dx) * distInv * this.height;
			float minExpandX = (dy) * distInv * this.minHeight;
			float minExpandY = (-dx) * distInv * this.minHeight;
			
			this.vg.beginPath();
			if(this.faceA) {
				this.xy[0] = this.startPoint.x();
				this.xy[1] = -this.startPoint.y();
				if(this.polar) this.xyToPolar();
				
				this.vg.moveTo(this.xy[0], this.xy[1]);
				
				for(int i = 0; i < this.count; i++) {
					float p = (float) i / (this.count - 1);
					
					// [0..1] frequency amplitude
					float freqVal = MathUtils.clamp(Math.max((MathUtils.getValue(this.audioData, MathUtils.lerp(this.minFreq, this.maxFreq, p) * this.audioData.length, true) - this.minDecibel), 0.0f) / (this.maxDecibel - this.minDecibel), 0.0f, 1.0f);
					freqVal = MathUtils.powf(freqVal, this.exponent);
					
					this.xy[0] = MathUtils.lerp(this.startPoint.x(), this.endPoint.x(), p) - freqVal * expandX - minExpandX;
					this.xy[1] = -MathUtils.lerp(this.startPoint.y(), this.endPoint.y(), p) + freqVal * expandY + minExpandY;
					if(this.polar) this.xyToPolar();
					
					this.vg.lineTo(this.xy[0], this.xy[1]);
				}
				
				this.xy[0] = this.endPoint.x();
				this.xy[1] = -this.endPoint.y();
				if(this.polar) this.xyToPolar();
				
				this.vg.lineTo(this.xy[0], this.xy[1]);
			}

			if(this.faceB) {
				this.xy[0] = this.startPoint.x();
				this.xy[1] = -this.startPoint.y();
				if(this.polar) this.xyToPolar();
				
				this.vg.moveTo(this.xy[0], this.xy[1]);
				
				for(int i = 0; i < this.count; i++) {
					float p = (float) i / (this.count - 1);
					
					// [0..1] frequency amplitude
					float freqVal = MathUtils.clamp(Math.max((MathUtils.getValue(this.audioData, MathUtils.lerp(this.minFreq, this.maxFreq, p) * this.audioData.length, true) - this.minDecibel), 0.0f) / (this.maxDecibel - this.minDecibel), 0.0f, 1.0f);
					freqVal = MathUtils.powf(freqVal, this.exponent);
					
					this.xy[0] = MathUtils.lerp(this.startPoint.x(), this.endPoint.x(), p) + freqVal * expandX + minExpandX;
					this.xy[1] = -MathUtils.lerp(this.startPoint.y(), this.endPoint.y(), p) - freqVal * expandY - minExpandY;
					if(this.polar) this.xyToPolar();

					this.vg.lineTo(this.xy[0], this.xy[1]);
				}

				this.xy[0] = this.endPoint.x();
				this.xy[1] = -this.endPoint.y();
				if(this.polar) this.xyToPolar();
				
				this.vg.lineTo(this.xy[0], this.xy[1]);
			}
		}
		
		private void renderFill() {
			this.traceOutline();
			this.vg.fill();
		}
		
		private void renderOutline() {
			this.traceOutline();
			this.vg.stroke();
		}
		
		private void renderLines() {
			float dx = this.endPoint.x() - this.startPoint.x();
			float dy = this.endPoint.y() - this.startPoint.y();
			float distInv = (float) (1.0 / Math.sqrt(dx * dx + dy * dy));
			float expandX = (dy) * distInv * this.height;
			float expandY = (-dx) * distInv * this.height;
			float minExpandX = (dy) * distInv * this.minHeight;
			float minExpandY = (-dx) * distInv * this.minHeight;
			
			this.vg.beginPath();
			for(int i = 0; i < this.count; i++) {
				float p = (float) i / (this.count - 1);
				
				// [0..1] frequency amplitude
				float freqVal = MathUtils.clamp(Math.max((MathUtils.getValue(this.audioData, MathUtils.lerp(this.minFreq, this.maxFreq, p) * this.audioData.length, true) - this.minDecibel), 0.0f) / (this.maxDecibel - this.minDecibel), 0.0f, 1.0f);
				freqVal = MathUtils.powf(freqVal, this.exponent);
				
				float sx = MathUtils.lerp(this.startPoint.x(), this.endPoint.x(), p);
				float sy = -MathUtils.lerp(this.startPoint.y(), this.endPoint.y(), p);
				float xa = sx - freqVal * expandX - minExpandX;
				float ya = sy + freqVal * expandY + minExpandY;
				float xb = sx + freqVal * expandX + minExpandX;
				float yb = sy - freqVal * expandY - minExpandY;

				if(this.faceA && this.faceB) {
					this.xy[0] = xa;
					this.xy[1] = ya;
					if(this.polar) this.xyToPolar();
					this.vg.moveTo(this.xy[0], this.xy[1]);
					
					this.xy[0] = xb;
					this.xy[1] = yb;
					if(this.polar) this.xyToPolar();
					this.vg.lineTo(this.xy[0], this.xy[1]);
				} else if(this.faceA) {
					this.xy[0] = sx;
					this.xy[1] = sy;
					if(this.polar) this.xyToPolar();
					this.vg.moveTo(this.xy[0], this.xy[1]);
					
					this.xy[0] = xa;
					this.xy[1] = ya;
					if(this.polar) this.xyToPolar();
					this.vg.lineTo(this.xy[0], this.xy[1]);
				} else if(this.faceB) {
					this.xy[0] = sx;
					this.xy[1] = sy;
					if(this.polar) this.xyToPolar();
					this.vg.moveTo(this.xy[0], this.xy[1]);

					this.xy[0] = xb;
					this.xy[1] = yb;
					if(this.polar) this.xyToPolar();
					this.vg.lineTo(this.xy[0], this.xy[1]);
				}
			}
			this.vg.stroke();
		}
		
		private void renderDots() {
			float dx = this.endPoint.x() - this.startPoint.x();
			float dy = this.endPoint.y() - this.startPoint.y();
			float distInv = (float) (1.0 / Math.sqrt(dx * dx + dy * dy));
			float expandX = (dy) * distInv * this.height;
			float expandY = (-dx) * distInv * this.height;
			float minExpandX = (dy) * distInv * this.minHeight;
			float minExpandY = (-dx) * distInv * this.minHeight;
			
			this.vg.beginPath();
			for(int i = 0; i < this.count; i++) {
				float p = (float) i / (this.count - 1);
				
				// [0..1] frequency amplitude
				float freqVal = MathUtils.clamp(Math.max((MathUtils.getValue(this.audioData, MathUtils.lerp(this.minFreq, this.maxFreq, p) * this.audioData.length, true) - this.minDecibel), 0.0f) / (this.maxDecibel - this.minDecibel), 0.0f, 1.0f);
				freqVal = MathUtils.powf(freqVal, this.exponent);
				
				float sx = MathUtils.lerp(this.startPoint.x(), this.endPoint.x(), p);
				float sy = -MathUtils.lerp(this.startPoint.y(), this.endPoint.y(), p);
				float xa = sx - freqVal * expandX - minExpandX;
				float ya = sy + freqVal * expandY + minExpandY;
				float xb = sx + freqVal * expandX + minExpandX;
				float yb = sy - freqVal * expandY - minExpandY;
				
				if(this.faceA) {
					this.xy[0] = xa;
					this.xy[1] = ya;
					if(this.polar) this.xyToPolar();
					this.vg.moveTo(this.xy[0], this.xy[1]);
					this.vg.oval(this.xy[0], this.xy[1], this.size, this.size);
				}
				
				if(this.faceB) {
					this.xy[0] = xb;
					this.xy[1] = yb;
					if(this.polar) this.xyToPolar();
					this.vg.moveTo(this.xy[0], this.xy[1]);
					this.vg.oval(this.xy[0], this.xy[1], this.size, this.size);
				}
			}
			this.vg.fill();
		}
		
		public void applyVideo(GL3 gl, VideoEffectArgs args) {
			this.args = args;
			this.mode = ((int) args.parameters.get(PNAME_mode));
			this.color = ((RGBA32) args.parameters.get(PNAME_color));
			this.startPoint = ((Vector2fc) args.parameters.get(PNAME_faceA));
			this.endPoint = ((Vector2fc) args.parameters.get(PNAME_faceB));
			this.faceA = args.parameters.get(PNAME_polar) == BoolValue.TRUE;
			this.faceB = args.parameters.get(PNAME_startPoint) == BoolValue.TRUE;
			this.polar = args.parameters.get(PNAME_endPoint) == BoolValue.TRUE;
			this.millisOffset = ((float) args.parameters.get(PNAME_millisOffset));
			this.duration = ((float) args.parameters.get(PNAME_duration));
			this.minDecibel = ((float) args.parameters.get(PNAME_minDecibel));
			this.maxDecibel = ((float) args.parameters.get(PNAME_maxDecibel));
			this.minFreq = ((float) args.parameters.get(PNAME_minFreq)) / UrmusicModel.getAudioRenderer().getSampleRate();
			this.maxFreq = ((float) args.parameters.get(PNAME_maxFreq)) / UrmusicModel.getAudioRenderer().getSampleRate();
			this.height = ((float) args.parameters.get(PNAME_height));
			this.minHeight = ((float) args.parameters.get(PNAME_minHeight));
			this.exponent = ((float) args.parameters.get(PNAME_exponent));
			this.size = ((float) args.parameters.get(PNAME_size));
			this.count = ((int) args.parameters.get(PNAME_count));
			
			if(!this.faceA && !this.faceB) {
				args.cancelled = true;
				return;
			}
			
			{ // In a block so "min" and "max" aren't annoying later if we need to name vars like that
				float min = Math.min(this.minDecibel, this.maxDecibel);
				float max = Math.max(this.minDecibel, this.maxDecibel);
				
				this.minDecibel = min;
				this.maxDecibel = max;
			}
			
			UrmusicModel.getAudioRenderer().getSamples(args.time + this.millisOffset / 1000.0f, this.duration / 1000.0f, this.audioData);
			MathUtils.applyBlackmanWindow(this.audioData, this.audioData.length);
			this.fft.fft(this.audioData, true);
			
			gl.glClear(GL.GL_COLOR_BUFFER_BIT);
			
			this.vg.begin(gl, args.width, args.height);
			this.vg.setLineWidth(this.size);
			this.vg.setStrokeColor(this.color.getRGBA());
			this.vg.setFillColor(this.color.getRGBA());
			
			switch(this.mode) {
				case 0: // OUTLINE
					this.renderOutline();
					break;
				case 2: // FILL
					this.renderFill();
					break;
				case 1: // LINES
					this.renderLines();
					break;
				case 3: // DOTS
					this.renderDots();
					break;
			}
			
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
		return new AudioSpectrumVFXInstance();
	}

	public void effectMain() {
	}

	public String getEffectClassID() {
		return "urm.audio_spectrum";
	}
}
