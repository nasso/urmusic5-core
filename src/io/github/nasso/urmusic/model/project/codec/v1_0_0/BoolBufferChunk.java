package io.github.nasso.urmusic.model.project.codec.v1_0_0;

import static io.github.nasso.urmusic.common.DataUtils.*;

import java.io.IOException;
import java.io.OutputStream;

import io.github.nasso.urmusic.common.BoolValue;

public class BoolBufferChunk extends ArrayBufferChunk<BoolValue> {
	static final int ID = buildBigInt('B', 'O', 'O', 'L');
	
	BoolValue[] values;
	
	public int size() {
		return 8 + this.values.length;
	}

	public void write(OutputStream out) throws IOException {
		writeBigInt(out, ID);
		writeBigInt(out, this.size());
		for(int i = 0; i < this.values.length; i++)
			out.write(this.values[i] == BoolValue.TRUE ? 1 : 0);
	}
	
	public static BoolBufferChunk from(BoolValue[] data) {
		BoolBufferChunk ch = new BoolBufferChunk();
		
		ch.values = data;
		
		return ch;
	}

	public BoolValue[] getData() {
		return this.values;
	}
}
