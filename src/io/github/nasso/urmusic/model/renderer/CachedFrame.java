package io.github.nasso.urmusic.model.renderer;

import io.github.nasso.urmusic.model.project.Composition;

public class CachedFrame {
	public final int index;
	public Composition comp;
	public int frame_id = 0;
	public boolean dirty = true;
	
	public CachedFrame(int i) {
		this.index = i;
	}
}
