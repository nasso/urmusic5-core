package io.github.nasso.urmusic.model.project.codec.v1_0_0;

import static io.github.nasso.urmusic.common.DataUtils.*;

import java.io.IOException;
import java.io.OutputStream;

class FloatBufferChunk extends ArrayBufferChunk<Float> {
	static final int ID = buildBigInt('F', 'L', 'T', '\0');
	
	Float[] values;
	
	public int size() {
		return 8 + this.values.length * 4;
	}

	public void write(OutputStream out) throws IOException {
		writeBigInt(out, ID);
		writeBigInt(out, this.size());
		for(int i = 0; i < this.values.length; i++)
			writeBigInt(out, Float.floatToIntBits(this.values[i]));
	}
	
	public static FloatBufferChunk from(Float[] data) {
		FloatBufferChunk ch = new FloatBufferChunk();
		
		ch.values = data;
		
		return ch;
	}

	public Float[] getData() {
		return this.values;
	}
}
