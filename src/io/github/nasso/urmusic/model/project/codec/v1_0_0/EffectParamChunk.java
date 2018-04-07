package io.github.nasso.urmusic.model.project.codec.v1_0_0;

import static io.github.nasso.urmusic.common.DataUtils.*;

import java.io.IOException;
import java.io.OutputStream;

import io.github.nasso.urmusic.model.project.param.EffectParam;

public class EffectParamChunk<T> implements Chunk {
	static final int ID = buildBigInt('X', 'P', 'A', 'R');
	
	StringChunk id;
	KeyFramesChunk<T> keyFrames;
	
	public int size() {
		return
			+ 8 // header
			+ this.id.size()
			+ this.keyFrames.size();
	}
	
	public void write(OutputStream out) throws IOException {
		writeBigInt(out, ID);
		writeBigInt(out, this.size());
		this.id.write(out);
		this.keyFrames.write(out); 
		// first keyframe is a fake one giving the constant value
		// so when there's only 1 keyframe (written), it means there's no actual key frame for the effect, and the only value written is the constant
	}
	
	public static <T> EffectParamChunk<T> from(EffectParam<T> param) {
		EffectParamChunk<T> ch = new EffectParamChunk<>();

		ch.id = StringChunk.from(param.getID());
		ch.keyFrames = KeyFramesChunk.from(param.getValue(0), param.getKeyFrames());
		
		return ch;
	}
}
