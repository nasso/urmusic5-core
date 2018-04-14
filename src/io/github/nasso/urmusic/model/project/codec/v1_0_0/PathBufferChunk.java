package io.github.nasso.urmusic.model.project.codec.v1_0_0;

import static io.github.nasso.urmusic.common.DataUtils.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

class PathBufferChunk extends ArrayBufferChunk<Path> {
	static final int ID = buildBigInt('P', 'A', 'T', 'H');
	
	Path[] values;
	
	public int size() {
		int size =
			+ 8 // header
			+ 4; // values.length
		
		for(int i = 0; i < this.values.length; i++) {
			size += 4 + this.values[i].toString().getBytes().length;
		}
		
		return size;
	}

	public void write(OutputStream out) throws IOException {
		writeBigInt(out, ID);
		writeBigInt(out, this.size());
		
		writeBigInt(out, this.values.length);
		for(int i = 0; i < this.values.length; i++) {
			byte[] str = this.values[i].toString().getBytes();
			writeBigInt(out, str.length);
			out.write(str);
		}
	}
	
	public static PathBufferChunk from(Path[] data) {
		PathBufferChunk ch = new PathBufferChunk();
		
		ch.values = data;
		
		return ch;
	}

	public Path[] getData() {
		return this.values;
	}
}
