package io.github.nasso.urmusic.model.project.codec.v1_0_0;

import static io.github.nasso.urmusic.common.DataUtils.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class IntegerBufferChunk extends ArrayBufferChunk<Integer> {
	static final int ID = buildBigInt('I', 'N', 'T', '\0');
	
	Integer[] values;
	
	public int size() {
		return 8 + this.values.length * 4;
	}

	public void write(OutputStream out) throws IOException {
		writeBigInt(out, ID);
		writeBigInt(out, this.size());
		for(int i = 0; i < this.values.length; i++)
			writeBigInt(out, this.values[i]);
	}

	public void read(InputStream in) throws IOException {
		Chunk.assertInt(in, ID);
		int size = readBigInt(in); size -= 8;
		
		int len = size / 4;
		this.values = new Integer[len];
		
		for(int i = 0; i < len; i++) {
			this.values[i] = readBigInt(in); size -= 4;
		}
		
		Chunk.assertZero(size);
	}
	
	public static IntegerBufferChunk from(Integer[] data) {
		IntegerBufferChunk ch = new IntegerBufferChunk();
		
		ch.values = data;
		
		return ch;
	}

	public Integer[] getData() {
		return this.values;
	}
}
