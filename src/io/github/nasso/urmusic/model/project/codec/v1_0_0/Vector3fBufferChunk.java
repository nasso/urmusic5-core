package io.github.nasso.urmusic.model.project.codec.v1_0_0;

import static io.github.nasso.urmusic.common.DataUtils.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.joml.Vector3f;
import org.joml.Vector3fc;

class Vector3fBufferChunk extends ArrayBufferChunk<Vector3fc> {
	static final int ID = buildBigInt('V', 'E', 'C', '3');
	
	Vector3fc[] values;
	
	public int size() {
		return 8 + this.values.length * 3 * 4;
	}

	public void write(OutputStream out) throws IOException {
		writeBigInt(out, ID);
		writeBigInt(out, this.size());
		for(int i = 0; i < this.values.length; i++) {
			writeBigInt(out, Float.floatToIntBits(this.values[i].x()));
			writeBigInt(out, Float.floatToIntBits(this.values[i].y()));
			writeBigInt(out, Float.floatToIntBits(this.values[i].z()));
		}
	}

	public void read(InputStream in) throws IOException {
		Chunk.assertInt(in, ID);
		int size = readBigInt(in); size -= 8;
		
		int len = size / 12;
		this.values = new Vector3fc[len];
		for(int i = 0; i < len; i++) {
			this.values[i] = new Vector3f(
				Float.intBitsToFloat(readBigInt(in)),
				Float.intBitsToFloat(readBigInt(in)),
				Float.intBitsToFloat(readBigInt(in))
			);
			
			size -= 12;
		}
		
		Chunk.assertZero(size);
	}
	
	public static Vector3fBufferChunk from(Vector3fc[] data) {
		Vector3fBufferChunk ch = new Vector3fBufferChunk();
		
		ch.values = data;
		
		return ch;
	}

	public Vector3fc[] getData() {
		return this.values;
	}
}
