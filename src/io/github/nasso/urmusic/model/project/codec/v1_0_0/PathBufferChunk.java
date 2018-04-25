package io.github.nasso.urmusic.model.project.codec.v1_0_0;

import static io.github.nasso.urmusic.common.DataUtils.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

class PathBufferChunk extends ArrayBufferChunk<Path> {
	static final int ID = buildBigInt('P', 'A', 'T', 'H');
	
	Path[] values;
	byte[][] data;
	
	public int size() {
		int size =
			+ 8 // header
			+ 4; // values.length
		
		for(int i = 0; i < this.data.length; i++) {
			size += 4 + this.data[i].length;
		}
		
		return size;
	}

	public void write(OutputStream out) throws IOException {
		writeBigInt(out, ID);
		writeBigInt(out, this.size());
		
		writeBigInt(out, this.data.length);
		for(int i = 0; i < this.data.length; i++) {
			writeBigInt(out, this.data[i].length);
			out.write(this.data[i]);
		}
	}

	public void read(InputStream in) throws IOException {
		Chunk.assertInt(in, ID);
		int size = readBigInt(in); size -= 8;
		
		int len = readBigInt(in); size -= 4;
		this.data = new byte[len][];
		this.values = new Path[len];
		for(int i = 0; i < len; i++) {
			int dataLen = readBigInt(in); size -= 4;
			
			size -= in.read(this.data[i] = new byte[dataLen]);
			this.values[i] = Paths.get(new String(this.data[i], StandardCharsets.UTF_16BE));
		}
		
		Chunk.assertZero(size);
	}
	
	public static PathBufferChunk from(Path[] data) {
		PathBufferChunk ch = new PathBufferChunk();
		
		ch.values = data;
		
		ch.data = new byte[ch.values.length][];
		for(int i = 0; i < ch.values.length; i++)
			ch.data[i] = ch.values[i].toAbsolutePath().toString().getBytes(StandardCharsets.UTF_16BE);
		
		return ch;
	}

	public Path[] getData() {
		return this.values;
	}
}
