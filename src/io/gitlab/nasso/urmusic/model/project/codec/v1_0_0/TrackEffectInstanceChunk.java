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

import io.gitlab.nasso.urmusic.model.UrmusicModel;
import io.gitlab.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;

class TrackEffectInstanceChunk implements Chunk {
	static final int ID = buildBigInt('V', 'F', 'X', '\0');
	
	StringChunk id;
	StringChunk scriptSrc;
	boolean enabled;
	EffectParamChunk<?>[] params;
	
	public int size() {
		int size =
			+ 8 // header
			+ this.id.size()
			+ this.scriptSrc.size()
			+ 1 // enabled
			+ 4; // params.length
		
		for(int i = 0; i < this.params.length; i++)
			size += this.params[i].size();
		
		return size;
	}
	
	public void write(OutputStream out) throws IOException {
		writeBigInt(out, ID);
		writeBigInt(out, this.size());
		
		this.id.write(out);
		this.scriptSrc.write(out);
		out.write(this.enabled ? 1 : 0);
		
		writeBigInt(out, this.params.length);
		for(int i = 0; i < this.params.length; i++) {
			this.params[i].write(out);
		}
	}

	public void read(InputStream in) throws IOException {
		Chunk.assertInt(in, ID);
		int size = readBigInt(in); size -= 8;
		
		(this.id = new StringChunk()).read(in); size -= this.id.size();
		(this.scriptSrc = new StringChunk()).read(in); size -= this.scriptSrc.size();
		this.enabled = in.read() != 0; size--;
		
		int len = readBigInt(in); size -= 4;
		this.params = new EffectParamChunk<?>[len];
		for(int i = 0; i < len; i++) {
			(this.params[i] = new EffectParamChunk<>()).read(in); size -= this.params[i].size();
		}
		
		Chunk.assertZero(size);
	}
	
	public static TrackEffectInstanceChunk from(TrackEffectInstance fx) {
		TrackEffectInstanceChunk ch = new TrackEffectInstanceChunk();
		
		ch.id = StringChunk.from(fx.getEffectClass().getEffectClassID());
		ch.scriptSrc = StringChunk.from(fx.getScript().getSource());
		ch.enabled = fx.isEnabled();
		ch.params = new EffectParamChunk[fx.getParameterCount()];
		
		for(int i = 0; i < fx.getParameterCount(); i++)
			ch.params[i] = EffectParamChunk.from(fx.getParameter(i));
		
		return ch;
	}

	public TrackEffectInstance build() {
		TrackEffectInstance tei = UrmusicModel.instanciateEffectById(this.id.build());
		if(tei == null)
			return null;
		
		tei.setEnabled(this.enabled);
		for(int i = 0; i < this.params.length; i++)
			this.params[i].applyTo(tei);
		
		tei.getScript().setSource(this.scriptSrc.build());
		
		return tei;
	}
}

