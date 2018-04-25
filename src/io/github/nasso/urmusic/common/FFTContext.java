package io.github.nasso.urmusic.common;

import org.jtransforms.fft.FloatFFT_1D;

public class FFTContext {
	private final int size;
	private final FloatFFT_1D fft;
	
	public FFTContext(int fftSize) {
		this.size = fftSize;
		this.fft = new FloatFFT_1D(fftSize);
	}
	
	public void fft(float[] data, boolean convertToDb) {
		this.fft.realForward(data);
		
		for(int i = 0, l = data.length / 2; i < l; i++)
			data[data.length - 1 - i] = data[i] = (float) Math.hypot(data[i * 2], data[i * 2 + 1]);
		
		if(convertToDb) {
			for(int i = 0; i < data.length; i++) {
				data[i] = data[i] == 0 ? -Float.MAX_VALUE : (float) (20.0 * Math.log10(Math.abs(data[i] / this.size)));
			}
		}
	}
}
