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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.gitlab.nasso.urmusic.model.project.Project;

public class Codec_V1_0_0 {
	public static void save(Project p, OutputStream out) throws IOException {
		Encoder.save(p, out);
	}
	
	public static Project load(InputStream in) throws IOException {
		return Decoder.load(in);
	}
}
