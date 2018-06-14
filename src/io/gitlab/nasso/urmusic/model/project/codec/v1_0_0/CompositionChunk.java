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

import io.gitlab.nasso.urmusic.model.project.Composition;

class CompositionChunk implements Chunk {
	static final int ID = buildBigInt('C', 'O', 'M', 'P');
	
	TimelineChunk timeline;
	StringChunk name;
	int clearColor;
	int width;
	int height;

	public int size() {
		return 8 + this.name.size() + 4 + 4 + 4 + this.timeline.size();
	}
	
	public void write(OutputStream out) throws IOException {
		writeBigInt(out, ID);
		writeBigInt(out, this.size());
		this.name.write(out);
		writeBigInt(out, this.clearColor);
		writeBigInt(out, this.width);
		writeBigInt(out, this.height);
		this.timeline.write(out);
	}

	public void read(InputStream in) throws IOException {
		Chunk.assertInt(in, ID);
		
		int size = readBigInt(in); size -= 8;
		(this.name = new StringChunk()).read(in); size -= this.name.size();
		this.clearColor = readBigInt(in); size -= 4;
		this.width = readBigInt(in); size -= 4;
		this.height = readBigInt(in); size -= 4;
		(this.timeline = new TimelineChunk()).read(in); size -= this.timeline.size();
		
		Chunk.assertZero(size);
	}
	
	static CompositionChunk from(Composition comp) {
		CompositionChunk ch = new CompositionChunk();
		
		ch.timeline = TimelineChunk.from(comp.getTimeline());
		ch.name = StringChunk.from(comp.getName());
		ch.clearColor = comp.getClearColor().getRGBA();
		ch.width = comp.getWidth();
		ch.height = comp.getHeight();
		
		return ch;
	}
	
	public Composition build() {
		Composition comp = new Composition(this.name.build(), this.timeline.build());
		comp.setClearColor(this.clearColor);
		comp.setWidth(this.width);
		comp.setHeight(this.height);
		
		return comp;
	}
}
