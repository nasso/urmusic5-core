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

import io.github.nasso.urmusic.common.MutableRGBA32;
import io.github.nasso.urmusic.common.RGBA32;

class RGBA32BufferChunk extends ArrayBufferChunk<RGBA32> {
	static final int ID = buildBigInt('R', 'G', 'B', 'A');
	
	RGBA32[] values;
	
	public int size() {
		return 8 + this.values.length * 4;
	}

	public void write(OutputStream out) throws IOException {
		writeBigInt(out, ID);
		writeBigInt(out, this.size());
		for(int i = 0; i < this.values.length; i++)
			writeBigInt(out, this.values[i].getRGBA());
	}

	public void read(InputStream in) throws IOException {
		Chunk.assertInt(in, ID);
		int size = readBigInt(in); size -= 8;
		
		int len = size / 4;
		this.values = new RGBA32[len];
		
		for(int i = 0; i < len; i++) {
			this.values[i] = new MutableRGBA32(readBigInt(in)); size -= 4;
		}
		
		Chunk.assertZero(size);
	}
	
	public static RGBA32BufferChunk from(RGBA32[] data) {
		RGBA32BufferChunk ch = new RGBA32BufferChunk();
		
		ch.values = data;
		
		return ch;
	}

	public RGBA32[] getData() {
		return this.values;
	}
}
