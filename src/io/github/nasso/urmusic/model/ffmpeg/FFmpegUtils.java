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
package io.github.nasso.urmusic.model.ffmpeg;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import io.github.nasso.urmusic.Urmusic;

public class FFmpegUtils {
	public static final Path FFMPEG_LOCATION = Urmusic.URM_STATIC_LIB_FOLDER.toPath().resolve("ffmpeg").toAbsolutePath();
	public static final Path FFMPEG_OUTPUT_LOG = Urmusic.URM_HOME.toPath().resolve("log").resolve("ffmpeg_out.txt");
	
	private FFmpegUtils() { }
	
	static {
		if(!Files.exists(FFmpegUtils.FFMPEG_OUTPUT_LOG)) {
			try {
				if(!Files.isDirectory(FFmpegUtils.FFMPEG_OUTPUT_LOG.getParent()))
					Files.createDirectories(FFmpegUtils.FFMPEG_OUTPUT_LOG.getParent());
				
				Files.createFile(FFmpegUtils.FFMPEG_OUTPUT_LOG);
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static Process execute(String... command) {
		List<String> cmd = new ArrayList<>(command.length + 1);
		cmd.add(FFmpegUtils.FFMPEG_LOCATION.toString());
		cmd.add("-y");
		for(String arg : command) {
			cmd.add(arg);
		}
		
		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.redirectErrorStream(true);
		pb.redirectOutput(FFmpegUtils.FFMPEG_OUTPUT_LOG.toFile());
		
		try {
			return pb.start();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
