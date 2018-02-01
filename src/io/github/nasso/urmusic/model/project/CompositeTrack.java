package io.github.nasso.urmusic.model.project;

import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;

public class CompositeTrack extends Track<TrackEffectInstance> {
	private Composition comp;
	
	public CompositeTrack(Composition comp) {
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
