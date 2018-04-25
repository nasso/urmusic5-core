package io.github.nasso.urmusic.model.project.codec.v1_0_0;

abstract class ArrayBufferChunk<T> implements Chunk {
	public abstract T[] getData();
}
