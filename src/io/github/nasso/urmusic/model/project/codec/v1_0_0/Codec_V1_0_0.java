package io.github.nasso.urmusic.model.project.codec.v1_0_0;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.github.nasso.urmusic.model.project.Project;

public class Codec_V1_0_0 {
	public static void save(Project p, OutputStream out) throws IOException {
		Encoder.save(p, out);
	}
	
	public static Project load(InputStream in) throws IOException {
		return Decoder.load(in);
	}
}
