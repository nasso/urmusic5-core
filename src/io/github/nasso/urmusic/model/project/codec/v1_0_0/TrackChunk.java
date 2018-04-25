package io.github.nasso.urmusic.model.project.codec.v1_0_0;

import static io.github.nasso.urmusic.common.DataUtils.*;

import java.io.IOException;
import java.io.InputStream;
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

	public void read(InputStream in) throws IOException {
		Chunk.assertInt(in, ID);
		int size = readBigInt(in); size -= 8;
		
		(this.name = new StringChunk()).read(in); size -= this.name.size();
		this.enabled = in.read() != 0; size--;
		(this.ranges = new TrackRangeListChunk()).read(in); size -= this.ranges.size();
		
		int len = readBigInt(in); size -= 4;
		this.effects = new TrackEffectInstanceChunk[len];
		for(int i = 0; i < len; i++) {
			(this.effects[i] = new TrackEffectInstanceChunk()).read(in); size -= this.effects[i].size();
		}
		
		Chunk.assertZero(size);
	}
	
	static TrackChunk from(Track t) {
		TrackChunk ch = new TrackChunk();
		
		ch.name = StringChunk.from(t.getName());
		ch.enabled = t.isEnabled();
		ch.ranges = TrackRangeListChunk.from(t.getActivityRanges());
		ch.effects = new TrackEffectInstanceChunk[t.getEffectCount()];
		for(int i = 0; i < ch.effects.length; i++)
			ch.effects[i] = TrackEffectInstanceChunk.from(t.getEffect(i));
		
		return ch;
	}

	public Track build() {
		Track tr = new Track();
		
		tr.setName(this.name.build());
		tr.setEnabled(this.enabled);
		for(int i = 0; i < this.ranges.start.length; i++)
			tr.addActiveRange(this.ranges.start[i], this.ranges.end[i]);
		for(int i = 0; i < this.effects.length; i++)
			tr.addEffect(this.effects[i].build());
		
		return tr;
	}
}
