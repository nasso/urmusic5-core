package io.github.nasso.urmusic.model.ffmpeg;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import io.github.nasso.urmusic.Urmusic;

public class FFmpegUtils {
	public static final Path FFMPEG_LOCATION = Paths.get(System.getProperty("ffmpegLocation")).toAbsolutePath();
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
