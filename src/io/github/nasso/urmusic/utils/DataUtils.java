package io.github.nasso.urmusic.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DataUtils {
	private DataUtils() { }

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
