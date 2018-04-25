package io.github.nasso.urmusic.model.project.codec.v1_0_0;

import java.io.IOException;
import java.io.OutputStream;

import io.github.nasso.urmusic.model.project.Project;

class Encoder {
	static void save(Project p, OutputStream out) throws IOException {
		CompositionChunk.from(p.getMainComposition()).write(out);
	}
}
