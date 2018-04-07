package io.github.nasso.urmusic.model.project.codec.v1_0_0;

import static io.github.nasso.urmusic.common.DataUtils.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import io.github.nasso.urmusic.model.project.Track.TrackActivityRange;

public class TrackRangeListChunk implements Chunk {
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
