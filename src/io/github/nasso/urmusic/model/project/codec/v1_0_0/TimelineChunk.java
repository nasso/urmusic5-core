package io.github.nasso.urmusic.model.project.codec.v1_0_0;

import static io.github.nasso.urmusic.common.DataUtils.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import io.github.nasso.urmusic.model.project.Timeline;
import io.github.nasso.urmusic.model.project.Track;

class TimelineChunk implements Chunk {
	static final int ID = buildBigInt('T', 'M', 'L', 'N');

	TrackChunk[] tracks;
	float duration;
	float framerate;

	public int size() {
		int size =
				+ 8 // header
				+ 4 // duration
				+ 4 // framerate
				+ 4; // tracks.length
		
		// track chunks
		for(int i = 0; i < this.tracks.length; i++)
			size += this.tracks[i].size();
		
		return size;
	}
	
	public void write(OutputStream out) throws IOException {
		writeBigInt(out, ID);
		writeBigInt(out, this.size());
		writeBigInt(out, Float.floatToIntBits(this.duration));
		writeBigInt(out, Float.floatToIntBits(this.framerate));
		
		// Tracks
		writeBigInt(out, this.tracks.length);
		for(int i = 0; i < this.tracks.length; i++) {
			this.tracks[i].write(out);
		}
	}

	public void read(InputStream in) throws IOException {
		Chunk.assertInt(in, ID);
		
		int size = readBigInt(in); size -= 8;
		this.duration = Float.intBitsToFloat(readBigInt(in)); size -= 4;
		this.framerate = Float.intBitsToFloat(readBigInt(in)); size -= 4;
		
		// Tracks
		this.tracks = new TrackChunk[readBigInt(in)]; size -= 4;
		
		for(int i = 0; i < this.tracks.length; i++) {
			(this.tracks[i] = new TrackChunk()).read(in); size -= this.tracks[i].size();
		}
		
		Chunk.assertZero(size);
	}
	
	static TimelineChunk from(Timeline tl) {
		List<Track> tracks = tl.getTracks();
		
		TimelineChunk ch = new TimelineChunk();
		
		ch.duration = tl.getDuration();
		ch.framerate = tl.getFramerate();
		ch.tracks = new TrackChunk[tracks.size()];
		for(int i = 0; i < ch.tracks.length; i++)
			ch.tracks[i] = TrackChunk.from(tracks.get(i));
		
		return ch;
	}

	public Timeline build() {
		Timeline tl = new Timeline();
		
		tl.setDuration(this.duration);
		tl.setFramerate(this.framerate);
		for(int i = 0; i < this.tracks.length; i++)
			tl.addTrack(this.tracks[i].build());
		
		return tl;
	}
}
