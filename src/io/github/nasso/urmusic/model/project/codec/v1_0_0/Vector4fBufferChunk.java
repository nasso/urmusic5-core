package io.github.nasso.urmusic.model.project.codec.v1_0_0;

import static io.github.nasso.urmusic.common.DataUtils.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.joml.Vector4f;
import org.joml.Vector4fc;

class Vector4fBufferChunk extends ArrayBufferChunk<Vector4fc> {
	static final int ID = buildBigInt('V', 'E', 'C', '4');
	
	Vector4fc[] values;
	
	public int size() {
		return 8 + this.values.length * 4 * 4;
	}

	public void write(OutputStream out) throws IOException {
		writeBigInt(out, ID);
		writeBigInt(out, this.size());
		for(int i = 0; i < this.values.length; i++) {
			writeBigInt(out, Float.floatToIntBits(this.values[i].x()));
			writeBigInt(out, Float.floatToIntBits(this.values[i].y()));
			writeBigInt(out, Float.floatToIntBits(this.values[i].z()));
			writeBigInt(out, Float.floatToIntBits(this.values[i].w()));
		}
	}

	public void read(InputStream in) throws IOException {
		Chunk.assertInt(in, ID);
		int size = readBigInt(in); size -= 8;
		
		int len = size / 16;
		this.values = new Vector4fc[len];
		for(int i = 0; i < len; i++) {
			this.values[i] = new Vector4f(
				Float.intBitsToFloat(readBigInt(in)),
				Float.intBitsToFloat(readBigInt(in)),
				Float.intBitsToFloat(readBigInt(in)),
				Float.intBitsToFloat(readBigInt(in))
			);
			
			size -= 16;
		}
		
		Chunk.assertZero(size);
	}
	
	public static Vector4fBufferChunk from(Vector4fc[] data) {
		Vector4fBufferChunk ch = new Vector4fBufferChunk();
		
		ch.values = data;
		
		return ch;
	}

	public Vector4fc[] getData() {
		return this.values;
	}
}
