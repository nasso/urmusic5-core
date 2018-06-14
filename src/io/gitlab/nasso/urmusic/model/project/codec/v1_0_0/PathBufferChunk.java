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
package io.gitlab.nasso.urmusic.model.project.codec.v1_0_0;

import static io.gitlab.nasso.urmusic.common.DataUtils.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

class PathBufferChunk extends ArrayBufferChunk<Path> {
	static final int ID = buildBigInt('P', 'A', 'T', 'H');
	
	Path[] values;
	byte[][] data;
	
	public int size() {
		int size =
			+ 8 // header
			+ 4; // values.length
		
		for(int i = 0; i < this.data.length; i++) {
			size += 4 + this.data[i].length;
		}
		
		return size;
	}

	public void write(OutputStream out) throws IOException {
		writeBigInt(out, ID);
		writeBigInt(out, this.size());
		
		writeBigInt(out, this.data.length);
		for(int i = 0; i < this.data.length; i++) {
			writeBigInt(out, this.data[i].length);
			out.write(this.data[i]);
		}
	}

	public void read(InputStream in) throws IOException {
		Chunk.assertInt(in, ID);
		int size = readBigInt(in); size -= 8;
		
		int len = readBigInt(in); size -= 4;
		this.data = new byte[len][];
		this.values = new Path[len];
		for(int i = 0; i < len; i++) {
			int dataLen = readBigInt(in); size -= 4;
			
			size -= in.read(this.data[i] = new byte[dataLen]);
			this.values[i] = Paths.get(new String(this.data[i], StandardCharsets.UTF_16BE));
		}
		
		Chunk.assertZero(size);
	}
	
	public static PathBufferChunk from(Path[] data) {
		PathBufferChunk ch = new PathBufferChunk();
		
		ch.values = data;
		
		ch.data = new byte[ch.values.length][];
		for(int i = 0; i < ch.values.length; i++)
			ch.data[i] = ch.values[i].toAbsolutePath().toString().getBytes(StandardCharsets.UTF_16BE);
		
		return ch;
	}

	public Path[] getData() {
		return this.values;
	}
}
