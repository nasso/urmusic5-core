package io.github.nasso.urmusic.model.project.codec.v1_0_0;

import static io.github.nasso.urmusic.common.DataUtils.*;

import java.io.IOException;
import java.io.OutputStream;

import io.github.nasso.urmusic.common.RGBA32;

class RGBA32BufferChunk extends ArrayBufferChunk<RGBA32> {
	static final int ID = buildBigInt('R', 'G', 'B', 'A');
	
	RGBA32[] values;
	
	public int size() {
		return 8 + this.values.length * 4;
	}

	public void write(OutputStream out) throws IOException {
		writeBigInt(out, ID);
		writeBigInt(out, this.size());
		for(int i = 0; i < this.values.length; i++)
			writeBigInt(out, this.values[i].getRGBA());
	}
	
	public static RGBA32BufferChunk from(RGBA32[] data) {
		RGBA32BufferChunk ch = new RGBA32BufferChunk();
		
		ch.values = data;
		
		return ch;
	}

	public RGBA32[] getData() {
		return this.values;
	}
}
