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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.nasso.urmusic.Urmusic;

public class FFmpeg {
	public static final Path FFMPEG_LOCATION = Urmusic.URM_STATIC_LIB_FOLDER.toPath().resolve("ffmpeg").toAbsolutePath();
	public static final Path FFMPEG_OUTPUT_LOG = Urmusic.URM_HOME.toPath().resolve("log").resolve("ffmpeg_out.txt");
	public static final Path FFMPEG_ENCODERS_OUTPUT = Urmusic.URM_HOME.toPath().resolve("log").resolve("ffmpeg_encoders.txt");
	public static final Path FFMPEG_MUXERS_OUTPUT = Urmusic.URM_HOME.toPath().resolve("log").resolve("ffmpeg_muxers.txt");
	
	private static final Pattern AVCODEC_REGEX = Pattern.compile("^ ([VA])(.)(.)(.)(.)(.) ([^\\=\\s]+)\\s+(.*)$");
	private static final Pattern FORMATS_REGEX = Pattern.compile("^ (.)(.) ([^\\=\\s]+)\\s+(.*)$");
	
	private static final List<Encoder> ENCODERS = new ArrayList<>();
	private static final List<Encoder> ENCODERS_UNMODIFIABLE = Collections.unmodifiableList(ENCODERS);
	
	private static final List<Muxer> MUXERS = new ArrayList<>();
	private static final List<Muxer> MUXERS_UNMODIFIABLE = Collections.unmodifiableList(MUXERS);
	
	private FFmpeg() { }
	
	public static void init() {
		if(!Files.exists(FFmpeg.FFMPEG_OUTPUT_LOG)) {
			try {
				if(!Files.isDirectory(FFmpeg.FFMPEG_OUTPUT_LOG.getParent()))
					Files.createDirectories(FFmpeg.FFMPEG_OUTPUT_LOG.getParent());
				
				Files.createFile(FFmpeg.FFMPEG_OUTPUT_LOG);
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		
		// Get supported encoders
		{
			ProcessBuilder pb = new ProcessBuilder(new String[] { FFmpeg.FFMPEG_LOCATION.toString(), "-encoders" });
			pb.redirectErrorStream(true);
			pb.redirectOutput(Redirect.to(FFMPEG_ENCODERS_OUTPUT.toFile()));
			
			try {
				Process proc = pb.start();
				proc.waitFor();
				
				BufferedReader reader = new BufferedReader(new FileReader(FFMPEG_ENCODERS_OUTPUT.toFile()));
				
				String line;
				while((line = reader.readLine()) != null) {
					Matcher m = AVCODEC_REGEX.matcher(line);
					if(!m.matches()) continue;
	
					ENCODERS.add(new Encoder(
						m.group(1).charAt(0) == 'V' ? Encoder.Type.VIDEO : Encoder.Type.AUDIO,
						m.group(2).charAt(0) == 'F',
						m.group(3).charAt(0) == 'S',
						m.group(4).charAt(0) == 'X',
						m.group(5).charAt(0) == 'B',
						m.group(6).charAt(0) == 'D',
						m.group(7),
						m.group(8)
					));
				}
				
				reader.close();
			} catch(IOException e) {
				e.printStackTrace();
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		// Get supported muxers
		{
			ProcessBuilder pb = new ProcessBuilder(new String[] { FFmpeg.FFMPEG_LOCATION.toString(), "-muxers" });
			pb.redirectErrorStream(true);
			pb.redirectOutput(Redirect.to(FFMPEG_MUXERS_OUTPUT.toFile()));
			
			try {
				Process proc = pb.start();
				proc.waitFor();
				
				BufferedReader reader = new BufferedReader(new FileReader(FFMPEG_MUXERS_OUTPUT.toFile()));
				
				String line;
				while((line = reader.readLine()) != null) {
					Matcher m = FORMATS_REGEX.matcher(line);
					if(!m.matches()) continue;
	
					MUXERS.add(new Muxer(
						m.group(3),
						m.group(4)
					));
				}
				
				reader.close();
			} catch(IOException e) {
				e.printStackTrace();
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static List<Encoder> getEncoders() {
		return ENCODERS_UNMODIFIABLE;
	}
	
	public static Encoder getEncoderByName(String name) {
		for(Encoder e : ENCODERS)
			if(e.getName().equals(name)) return e;
		
		return null;
	}

	public static List<Muxer> getMuxers() {
		return MUXERS_UNMODIFIABLE;
	}
	
	public static Muxer getMuxerByName(String name) {
		for(Muxer e : MUXERS)
			if(e.getName().equals(name)) return e;
		
		return null;
	}
	
	public static Process execute(String... command) {
		List<String> cmd = new ArrayList<>(command.length + 1);
		cmd.add(FFmpeg.FFMPEG_LOCATION.toString());
		cmd.add("-y");
		for(String arg : command) {
			cmd.add(arg);
		}
		
		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.redirectErrorStream(true);
		pb.redirectOutput(FFmpeg.FFMPEG_OUTPUT_LOG.toFile());
		
		try {
			return pb.start();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
