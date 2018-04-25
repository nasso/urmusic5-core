package io.github.nasso.urmusic.model.project.codec.v1_0_0;

import static io.github.nasso.urmusic.common.DataUtils.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

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

	public void read(InputStream in) throws IOException {
		Chunk.assertInt(in, ID);
		int size = readBigInt(in); size -= 8;
		
		this.data = new byte[size];
		size -= in.read(this.data);
		
		Chunk.assertZero(size);
	}
	
	static StringChunk from(String str) {
		StringChunk ch = new StringChunk();
		
		ch.data = str.getBytes(StandardCharsets.UTF_16BE);
		
		return ch;
	}
	
	public String build() {
		return new String(this.data, StandardCharsets.UTF_16BE);
	}
}
