package io.github.nasso.urmusic.model.project.codec.v1_0_0;

import static io.github.nasso.urmusic.common.DataUtils.*;

import java.io.IOException;
import java.io.OutputStream;

import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;

class TrackEffectInstanceChunk implements Chunk {
	static final int ID = buildBigInt('V', 'F', 'X', '\0');
	
	StringChunk id;
	boolean enabled;
	EffectParamChunk<?>[] params;
	
	public int size() {
		int size =
			+ 8 // header
			+ 1 // enabled
			+ 4; // params.length
		
		for(int i = 0; i < this.params.length; i++) {
			size += this.params[i].size();
		}
		
		return size;
	}
	
	public void write(OutputStream out) throws IOException {
		writeBigInt(out, ID);
		writeBigInt(out, this.size());
		out.write(this.enabled ? 1 : 0);
		
		writeBigInt(out, this.params.length);
		for(int i = 0; i < this.params.length; i++) {
			this.params[i].write(out);
		}
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
}
