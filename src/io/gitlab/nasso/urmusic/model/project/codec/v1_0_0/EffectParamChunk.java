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

import io.gitlab.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.gitlab.nasso.urmusic.model.project.param.EffectParam;

class EffectParamChunk<T> implements Chunk {
	static final int ID = buildBigInt('X', 'P', 'A', 'R');
	
	StringChunk id;
	KeyFramesChunk<T> keyFrames;
	
	public int size() {
		return
			+ 8 // header
			+ this.id.size()
			+ this.keyFrames.size();
	}
	
	public void write(OutputStream out) throws IOException {
		writeBigInt(out, ID);
		writeBigInt(out, this.size());
		this.id.write(out);
		this.keyFrames.write(out); 
		// first keyframe is a fake one giving the constant value
		// so when there's only 1 keyframe (written), it means there's no actual key frame for the effect, and the only value written is the constant
	}

	public void read(InputStream in) throws IOException {
		Chunk.assertInt(in, ID);
		int size = readBigInt(in); size -= 8;
		
		(this.id = new StringChunk()).read(in); size -= this.id.size();
		(this.keyFrames = new KeyFramesChunk<>()).read(in); size -= this.keyFrames.size();
		
		Chunk.assertZero(size);
	}
	
	public static <T> EffectParamChunk<T> from(EffectParam<T> param) {
		EffectParamChunk<T> ch = new EffectParamChunk<>();

		ch.id = StringChunk.from(param.getID());
		ch.keyFrames = KeyFramesChunk.from(param.getValue(0), param.getKeyFrames());
		
		return ch;
	}

	@SuppressWarnings("unchecked")
	public void applyTo(TrackEffectInstance tei) {
		EffectParam<T> param = (EffectParam<T>) tei.getParamByID(this.id.build());
		
		if(param == null)
			return;
		
		T[] values = this.keyFrames.values.getData();
		Float[] times = this.keyFrames.times.getData();
		EasingFuncChunk[] easings = this.keyFrames.easings;
		
		// Keyframe zero used to set default value: always exists
		param.setValue(values[0], times[0]);
		
		for(int i = 1; i < values.length; i++)
			param.addKeyFrame(times[i], values[i], easings[i].func);
	}
}
