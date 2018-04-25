package io.github.nasso.urmusic.model.project.codec.v1_0_0;

import java.io.IOException;
import java.io.InputStream;

import io.github.nasso.urmusic.model.project.Project;

class Decoder {
	static Project load(InputStream in) throws IOException {
		CompositionChunk ch = new CompositionChunk();
		ch.read(in);
		
		return new Project(ch.build());
	}
}
