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
package io.github.nasso.urmusic.common;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import io.github.nasso.urmusic.Urmusic;

/**
 * Small thread safe high and low level util class for byte manipulation and other IO operations.
 * 
 * @author nasso
 */
public class DataUtils {
	private DataUtils() { }

	public static BufferedImage loadImage(String imagePath) {
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File(imagePath));
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		if(img == null) {
			System.err.println("Couldn't load " + imagePath);
			return null;
		}
		
		return img;
	}
	
	/**
	 * Creates a local file, located in the {@link Urmusic#URM_HOME} folder.
	 * @param path
	 * @return
	 */
	public static File localFile(String path) {
		return new File(Urmusic.getHome(), path);
	}
	
	/**
	 * Normalizes a path (aka removes unecessary ".." and ".")
	 * @param path
	 * @return
	 */
	public static String normalizePath(String path, char separator) {
		String[] elems = path.split(Matcher.quoteReplacement("" + separator));
		
		Deque<String> pathElements = new ArrayDeque<>();
		
		boolean isInDir = false;
		
		for(int i = 0; i < elems.length; i++) {
			String e = elems[i];
			
			if(e.isEmpty() || ".".equals(e)) continue;
			
			if(!pathElements.isEmpty() && "..".equals(e) && !"..".equals(pathElements.peek())) {
				pathElements.pop();
				isInDir = true;
			} else {
				isInDir = false;
				pathElements.push(e);
			}
		}
		
		StringBuilder b = new StringBuilder();
		
		while(!pathElements.isEmpty()) {
			String e = pathElements.pollLast();
			b.append(e);
			
			if(pathElements.size() >= 1) b.append(separator);
		}
		
		if(isInDir || path.endsWith("" + separator) || path.endsWith(separator + ".")) b.append(separator);
		
		return b.toString();
	}
	
	public static String normalizePath(String path) {
		return DataUtils.normalizePath(DataUtils.normalizeSeparators(path), File.separatorChar);
	}

	public static String getPathName(String path, char separator) {
		String[] e = DataUtils.normalizePath(path, separator).split(Matcher.quoteReplacement("" + separator));
		if(e.length <= 0) return "";
		return e[e.length - 1];
	}

	public static String getPathName(String path) {
		return DataUtils.getPathName(DataUtils.normalizeSeparators(path), File.separatorChar);
	}
	
	public static String normalizeSeparators(String path, String separator) {
		return path.replaceAll("\\\\|\\/", Matcher.quoteReplacement(separator));
	}
	
	public static String normalizeSeparators(String path) {
		return DataUtils.normalizeSeparators(path, File.separator);
	}
	
	/**
	 * Returns all the files in the given folder in the class path, including the content of subdirectories.
	 * Only the files are returned, directories are ommited.
	 * 
	 * @param path
	 * @return
	 * @throws IOException 
	 * @throws URISyntaxException 
	 */
	public static List<Path> listFilesInResource(String path) throws IOException, URISyntaxException {
		URI uri = DataUtils.class.getClassLoader().getResource(path).toURI();
		
		Closeable thingToClose = null;
		
		Path p;
		if(uri.getScheme().equals("jar")) {
			FileSystem fs = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
			p = fs.getPath("/" + path);
			
			thingToClose = fs;
		} else p = Paths.get(uri);
		
		List<Path> paths = Files.walk(p).filter(e -> !Files.isDirectory(e)).map(e -> p.relativize(e)).collect(Collectors.toList());
		
		if(thingToClose != null) thingToClose.close();
		
		return paths;
	}
	
	public static InputStream getFileInputStream(String filePath, boolean inJar) throws IOException {
		if(inJar) return DataUtils.class.getClassLoader().getResourceAsStream(filePath);
		else return new BufferedInputStream(new FileInputStream(new File(filePath)));
	}
	
	public static void exportResource(String resPath, String destPath) throws IOException {
		resPath = DataUtils.normalizeSeparators(resPath, "/");
		
		File dest = new File(destPath);
		File destParent = dest.getParentFile();
		
		if(destParent != null && !destParent.exists())
			destParent.mkdirs();
		
		BufferedInputStream in = new BufferedInputStream(DataUtils.getFileInputStream(resPath, true));
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(dest));
		
		byte[] buffer = new byte[4096];
		int numread = 0;
		
		while((numread = in.read(buffer)) != -1) {
			out.write(buffer, 0, numread);
		}
		
		in.close();
		out.close();
	}
	
	public static String readAllString(InputStream in) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		
		StringBuilder lines = new StringBuilder();
		String line;
		
		while((line = reader.readLine()) != null)
			lines.append(line).append('\n');
		
		reader.close();
		
		return lines.toString();
	}
	
	public static String readFile(CharSequence filePath, boolean inJar) throws IOException {
		InputStream in = DataUtils.getFileInputStream(filePath.toString(), inJar);
		if(in == null) {
			System.err.println("Can't find ressource: " + filePath);
			return null;
		}
		
		String str = DataUtils.readAllString(in);
		in.close();
		
		return str;
	}

	public static int readBigInt(InputStream in) throws IOException {
		// read int in big endian
		return buildBigInt((byte) (in.read() & 0xFF), (byte) (in.read() & 0xFF), (byte) (in.read() & 0xFF), (byte) (in.read() & 0xFF));
	}
	
	public static int readLilInt(InputStream in) throws IOException {
		// read int in little endian
		return buildLilInt((byte) (in.read() & 0xFF), (byte) (in.read() & 0xFF), (byte) (in.read() & 0xFF), (byte) (in.read() & 0xFF));
	}
	
	public static short readLilShort(InputStream in) throws IOException {
		return buildLilShort((byte) (in.read() & 0xFF), (byte) (in.read() & 0xFF));
	}
	
	public static short readBigShort(InputStream in) throws IOException {
		return buildBigShort((byte) (in.read() & 0xFF), (byte) (in.read() & 0xFF));
	}

	public static void writeBigInt(OutputStream out, int i) throws IOException {
		byte[] dword = new byte[4];
		
		unpackBigInt(i, dword);
		out.write(dword, 0, 4);
	}
	
	public static void writeLilInt(OutputStream out, int i) throws IOException {
		byte[] dword = new byte[4];
		
		unpackLilInt(i, dword);
		out.write(dword, 0, 4);
	}
	
	public static void writeBigShort(OutputStream out, short i) throws IOException {
		byte[] word = new byte[2];
		
		unpackBigShort(i, word);
		out.write(word, 0, 2);
	}
	
	public static void writeLilShort(OutputStream out, short i) throws IOException {
		byte[] word = new byte[2];
		
		unpackLilShort(i, word);
		out.write(word, 0, 2);
	}
	
	public static void unpackLilInt(int i, byte[] destdword) {
		destdword[0] = (byte) (i & 0xFF);
		destdword[1] = (byte) ((i >> 8) & 0xFF);
		destdword[2] = (byte) ((i >> 16) & 0xFF);
		destdword[3] = (byte) ((i >> 24) & 0xFF);
	}
	
	public static void unpackBigInt(int i, byte[] destdword) {
		destdword[0] = (byte) ((i >> 24) & 0xFF);
		destdword[1] = (byte) ((i >> 16) & 0xFF);
		destdword[2] = (byte) ((i >> 8) & 0xFF);
		destdword[3] = (byte) (i & 0xFF);
	}
	
	public static void unpackLilShort(short i, byte[] destword) {
		destword[0] = (byte) (i & 0xFF);
		destword[1] = (byte) ((i >> 8) & 0xFF);
	}
	
	public static void unpackBigShort(short i, byte[] destword) {
		destword[0] = (byte) ((i >> 8) & 0xFF);
		destword[1] = (byte) (i & 0xFF);
	}
	
	public static void unpackLilInt(int i, char[] destdword) {
		destdword[0] = (char) (i & 0xFF);
		destdword[1] = (char) ((i >> 8) & 0xFF);
		destdword[2] = (char) ((i >> 16) & 0xFF);
		destdword[3] = (char) ((i >> 24) & 0xFF);
	}
	
	public static void unpackBigInt(int i, char[] destdword) {
		destdword[0] = (char) ((i >> 24) & 0xFF);
		destdword[1] = (char) ((i >> 16) & 0xFF);
		destdword[2] = (char) ((i >> 8) & 0xFF);
		destdword[3] = (char) (i & 0xFF);
	}
	
	public static void unpackLilShort(short i, char[] destword) {
		destword[0] = (char) (i & 0xFF);
		destword[1] = (char) ((i >> 8) & 0xFF);
	}
	
	public static void unpackBigShort(short i, char[] destword) {
		destword[0] = (char) ((i >> 8) & 0xFF);
		destword[1] = (char) (i & 0xFF);
	}
	
	public static int buildLilInt(byte a, byte b, byte c, byte d) {
		return ((d & 0xFF) << 24) | ((c & 0xFF) << 16) | ((b & 0xFF) << 8) | (a & 0xFF);
	}
	
	public static int buildBigInt(byte a, byte b, byte c, byte d) {
		return ((a & 0xFF) << 24) | ((b & 0xFF) << 16) | ((c & 0xFF) << 8) | (d & 0xFF);
	}
	
	public static short buildLilShort(byte a, byte b) {
		return (short) (((b & 0xFF) << 8) | (a & 0xFF));
	}
	
	public static short buildBigShort(byte a, byte b) {
		return (short) (((a & 0xFF) << 8) | (b & 0xFF));
	}
	
	public static int buildLilInt(char a, char b, char c, char d) {
		return buildLilInt((byte) a, (byte) b, (byte) c, (byte) d);
	}
	
	public static int buildBigInt(char a, char b, char c, char d) {
		return buildBigInt((byte) a, (byte) b, (byte) c, (byte) d);
	}
	
	public static short buildLilShort(char a, char b) {
		return buildLilShort((byte) a, (byte) b);
	}
	
	public static short buildBigShort(char a, char b) {
		return buildBigShort((byte) a, (byte) b);
	}
}
