package io.github.nasso.urmusic.model.project.codec.v1_0_0;

import static io.github.nasso.urmusic.common.DataUtils.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;

class TrackEffectInstanceChunk implements Chunk {
	static final int ID = buildBigInt('V', 'F', 'X', '\0');
	
	StringChunk id;
	boolean enabled;
	EffectParamChunk<?>[] params;
	
	public int size() {
		int size =
			+ 8 // header
			+ this.id.size()
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
		
		return tei;
	}
}

