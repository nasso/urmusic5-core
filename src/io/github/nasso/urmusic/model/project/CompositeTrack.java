package io.github.nasso.urmusic.model.project;

public class CompositeTrack extends Track {
	private Composition comp;
	
	public CompositeTrack(Composition comp) {
		super(comp.getTimeline().getTotalFrameCount());
		this.comp = comp;
	}
	
	public CompositeTrack() {
		this(new Composition());
	}

	public Composition getComposition() {
		return this.comp;
	}

	public void setComposition(Composition comp) {
		this.comp = comp;
	}
}
