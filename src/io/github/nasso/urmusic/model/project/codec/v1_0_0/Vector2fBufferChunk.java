package io.github.nasso.urmusic.model.project.codec.v1_0_0;

import static io.github.nasso.urmusic.common.DataUtils.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.joml.Vector2f;
import org.joml.Vector2fc;

class Vector2fBufferChunk extends ArrayBufferChunk<Vector2fc> {
	static final int ID = buildBigInt('V', 'E', 'C', '2');
	
	Vector2fc[] values;
	
	public int size() {
		return 8 + this.values.length * 2 * 4;
	}

	public void write(OutputStream out) throws IOException {
		writeBigInt(out, ID);
		writeBigInt(out, this.size());
		
		for(int i = 0; i < this.values.length; i++) {
			writeBigInt(out, Float.floatToIntBits(this.values[i].x()));
			writeBigInt(out, Float.floatToIntBits(this.values[i].y()));
		}
	}

	public void read(InputStream in) throws IOException {
		Chunk.assertInt(in, ID);
		int size = readBigInt(in); size -= 8;
		
		int len = size / 8;
		this.values = new Vector2fc[len];
		for(int i = 0; i < len; i++) {
			this.values[i] = new Vector2f(
				Float.intBitsToFloat(readBigInt(in)),
				Float.intBitsToFloat(readBigInt(in))
			);
			
			size -= 8;
		}
		
		Chunk.assertZero(size);
	}
	
	public static Vector2fBufferChunk from(Vector2fc[] data) {
		Vector2fBufferChunk ch = new Vector2fBufferChunk();
		
		ch.values = data;
		
		return ch;
	}

	public Vector2fc[] getData() {
		return this.values;
	}
}
