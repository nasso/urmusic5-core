package io.github.nasso.urmusic.common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * A wave format loader, originally made for <a href="https://github.com/nasso/nhengine">nhengine</a>.
 * @author nasso
 */
public class WaveFormatLoader {
	private static final int CHUNKID_RIFF = 0x52494646; // = "RIFF"
	private static final int CHUNKID_RIFX = 0x52494658; // = "RIFX"
	private static final int CHUNKID_FMT_ = 0x666D7420; // = "fmt "
	private static final int CHUNKID_DATA = 0x64617461; // = "data"
	private static final int RIFFTYPE_WAVE = 0x57415645; // = "WAVE"
	
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
	
	private int readInt(InputStream in, WaveData data) throws IOException {
		return data.bigEndian ? this.readBigInt(in) : this.readLilInt(in);
	}
	
	private short readShort(InputStream in, WaveData data) throws IOException {
		return data.bigEndian ? this.readBigShort(in) : this.readLilShort(in);
	}
	
	private void readRiffHeader(InputStream in, WaveData data) throws IOException {
		int type = this.readBigInt(in);
		if(type != CHUNKID_RIFF && type != CHUNKID_RIFX) {
			data.isValid = false;
			data.error = "no RIFF or RIFX header";
			return;
		}
		
		data.bigEndian = type == CHUNKID_RIFX;
		
		// skip size
		in.skip(4);
		
		if(this.readBigInt(in) != RIFFTYPE_WAVE) {
			data.isValid = false;
			data.error = "the RIFF type isn't WAVE";
			return;
		}
	}
	
	private void readChunk(InputStream in, WaveData data) throws IOException {
		int chunkID = this.readBigInt(in);
		int chunkSize = this.readInt(in, data);
		
		if(chunkID == CHUNKID_FMT_) {
			// Total expected len: 16
			if(chunkSize != 16) {
				data.isValid = false;
				data.error = "unsupported or invalid format (size is " + chunkSize + ")";
				return;
			}
			
			// Len	| Name
			// 2	| Compression code (1 = Uncompressed PCM)
			if(this.readShort(in, data) != 1) {
				data.isValid = false;
				data.error = "unsupported format: only uncompressed PCM is supported";
				return;
			}
			
			// 2	| Channels
			data.channels = this.readShort(in, data);
			
			// 4	| Sample rate
			data.sampleRate = this.readInt(in, data);
			
			// 4	| Bytes per sec
			in.skip(4); // no one cares about you, bitrate!
			
			// 2	| Block align
			data.blockAlign = this.readShort(in, data);
			
			// 2	| Bits per sample (usually 16 or 8)
			data.bitsPerSample = this.readShort(in, data);
		} else if(chunkID == CHUNKID_DATA) {
			if(data.channels == -1) { // (could have checked any other value)
				// then it means that we didn't find any format chunk
				data.isValid = false;
				data.error = "no format chunk found before data";
				return;
			}
			
			data.sampleCount = chunkSize / data.blockAlign;
			data.samples = ByteBuffer.allocateDirect(chunkSize);
			
			byte buf[] = new byte[2048];
			int numRead = 0;
			int numToRead = 0;
			while((numToRead = Math.min(buf.length, data.samples.remaining())) != 0) {
				numRead = in.read(buf, 0, numToRead);
				
				if(data.bigEndian) {
					if(data.bitsPerSample == 8) {
						for(int i = 0; i < numRead; i += 2) {
							data.samples.put(buf[i + 1]);
							data.samples.put(buf[i]);
						}
					} else if(data.bitsPerSample == 16) {
						for(int i = 0; i < numRead; i += 4) {
							data.samples.put(buf[i + 3]);
							data.samples.put(buf[i + 2]);
							data.samples.put(buf[i + 1]);
							data.samples.put(buf[i]);
						}
					} else {
						data.isValid = false;
						data.error = "unsupported format: big endian, " + data.bitsPerSample + "bits";
						return;
					}
				} else {
					data.samples.put(buf, 0, numRead);
				}
			}
			data.samples.flip();
			
			data.isComplete = true;
		} else {
			// skip if we're not interested
			in.skip(chunkSize);
		}
	}
	
	public Sound load(String fileName, boolean inJar) throws IOException {
		Sound snd = null;
		
		try(InputStream in = DataUtils.getFileInputStream(fileName, inJar)) {
			WaveData data = new WaveData();
			
			if(data.isValid) this.readRiffHeader(in, data);
			
			while(data.isValid && !data.isComplete)
				this.readChunk(in, data);
			
			if(data.isValid) snd = new Sound(data.samples, data.channels, data.sampleRate, data.bitsPerSample, data.sampleCount);
			else System.err.println("Error: '" + fileName + "' isn't a valid WAVE file: " + data.error);
		} catch(IOException e1) {
			e1.printStackTrace();
		}
		
		return snd;
	}
	
	private static class WaveData {
		public String error = null;
		
		public boolean isValid = true;
		public boolean isComplete = false;
		public boolean bigEndian = false;
		
		public ByteBuffer samples = null;
		public int channels = -1;
		public int sampleRate = -1;
		public int sampleCount = -1;
		
		public short blockAlign = -1;
		public short bitsPerSample = -1;
	}
}
