package io.github.nasso.urmusic.model.project.codec.v1_0_0;

import static io.github.nasso.urmusic.common.DataUtils.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.github.nasso.urmusic.model.project.Composition;

class CompositionChunk implements Chunk {
	static final int ID = buildBigInt('C', 'O', 'M', 'P');
	
	TimelineChunk timeline;
	int clearColor;
	int width;
	int height;

	public int size() {
		return 8 + 4 + 4 + 4 + this.timeline.size();
	}
	
	public void write(OutputStream out) throws IOException {
		writeBigInt(out, ID);
		writeBigInt(out, this.size());
		writeBigInt(out, this.clearColor);
		writeBigInt(out, this.width);
		writeBigInt(out, this.height);
		this.timeline.write(out);
	}

	public void read(InputStream in) throws IOException {
		Chunk.assertInt(in, ID);
		
		int size = readBigInt(in); size -= 8;
		this.clearColor = readBigInt(in); size -= 4;
		this.width = readBigInt(in); size -= 4;
		this.height = readBigInt(in); size -= 4;
		(this.timeline = new TimelineChunk()).read(in); size -= this.timeline.size();
		
		Chunk.assertZero(size);
	}
	
	static CompositionChunk from(Composition comp) {
		CompositionChunk ch = new CompositionChunk();
		
		ch.timeline = TimelineChunk.from(comp.getTimeline());
		ch.clearColor = comp.getClearColor().getRGBA();
		ch.width = comp.getWidth();
		ch.height = comp.getHeight();
		
		return ch;
	}
	
	public Composition build() {
		Composition comp = new Composition(this.timeline.build());
		comp.setClearColor(this.clearColor);
		comp.setWidth(this.width);
		comp.setHeight(this.height);
		
		return comp;
	}
}
