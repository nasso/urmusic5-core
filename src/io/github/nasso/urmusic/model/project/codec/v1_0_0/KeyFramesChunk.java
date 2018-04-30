/*******************************************************************************
 * urmusic - The Free and Open Source Music Visualizer Tool
 * Copyright (C) 2018  nasso (https://github.com/nasso)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * Contact "nasso": nassomails -at- gmail dot com
 ******************************************************************************/
package io.github.nasso.urmusic.model.project.codec.v1_0_0;

import static io.github.nasso.urmusic.common.DataUtils.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import io.github.nasso.urmusic.common.BoolValue;
import io.github.nasso.urmusic.common.RGBA32;
import io.github.nasso.urmusic.model.project.param.KeyFrame;

class KeyFramesChunk<T> implements Chunk {
	static final int ID = buildBigInt('K', 'F', 'L', 'S');
	
	private static final byte TYPE_BOOL = 0;
	private static final byte TYPE_FLOAT = 1;
	private static final byte TYPE_INT  = 2;
	private static final byte TYPE_RGBA32 = 3;
	private static final byte TYPE_VEC2 = 4;
	private static final byte TYPE_VEC3 = 5;
	private static final byte TYPE_VEC4 = 6;
	private static final byte TYPE_PATH = 7;

	byte type;
	ArrayBufferChunk<T> values;
	FloatBufferChunk times;
	EasingFuncChunk[] easings;
	
	public int size() {
		int size =
			+ 8 // header
			+ 1 // type
			+ this.values.size()
			+ this.times.size();
		
		for(int i = 0; i < this.easings.length; i++)
			size += this.easings[i].size();
		
		return size;
	}
	
	public void write(OutputStream out) throws IOException {
		writeBigInt(out, ID);
		writeBigInt(out, this.size());
		out.write(this.type);
		this.values.write(out);
		this.times.write(out);
		
		for(int i = 0; i < this.easings.length; i++)
			this.easings[i].write(out);
	}

	@SuppressWarnings("unchecked")
	public void read(InputStream in) throws IOException {
		Chunk.assertInt(in, ID);
		int size = readBigInt(in); size -= 8;
		
		this.type = (byte) in.read(); size--;
		switch(this.type) {
			case TYPE_BOOL:
				this.values = (ArrayBufferChunk<T>) new BoolBufferChunk();
				break;
			case TYPE_FLOAT:
				this.values = (ArrayBufferChunk<T>) new FloatBufferChunk();
				break;
			case TYPE_INT:
				this.values = (ArrayBufferChunk<T>) new IntegerBufferChunk();
				break;
			case TYPE_RGBA32:
				this.values = (ArrayBufferChunk<T>) new RGBA32BufferChunk();
				break;
			case TYPE_VEC2:
				this.values = (ArrayBufferChunk<T>) new Vector2fBufferChunk();
				break;
			case TYPE_VEC3:
				this.values = (ArrayBufferChunk<T>) new Vector3fBufferChunk();
				break;
			case TYPE_VEC4:
				this.values = (ArrayBufferChunk<T>) new Vector4fBufferChunk();
				break;
			case TYPE_PATH:
				this.values = (ArrayBufferChunk<T>) new PathBufferChunk();
				break;
		}
		
		this.values.read(in); size -= this.values.size();
		(this.times = new FloatBufferChunk()).read(in); size -= this.times.size();
		
		int len = this.times.values.length;
		this.easings = new EasingFuncChunk[len];
		for(int i = 0; i < len; i++) {
			(this.easings[i] = new EasingFuncChunk()).read(in); size -= this.easings[i].size();
		}
		
		Chunk.assertZero(size);
	}
	
	/**
	 * @param sample Needed to detect the type. Will be the default value
	 * @param frames
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> KeyFramesChunk<T> from(T sample, List<KeyFrame<T>> frames) {
		KeyFramesChunk<T> ch = new KeyFramesChunk<>();
		
		Object[] ovalues = new Object[frames.size() + 1];
		Float[] timesBuffer = new Float[frames.size() + 1];
		ch.easings = new EasingFuncChunk[frames.size() + 1];
		
		ovalues[0] = sample;
		timesBuffer[0] = 0.0f;
		ch.easings[0] = EasingFuncChunk.DUMMY;
		
		for(int i = 1; i < ovalues.length; i++) {
			KeyFrame<T> kf = frames.get(i - 1);
			
			ovalues[i] = kf.getValue();
			timesBuffer[i] = kf.getPosition();
			ch.easings[i] = EasingFuncChunk.from(kf.getEasingFunction());
		}
		
		byte type = 0;
		ArrayBufferChunk<?> vch = null;
		
		if(sample instanceof BoolValue) { // bool
			type = TYPE_BOOL;
			
			BoolValue[] fvalues = new BoolValue[ovalues.length];
			System.arraycopy(ovalues, 0, fvalues, 0, ovalues.length);
			
			vch = BoolBufferChunk.from(fvalues);
		} else if(sample instanceof Float) { // float
			type = TYPE_FLOAT;
			
			Float[] fvalues = new Float[ovalues.length];
			System.arraycopy(ovalues, 0, fvalues, 0, ovalues.length);
			
			vch = FloatBufferChunk.from(fvalues);
		} else if(sample instanceof Integer) { // int
			type = TYPE_INT;
			
			Integer[] fvalues = new Integer[ovalues.length];
			System.arraycopy(ovalues, 0, fvalues, 0, ovalues.length);
			
			vch = IntegerBufferChunk.from(fvalues);
		} else if(sample instanceof RGBA32) { // rgba32 (int)
			type = TYPE_RGBA32;
			
			RGBA32[] fvalues = new RGBA32[ovalues.length];
			System.arraycopy(ovalues, 0, fvalues, 0, ovalues.length);
			
			vch = RGBA32BufferChunk.from(fvalues);
		} else if(sample instanceof Vector2f) { // vec2
			type = TYPE_VEC2;
			
			Vector2f[] fvalues = new Vector2f[ovalues.length];
			System.arraycopy(ovalues, 0, fvalues, 0, ovalues.length);
			
			vch = Vector2fBufferChunk.from(fvalues);
		} else if(sample instanceof Vector3f) { // vec3
			type = TYPE_VEC3;
			
			Vector3f[] fvalues = new Vector3f[ovalues.length];
			System.arraycopy(ovalues, 0, fvalues, 0, ovalues.length);
			
			vch = Vector3fBufferChunk.from(fvalues);
		} else if(sample instanceof Vector4f) { // vec4
			type = TYPE_VEC4;
			
			Vector4fc[] fvalues = new Vector4fc[ovalues.length];
			System.arraycopy(ovalues, 0, fvalues, 0, ovalues.length);
			
			vch = Vector4fBufferChunk.from(fvalues);
		} else if(sample instanceof Path) { // path
			type = TYPE_PATH;
			
			Path[] fvalues = new Path[ovalues.length];
			System.arraycopy(ovalues, 0, fvalues, 0, ovalues.length);
			
			vch = PathBufferChunk.from(fvalues);
		} else { // default (empty bool), warning
			System.err.println("Warning: unsupported parameter type: " + sample.getClass().getSimpleName());
			vch = BoolBufferChunk.from(new BoolValue[] {});
		}
		
		ch.type = type;
		ch.values = (ArrayBufferChunk<T>) vch;
		ch.times = FloatBufferChunk.from(timesBuffer);
		
		return ch;
	}
}
