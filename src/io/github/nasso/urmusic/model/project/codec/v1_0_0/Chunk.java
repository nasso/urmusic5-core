package io.github.nasso.urmusic.model.project.codec.v1_0_0;

import static io.github.nasso.urmusic.common.DataUtils.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

interface Chunk {
	int size();
	void write(OutputStream out) throws IOException;
	void read(InputStream in) throws IOException;
	
	static void assertInt(InputStream in, int val) throws IOException {
		if(readBigInt(in) != val) throw new IOException("Unexpected int value. Invalid file.");
	}
	
	static void assertZero(int a) throws IOException {
		if(a != 0) throw new IOException("Unexpected non-zero value. Invalid file.");
	}
}
