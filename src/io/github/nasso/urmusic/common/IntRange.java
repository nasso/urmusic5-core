package io.github.nasso.urmusic.common;

/**
 * An integer range, defined by <code>[start; end]</code> (both <code>start</code> and <code>end</code> are included in the range).
 * @author nasso
 */
public interface IntRange {
	public int getStart();
	public int getEnd();
	
	public default boolean contains(int x) {
		return x >= this.getStart() && x <= this.getEnd();
	}
}
