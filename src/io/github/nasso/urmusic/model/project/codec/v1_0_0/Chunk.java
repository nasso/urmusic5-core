package io.github.nasso.urmusic.model.project.codec.v1_0_0;

import java.io.IOException;
import java.io.OutputStream;

interface Chunk {
	int size();
	void write(OutputStream out) throws IOException;
}
