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
package io.github.nasso.urmusic.view.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.github.nasso.urmusic.common.DataUtils;

public class UrmusicViewStateCodec {
	public static final int CODEC_VERSION = 0;
	
	private UrmusicViewStateCodec() { }
	
	public static void writeState(UrmusicViewState state, OutputStream out) throws IOException {
		UrmusicSplittedPaneState[] paneStates = state.getPaneStates();
		
		DataUtils.writeBigInt(out, CODEC_VERSION);
		DataUtils.writeBigInt(out, paneStates.length);
		for(int i = 0; i < paneStates.length; i++) {
			paneStates[i].write(out);
		}
	}
	
	public static UrmusicViewState readState(InputStream in) throws IOException {
		int codecVersion = DataUtils.readBigInt(in);
		if(codecVersion != CODEC_VERSION) return null;
		
		int frameCount = DataUtils.readBigInt(in);
		if(frameCount < 0) return null;
		
		UrmusicSplittedPaneState[] paneStates = new UrmusicSplittedPaneState[frameCount];

		for(int i = 0; i < paneStates.length; i++) {
			paneStates[i] = new UrmusicSplittedPaneState();
			paneStates[i].read(in);
		}
		
		return new UrmusicViewState(paneStates);
	}
}
