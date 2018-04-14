package io.github.nasso.urmusic.model.project.codec;

import static io.github.nasso.urmusic.common.DataUtils.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.github.nasso.urmusic.model.project.Project;
import io.github.nasso.urmusic.model.project.codec.v1_0_0.Codec_V1_0_0;

public class ProjectCodec {
	public static final String FILE_EXT = "urmx";
	
	public static enum Version {
		// BEWARE THAT THE ORDER AND INDEX OF DECLARATION FOR EACH VALUE IS FIXED AND SHOULDN'T BE CHANGED
		V1_0_0
	}
	
	private static final int FORMAT_SIGNATURE = buildBigInt('U', 'R', 'M', 'X');
	
	private ProjectCodec() {}

	public static void save(Project p, OutputStream out) throws IOException {
		writeBigInt(out, FORMAT_SIGNATURE);
		writeBigInt(out, Version.V1_0_0.ordinal());
		
		Codec_V1_0_0.save(p, out);
	}
	
	public static Project load(InputStream in) throws IOException {
		if(readBigInt(in) != FORMAT_SIGNATURE) throw new IOException("Not a project file.");
		
		Version v = Version.values()[readBigInt(in)];
		
		switch(v) {
			case V1_0_0:
				return Codec_V1_0_0.load(in);
			default:
				throw new IOException("Unsupported format version: " + v + ".");
		}
	}
}
