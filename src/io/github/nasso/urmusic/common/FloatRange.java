package io.github.nasso.urmusic.common;

/**
 * An float range, defined by <code>[start; end]</code> (both <code>start</code> and <code>end</code> are included in the range).
 * @author nasso
 */
public interface FloatRange {
	public float getStart();
	public float getEnd();
	
	public default boolean contains(float x) {
		return x >= this.getStart() && x <= this.getEnd();
	}
}
