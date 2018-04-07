package io.github.nasso.urmusic.model.project.codec.v1_0_0;

import static io.github.nasso.urmusic.common.DataUtils.*;

import java.io.IOException;
import java.io.OutputStream;

import io.github.nasso.urmusic.model.project.Composition;

class CompositionChunk implements Chunk {
	static final int ID = buildBigInt('C', 'O', 'M', 'P');
	
	TimelineChunk timeline;
	int clearColor;
	int width;
	int height;
	
	public void write(OutputStream out) throws IOException {
		writeBigInt(out, ID);
		writeBigInt(out, this.size());
		writeBigInt(out, this.clearColor);
		writeBigInt(out, this.width);
		writeBigInt(out, this.height);
		this.timeline.write(out);
	}

	public int size() {
		return 8 + 4 + 4 + 4 + this.timeline.size();
	}
	
	static CompositionChunk from(Composition comp) {
		CompositionChunk ch = new CompositionChunk();
		
		ch.timeline = TimelineChunk.from(comp.getTimeline());
		ch.clearColor = comp.getClearColor().getRGBA();
		ch.width = comp.getWidth();
		ch.height = comp.getHeight();
		
		return ch;
	}
}
