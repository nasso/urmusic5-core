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
	public static final int FFT_SIZE = 1 << 14;
	
	private class AudioSpectrumVFXInstance extends TrackEffectInstance implements VideoEffectInstance {
		private OptionParam pMode = new OptionParam("mode", 1, "outline", "lines", "fill", "dots");
		private RGBA32Param pColor = new RGBA32Param("color", 0xFFFFFFFF);
		private BooleanParam pFaceA = new BooleanParam("faceA", BoolValue.TRUE);
		private BooleanParam pFaceB = new BooleanParam("faceB", BoolValue.TRUE);
		private BooleanParam pPolar = new BooleanParam("polar", BoolValue.FALSE);
		private Point2DParam pStartPoint = new Point2DParam("startPoint", -500, 150);
		private Point2DParam pEndPoint = new Point2DParam("endPoint", +500, 150);
		private FloatParam pMillisOffset = new FloatParam("millisOffset", 0.0f, 1.0f);
		private FloatParam pDuration = new FloatParam("duration", 200.0f, 1.0f, Float.MIN_VALUE, Float.MAX_VALUE);
		private FloatParam pMinDecibel = new FloatParam("minDecibel", -50.0f, 1.0f);
		private FloatParam pMaxDecibel = new FloatParam("maxDecibel", -20.0f, 1.0f);
		private FloatParam pMinFreq = new FloatParam("minFreq", 0.0f, 10.0f, 0.0f, Float.MAX_VALUE);
		private FloatParam pMaxFreq = new FloatParam("maxFreq", 140.0f, 10.0f, 0.0f, Float.MAX_VALUE);
		private FloatParam pHeight = new FloatParam("height", 200.0f, 1.0f);
		private FloatParam pMinHeight = new FloatParam("minHeight", 2.0f, 1.0f, 0.0f, Float.MAX_VALUE);
		private FloatParam pExponent = new FloatParam("exponent", 2.0f, 1.0f, 0.0f, Float.MAX_VALUE);
		private FloatParam pSize = new FloatParam("size", 2.0f, 1.0f, 0.0f, Float.MAX_VALUE);
		private IntParam pCount = new IntParam("count", 128, 1, 1, Integer.MAX_VALUE);
		
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
		
		public AudioSpectrumVFXInstance() {
			this.addParameter(this.pMode);
			this.addParameter(this.pColor);
			this.addParameter(this.pFaceA);
			this.addParameter(this.pFaceB);
			this.addParameter(this.pPolar);
			this.addParameter(this.pStartPoint);
			this.addParameter(this.pEndPoint);
			this.addParameter(this.pMillisOffset);
			this.addParameter(this.pDuration);
			this.addParameter(this.pMinDecibel);
			this.addParameter(this.pMaxDecibel);
			this.addParameter(this.pMinFreq);
			this.addParameter(this.pMaxFreq);
			this.addParameter(this.pHeight);
			this.addParameter(this.pMinHeight);
			this.addParameter(this.pExponent);
			this.addParameter(this.pSize);
			this.addParameter(this.pCount);
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
			this.mode = this.pMode.getValue(args.time);
			this.color = this.pColor.getValue(args.time);
			this.startPoint = this.pStartPoint.getValue(args.time);
			this.endPoint = this.pEndPoint.getValue(args.time);
			this.faceA = this.pFaceA.getValue(args.time) == BoolValue.TRUE;
			this.faceB = this.pFaceB.getValue(args.time) == BoolValue.TRUE;
			this.polar = this.pPolar.getValue(args.time) == BoolValue.TRUE;
			this.millisOffset = this.pMillisOffset.getValue(args.time);
			this.duration = this.pDuration.getValue(args.time);
			this.minDecibel = this.pMinDecibel.getValue(args.time);
			this.maxDecibel = this.pMaxDecibel.getValue(args.time);
			this.minFreq = this.pMinFreq.getValue(args.time) / UrmusicModel.getAudioRenderer().getSampleRate();
			this.maxFreq = this.pMaxFreq.getValue(args.time) / UrmusicModel.getAudioRenderer().getSampleRate();
			this.height = this.pHeight.getValue(args.time);
			this.minHeight = this.pMinHeight.getValue(args.time);
			this.exponent = this.pExponent.getValue(args.time);
			this.size = this.pSize.getValue(args.time);
			this.count = this.pCount.getValue(args.time);
			
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

	public String getEffectClassName() {
		return "audio_spectrum";
	}
}
