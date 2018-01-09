package io.github.nasso.urmusic.view.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.github.nasso.urmusic.utils.DataUtils;

public class UrmusicViewStateCodec {
	private UrmusicViewStateCodec() { }
	
	public static void writeState(UrmusicViewState state, OutputStream out) throws IOException {
		DataUtils.writeBigInt(out, state.getFrameCount());
	}
	
	public static void readState(UrmusicViewState state, InputStream in) throws IOException {
		state.setFrameCount(DataUtils.readBigInt(in));
	}
}
