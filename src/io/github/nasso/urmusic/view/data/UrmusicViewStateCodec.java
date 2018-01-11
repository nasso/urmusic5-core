package io.github.nasso.urmusic.view.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.github.nasso.urmusic.utils.DataUtils;

public class UrmusicViewStateCodec {
	public static final int CODEC_VERSION = 0;
	
	private UrmusicViewStateCodec() { }
	
	public static void writeState(UrmusicViewState state, OutputStream out) throws IOException {
		UrmusicSplittedPaneState[] paneStates = state.getPaneStates();
		
		DataUtils.writeBigInt(out, CODEC_VERSION);
		DataUtils.writeBigInt(out, paneStates.length);
		for(int i = 0; i < paneStates.length; i++) {
			paneStates[i].write(out);
		}
	}
	
	public static UrmusicViewState readState(InputStream in) throws IOException {
		int codecVersion = DataUtils.readBigInt(in);
		if(codecVersion != CODEC_VERSION) return null;
		
		int frameCount = DataUtils.readBigInt(in);
		if(frameCount < 0) return null;
		
		UrmusicSplittedPaneState[] paneStates = new UrmusicSplittedPaneState[frameCount];

		for(int i = 0; i < paneStates.length; i++) {
			paneStates[i] = new UrmusicSplittedPaneState();
			paneStates[i].read(in);
		}
		
		return new UrmusicViewState(paneStates);
	}
}
