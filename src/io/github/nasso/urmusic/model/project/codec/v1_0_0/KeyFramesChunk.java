package io.github.nasso.urmusic.model.project.codec.v1_0_0;

import static io.github.nasso.urmusic.common.DataUtils.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import io.github.nasso.urmusic.common.BoolValue;
import io.github.nasso.urmusic.common.RGBA32;
import io.github.nasso.urmusic.model.project.param.KeyFrame;

public class KeyFramesChunk<T> implements Chunk {
	static final int ID = buildBigInt('K', 'F', 'L', 'S');
	
	ArrayBufferChunk<T> values;
	float[] times;
	// TODO: save easing functions
	
	public int size() {
		return
			+ 8 // header
			+ this.values.size()
			+ this.times.length * 4;
	}
	
	public void write(OutputStream out) throws IOException {
		writeBigInt(out, ID);
		writeBigInt(out, this.size());
		this.values.write(out); // this tells the data count, so no need to write times.length
		
		for(int i = 0; i < this.times.length; i++)
			writeBigInt(out, Float.floatToIntBits(this.times[i]));
	}
	
	/**
	 * @param sample Needed to detect the type
	 * @param frames
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> KeyFramesChunk<T> from(T sample, List<KeyFrame<T>> frames) {
		KeyFramesChunk<T> ch = new KeyFramesChunk<>();
		
		Object[] ovalues = new Object[frames.size() + 1];
		ch.times = new float[frames.size() + 1];
		
		ovalues[0] = sample;
		ch.times[0] = 0;
		
		for(int i = 1; i < ovalues.length; i++) {
			KeyFrame<T> kf = frames.get(i - 1);
			
			ovalues[i] = kf.getValue();
			ch.times[i] = kf.getPosition();
		}
		
		ArrayBufferChunk<?> vch = null;
		
		if(sample instanceof BoolValue) { // bool
			BoolValue[] fvalues = new BoolValue[ovalues.length];
			System.arraycopy(ovalues, 0, fvalues, 0, ovalues.length);
			
			vch = BoolBufferChunk.from(fvalues);
		} else if(sample instanceof Float) { // float
			Float[] fvalues = new Float[ovalues.length];
			System.arraycopy(ovalues, 0, fvalues, 0, ovalues.length);
			
			vch = FloatBufferChunk.from(fvalues);
		} else if(sample instanceof Integer) { // int
			Integer[] fvalues = new Integer[ovalues.length];
			System.arraycopy(ovalues, 0, fvalues, 0, ovalues.length);
			
			vch = IntegerBufferChunk.from(fvalues);
		} else if(sample instanceof RGBA32) { // rgba32 (int)
			RGBA32[] fvalues = new RGBA32[ovalues.length];
			System.arraycopy(ovalues, 0, fvalues, 0, ovalues.length);
			
			vch = RGBA32BufferChunk.from(fvalues);
		} else if(sample instanceof Vector2f) { // vec2
			Vector2f[] fvalues = new Vector2f[ovalues.length];
			System.arraycopy(ovalues, 0, fvalues, 0, ovalues.length);
			
			vch = Vector2fBufferChunk.from(fvalues);
		} else if(sample instanceof Vector4f) { // vec4
			Vector4fc[] fvalues = new Vector4fc[ovalues.length];
			System.arraycopy(ovalues, 0, fvalues, 0, ovalues.length);
			
			vch = Vector4fBufferChunk.from(fvalues);
		} else if(sample instanceof Path) { // path
			Path[] fvalues = new Path[ovalues.length];
			System.arraycopy(ovalues, 0, fvalues, 0, ovalues.length);
			
			vch = PathBufferChunk.from(fvalues);
		} else { // default (empty bool), warning
			System.err.println("Warning: unsupported parameter type: " + sample.getClass().getSimpleName());
			vch = BoolBufferChunk.from(new BoolValue[] {});
		}
		
		ch.values = (ArrayBufferChunk<T>) vch;
		
		return ch;
	}
}
