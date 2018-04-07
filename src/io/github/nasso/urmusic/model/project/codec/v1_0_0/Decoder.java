package io.github.nasso.urmusic.model.project.codec.v1_0_0;

import java.io.IOException;
import java.io.InputStream;

import io.github.nasso.urmusic.model.project.Composition;
import io.github.nasso.urmusic.model.project.Project;

class Decoder {
	static Project load(InputStream in) throws IOException {
		Composition comp = new Composition();
		
		return new Project(comp);
	}
}
