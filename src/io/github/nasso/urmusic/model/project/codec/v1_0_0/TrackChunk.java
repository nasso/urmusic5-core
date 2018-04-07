package io.github.nasso.urmusic.model.project.codec.v1_0_0;

import static io.github.nasso.urmusic.common.DataUtils.*;

import java.io.IOException;
import java.io.OutputStream;

import io.github.nasso.urmusic.model.project.Track;

class TrackChunk implements Chunk {
	static final int ID = buildBigInt('T', 'R', 'C', 'K');
	
	StringChunk name;
	boolean enabled;
	TrackRangeListChunk ranges;
	TrackEffectInstanceChunk[] effects;
	
	public int size() {
		int size =
			+ 8 // Header
			+ this.name.size()
			+ 1 // enabled
			+ this.ranges.size()
			+ 4; // effects.length
		
		for(int i = 0; i < this.effects.length; i++)
			size += this.effects[i].size();
		
		return size;
	}
	
	public void write(OutputStream out) throws IOException {
		writeBigInt(out, ID);
		writeBigInt(out, this.size());
		this.name.write(out);
		out.write(this.enabled ? 1 : 0);
		this.ranges.write(out);
		
		writeBigInt(out, this.effects.length);
		for(int i = 0; i < this.effects.length; i++)
			this.effects[i].write(out);
	}
	
	static TrackChunk from(Track t) {
		TrackChunk ch = new TrackChunk();
		
		ch.name = StringChunk.from(t.getName());
		ch.enabled = t.isEnabled();
		ch.ranges = TrackRangeListChunk.from(t.getActivityRanges());
		ch.effects = new TrackEffectInstanceChunk[t.getEffectCount()];
		
		for(int i = 0; i < ch.effects.length; i++) {
			ch.effects[i] = TrackEffectInstanceChunk.from(t.getEffect(i));
		}
		
		return ch;
	}
}
