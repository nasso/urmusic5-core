package io.github.nasso.urmusic.common;

import org.jtransforms.fft.FloatFFT_1D;

public class FFTContext {
	private FloatFFT_1D fft;
	private float[] altBuffer;
	
	public FFTContext(int fftSize) {
		this.fft = new FloatFFT_1D(fftSize);
		this.altBuffer = new float[fftSize];
	}
	
	public int size() {
		return this.altBuffer.length;
	}
	
	public float[] fft(float[] in, float[] out) {
		System.arraycopy(in, 0, this.altBuffer, 0, in.length);
		this.fft.realForward(this.altBuffer);
		
		for(int i = 0, l = this.altBuffer.length / 2; i < l; i++) {
			out[i] = (float) Math.hypot(this.altBuffer[i * 2], this.altBuffer[i * 2 + 1]);
		}
		
		return out;
	}
}
