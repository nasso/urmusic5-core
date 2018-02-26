package io.github.nasso.urmusic.model.renderer.audio;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import io.github.nasso.urmusic.common.DataUtils;

/**
 * A wave format loader, originally made for <a href="https://github.com/nasso/nhengine">nhengine</a>,
 * modified for urmusic.
 * 
 * @author nasso
 */
public class WaveFormatLoader {
	private static final int CHUNKID_RIFF = 0x52494646; // = "RIFF"
	private static final int CHUNKID_RIFX = 0x52494658; // = "RIFX"
	private static final int CHUNKID_FMT_ = 0x666D7420; // = "fmt "
	private static final int CHUNKID_DATA = 0x64617461; // = "data"
	private static final int RIFFTYPE_WAVE = 0x57415645; // = "WAVE"

	private FileChannel fc = null;
	
	private String error = null;
	private boolean isValid = true;
	
	private boolean bigEndian = false;
	private int channels = -1;
	private int sampleRate = -1;
	private int sampleCount = -1;
	
	private int dataPosition = 0;
	
	private short blockAlign = -1;
	private short bitsPerSample = -1;
	
	private ByteBuffer readBuffer;
	
	public void setBufferSize(int bufferSize) {
		if(this.readBuffer != null && this.readBuffer.capacity() == bufferSize) return;
		
		this.readBuffer = ByteBuffer.allocate(bufferSize);
	}
	
	public boolean isLoaded() {
		return this.fc != null;
	}
	
	public boolean isBigEndian() {
		return this.bigEndian;
	}
	
	public int getChannels() {
		return this.channels;
	}
	
	public int getSampleRate() {
		return this.sampleRate;
	}
	
	public int getSampleCount() {
		return this.sampleCount;
	}
	
	public int getBlockAlign() {
		return this.blockAlign;
	}
	
	public int getBitsPerSample() {
		return this.bitsPerSample;
	}
	
	public boolean isSigned() {
		return this.bitsPerSample == 16;
	}
	
	private int readBigInt(InputStream in) throws IOException {
		// read int in big endian
		return ((in.read() & 0xFF) << 24) | ((in.read() & 0xFF) << 16) | ((in.read() & 0xFF) << 8) | (in.read() & 0xFF);
	}
	
	private int readLilInt(InputStream in) throws IOException {
		// read int in little endian
		return ((((in.read() & 0xFF) | ((in.read() & 0xFF) << 8)) & 0xFFFF) | ((((in.read() & 0xFF) | ((in.read() & 0xFF) << 8)) & 0xFFFF) << 16));
	}
	
	private short readLilShort(InputStream in) throws IOException {
		return (short) ((in.read() & 0xFF) | ((in.read() & 0xFF) << 8));
	}
	
	private short readBigShort(InputStream in) throws IOException {
		return (short) (((in.read() & 0xFF) << 8) | (in.read() & 0xFF));
	}
	
	private int readInt(InputStream in) throws IOException {
		return this.bigEndian ? this.readBigInt(in) : this.readLilInt(in);
	}
	
	private short readShort(InputStream in) throws IOException {
		return this.bigEndian ? this.readBigShort(in) : this.readLilShort(in);
	}
	
	private void readRiffHeader(InputStream in) throws IOException {
		int type = this.readBigInt(in);
		if(type != WaveFormatLoader.CHUNKID_RIFF && type != WaveFormatLoader.CHUNKID_RIFX) {
			this.isValid = false;
			this.error = "no RIFF or RIFX header";
			return;
		}
		
		this.bigEndian = type == WaveFormatLoader.CHUNKID_RIFX;
		
		// skip size
		in.skip(4);
		
		if(this.readBigInt(in) != WaveFormatLoader.RIFFTYPE_WAVE) {
			this.isValid = false;
			this.error = "the RIFF type isn't WAVE";
			return;
		}
		
		this.dataPosition += 12;
	}
	
	private void readChunkFormat(InputStream in) throws IOException {
		int chunkID = this.readBigInt(in);
		int chunkSize = this.readInt(in);
		this.dataPosition += 8 + chunkSize;
		
		if(chunkID == WaveFormatLoader.CHUNKID_FMT_) {
			// Total expected len: 16
			if(chunkSize != 16) {
				this.isValid = false;
				this.error = "unsupported or invalid format (size is " + chunkSize + ")";
				return;
			}
			
			// Len	| Name
			// 2	| Compression code (1 = Uncompressed PCM)
			if(this.readShort(in) != 1) {
				this.isValid = false;
				this.error = "unsupported format: only uncompressed PCM is supported";
				return;
			}
			
			// 2	| Channels
			this.channels = this.readShort(in);
			
			// 4	| Sample rate
			this.sampleRate = this.readInt(in);
			
			// 4	| Bytes per sec
			in.skip(4); // no one cares about you, bitrate!
			
			// 2	| Block align
			this.blockAlign = this.readShort(in);
			
			// 2	| Bits per sample (usually 16 or 8)
			this.bitsPerSample = this.readShort(in);
		} else {
			// then it means that we didn't find any format chunk
			this.isValid = false;
			this.error = "no format chunk found before data";
		}
	}
	
	private void findDataChunk(InputStream in) throws IOException {
		int chunkID = this.readBigInt(in);
		int chunkSize = this.readInt(in);
		this.dataPosition += 8;
		
		while(chunkID != WaveFormatLoader.CHUNKID_DATA) {
			in.skip(chunkSize);
			this.dataPosition += chunkSize;
			
			chunkID = this.readBigInt(in);
			chunkSize = this.readInt(in);
			this.dataPosition += 8;
		}
		
		this.sampleCount = chunkSize / this.blockAlign;
	}
	
	public void loadFile(Path filePath, boolean inJar) throws IOException {
		this.close();
		
		try(InputStream in = DataUtils.getFileInputStream(filePath.toAbsolutePath().toString(), inJar)) {
			if(this.isValid) this.readRiffHeader(in);
			if(this.isValid) this.readChunkFormat(in);
			if(this.isValid) this.findDataChunk(in);
			
			if(!this.isValid) {
				System.err.println("WAVE file read error for \"" + filePath.toAbsolutePath() + "\": " + this.error);
				return;
			}
		} catch(IOException e1) {
			e1.printStackTrace();
		}

		this.fc = FileChannel.open(filePath, StandardOpenOption.READ);
	}
	
	public int loadSamples(long sampleOffset, byte[] dest) throws IOException {
		this.fc.position(this.dataPosition + sampleOffset * this.blockAlign);
		
		int numRead = this.fc.read(this.readBuffer);
		if(numRead == -1) {
			Arrays.fill(dest, (byte) 0x00);
			return -1;
		}
		
		this.readBuffer.flip();
		
		this.readBuffer.get(dest, 0, numRead);
		this.readBuffer.flip();
		
		for(int i = numRead; i < dest.length; i++) {
			dest[i] = 0x00;
		}
		
		return numRead / this.blockAlign;
	}
	
	public void close() throws IOException {
		if(this.fc != null) {
			this.fc.close();
			this.fc = null;
			
			this.error = null;
			this.isValid = true;
			this.bigEndian = false;
			
			this.channels = -1;
			this.sampleRate = -1;
			this.sampleCount = -1;
			
			this.dataPosition = 0;
			
			this.blockAlign = -1;
			this.bitsPerSample = -1;
		}
	}
}
