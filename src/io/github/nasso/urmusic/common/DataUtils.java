package io.github.nasso.urmusic.common;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Deque;

public class DataUtils {
	private DataUtils() { }

	/**
	 * Normalizes a path (aka removes unecessary ".." and ".")
	 * @param path
	 * @return
	 */
	public static String normalizePath(String path) {
		String[] elems = path.split("\\\\|\\/");
		
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
			
			if(pathElements.size() >= 1) b.append('/');
		}
		
		if(isInDir || path.endsWith("/") || path.endsWith("/.")) b.append("/");
		
		return b.toString();
	}
	
	public static String getPathName(String path) {
		String[] e = normalizePath(path).split("/");
		if(e.length <= 0) return "";
		return e[e.length - 1];
	}
	
	public static InputStream getFileInputStream(String filePath, boolean inJar) throws IOException {
		if(inJar) return DataUtils.class.getClassLoader().getResourceAsStream(filePath);
		else return new BufferedInputStream(new FileInputStream(filePath));
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
		InputStream in = getFileInputStream(filePath.toString(), inJar);
		if(in == null) {
			System.err.println("Can't find ressource: " + filePath);
			return null;
		}
		
		String str = readAllString(in);
		in.close();
		
		return str;
	}
	
	public static int readBigInt(InputStream in) throws IOException {
		// read int in big endian
		return ((in.read() & 0xFF) << 24) | ((in.read() & 0xFF) << 16) | ((in.read() & 0xFF) << 8) | (in.read() & 0xFF);
	}
	
	public static int readLilInt(InputStream in) throws IOException {
		// read int in little endian
		return (((((in.read() & 0xFF) << 8) | (in.read() & 0xFF)) & 0xFFFF) | (((((in.read() & 0xFF) << 8) | (in.read() & 0xFF)) & 0xFFFF) << 16));
	}
	
	public static short readLilShort(InputStream in) throws IOException {
		return (short) ((in.read() & 0xFF) | ((in.read() & 0xFF) << 8));
	}
	
	public static short readBigShort(InputStream in) throws IOException {
		return (short) (((in.read() & 0xFF) << 8) | (in.read() & 0xFF));
	}
	
	public static void writeBigInt(OutputStream out, int i) throws IOException {
		out.write(new byte[]{
			(byte) ((i >> 24) & 0xFF),
			(byte) ((i >> 16) & 0xFF),
			(byte) ((i >> 8) & 0xFF),
			(byte) (i & 0xFF)
		});
	}
	
	public static void writeLilInt(OutputStream out, int i) throws IOException {
		out.write(new byte[]{
			(byte) ((i >> 8) & 0xFF),
			(byte) (i & 0xFF),
			(byte) ((i >> 24) & 0xFF),
			(byte) ((i >> 16) & 0xFF)
		});
	}
	
	public static void writeBigShort(OutputStream out, short i) throws IOException {
		out.write(new byte[]{
			(byte) ((i >> 8) & 0xFF),
			(byte) (i & 0xFF)
		});
	}
	
	public static void writeLilShort(OutputStream out, short i) throws IOException {
		out.write(new byte[]{
			(byte) (i & 0xFF),
			(byte) ((i >> 8) & 0xFF)
		});
	}
}
