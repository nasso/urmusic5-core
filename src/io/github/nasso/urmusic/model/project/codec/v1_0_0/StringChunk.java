package io.github.nasso.urmusic.model.project.codec.v1_0_0;

import static io.github.nasso.urmusic.common.DataUtils.*;

import java.io.IOException;
import java.io.OutputStream;

class StringChunk implements Chunk {
	static final int ID = buildBigInt('S', 'T', 'R', 'G');
	
	byte[] data;
	
	public int size() {
		return 8 + this.data.length;
	}
	
	public void write(OutputStream out) throws IOException {
		writeBigInt(out, ID);
		writeBigInt(out, this.size());
		out.write(this.data);
	}
	
	static StringChunk from(String str) {
		StringChunk ch = new StringChunk();
		
		ch.data = str.getBytes();
		
		return ch;
	}
}
