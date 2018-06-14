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

import io.gitlab.nasso.urmusic.common.BoolValue;

class BoolBufferChunk extends ArrayBufferChunk<BoolValue> {
	static final int ID = buildBigInt('B', 'O', 'O', 'L');
	
	BoolValue[] values;
	
	public int size() {
		return 8 + this.values.length;
	}

	public void write(OutputStream out) throws IOException {
		writeBigInt(out, ID);
		writeBigInt(out, this.size());
		for(int i = 0; i < this.values.length; i++)
			out.write(this.values[i] == BoolValue.TRUE ? 1 : 0);
	}

	public void read(InputStream in) throws IOException {
		Chunk.assertInt(in, ID);
		
		int size = readBigInt(in); size -= 8;
		
		int len = size;
		this.values = new BoolValue[len];
		for(int i = 0; i < this.values.length; i++) {
			this.values[i] = in.read() == 1 ? BoolValue.TRUE : BoolValue.FALSE; size--;
		}
	}
	
	public static BoolBufferChunk from(BoolValue[] data) {
		BoolBufferChunk ch = new BoolBufferChunk();
		
		ch.values = data;
		
		return ch;
	}

	public BoolValue[] getData() {
		return this.values;
	}
}
