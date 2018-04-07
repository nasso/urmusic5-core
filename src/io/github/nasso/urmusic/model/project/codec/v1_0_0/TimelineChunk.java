package io.github.nasso.urmusic.model.project.codec.v1_0_0;

import static io.github.nasso.urmusic.common.DataUtils.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import io.github.nasso.urmusic.model.project.Timeline;
import io.github.nasso.urmusic.model.project.Track;

class TimelineChunk implements Chunk {
	static final int ID = buildBigInt('T', 'M', 'L', 'N');

	TrackChunk[] tracks;
	float duration;
	float framerate;
	
	public void write(OutputStream out) throws IOException {
		writeBigInt(out, ID);
		writeBigInt(out, this.size());
		writeBigInt(out, Float.floatToIntBits(this.duration));
		writeBigInt(out, Float.floatToIntBits(this.framerate));
		
		// Tracks
		for(int i = 0; i < this.tracks.length; i++) {
			this.tracks[i].write(out);
		}
	}

	public int size() {
		// duration and framerate
		int size = 8;
		
		// track chunks
		for(int i = 0; i < this.tracks.length; i++)
			size += this.tracks[i].size();
		
		return 8 + size;
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
}
