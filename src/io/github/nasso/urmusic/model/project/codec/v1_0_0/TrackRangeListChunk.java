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
import java.util.List;

import io.github.nasso.urmusic.model.project.Track.TrackActivityRange;

class TrackRangeListChunk implements Chunk {
	static final int ID = buildBigInt('T', 'R', 'N', 'G');
	
	float[] start;
	float[] end;
	
	public int size() {
		return
			+ 8 // Header
			+ this.start.length * 8; // Data
	}
	
	public void write(OutputStream out) throws IOException {
		writeBigInt(out, ID);
		writeBigInt(out, this.size());
		
		for(int i = 0; i < this.start.length; i++) {
			writeBigInt(out, Float.floatToIntBits(this.start[i]));
			writeBigInt(out, Float.floatToIntBits(this.end[i]));
		}
	}

	public void read(InputStream in) throws IOException {
		Chunk.assertInt(in, ID);
		int size = readBigInt(in); size -= 8;
		
		int len = size / 8;
		this.start = new float[len];
		this.end = new float[len];
		for(int i = 0; i < len; i++) {
			this.start[i] = Float.intBitsToFloat(readBigInt(in)); size -= 4;
			this.end[i] = Float.intBitsToFloat(readBigInt(in)); size -= 4;
		}
		
		Chunk.assertZero(size);
	}
	
	public static TrackRangeListChunk from(List<TrackActivityRange> rangeList) {
		TrackRangeListChunk ch = new TrackRangeListChunk();
		
		ch.start = new float[rangeList.size()];
		ch.end = new float[ch.start.length];
		
		for(int i = 0; i < ch.start.length; i++) {
			TrackActivityRange r = rangeList.get(i);
			ch.start[i] = r.getStart();
			ch.end[i] = r.getEnd();
		}
		
		return ch;
	}
}
