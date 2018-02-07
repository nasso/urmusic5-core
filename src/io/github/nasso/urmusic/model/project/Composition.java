package io.github.nasso.urmusic.model.project;

import java.util.ArrayList;
import java.util.List;

import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.event.CompositionListener;
import io.github.nasso.urmusic.model.event.TrackListener;
import io.github.nasso.urmusic.model.event.TimelineListener;
import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.github.nasso.urmusic.utils.MutableRGBA32;
import io.github.nasso.urmusic.utils.RGBA32;

public class Composition implements TimelineListener, TrackListener {
	private Timeline timeline = new Timeline();
	private MutableRGBA32 clearColor = new MutableRGBA32();
	private int width = 1280, height = 720;
	
	private List<CompositionListener> listeners = new ArrayList<>();
	
	public Composition() {
		this.timeline.addTracklistListener(this);
	}
	
	public Timeline getTimeline() {
		return this.timeline;
	}
	
	public int getWidth() {
		return this.width;
	}

	public void setWidth(int width) {
		if(this.width == width) return;
		
		this.setSize(width, this.height);
	}

	public int getHeight() {
		return this.height;
	}

	public void setHeight(int height) {
		if(this.height == height) return;
		
		this.setSize(this.width, height);
	}
	
	public void setSize(int w, int h) {
		if(w == this.width && h == this.height) return;
		
		this.width = w;
		this.height = h;
		
		this.notifyResized();
	}

	public MutableRGBA32 getClearColor() {
		return this.clearColor;
	}

	public void setClearColor(RGBA32 val) {
		this.setClearColor(val.getRGBA());
	}
	
	public void setClearColor(int rgba) {
		if(this.clearColor.getRGBA() == rgba) return;
		
		this.clearColor.setRGBA(rgba);
		this.notifyClearColorChanged();
	}
	
	public void dispose() {
		this.timeline.dispose();
		
		for(CompositionListener l : this.listeners) {
			l.dispose(this);
		}
	}

	public void addListener(CompositionListener l) {
		this.listeners.add(l);
	}
	
	public void removeListener(CompositionListener l) {
		this.listeners.remove(l);
	}
	
	private void notifyClearColorChanged() {
		for(CompositionListener l : this.listeners) {
			l.clearColorChanged(this);
		}
	}
	
	private void notifyResized() {
		for(CompositionListener l : this.listeners) {
			l.resize(this);
		}
	}
	
	public void dirtyFlagged(Track source) {
		UrmusicModel.makeCompositionDirty(Composition.this);
	}
	
	public void effectAdded(Track source, TrackEffectInstance e, int pos) {
		UrmusicModel.makeCompositionDirty(Composition.this);
	}
	
	public void effectRemoved(Track source, TrackEffectInstance e, int pos) {
		UrmusicModel.makeCompositionDirty(Composition.this);
	}
	
	public void effectMoved(Track source, TrackEffectInstance e, int oldPos, int newPos) {
		UrmusicModel.makeCompositionDirty(Composition.this);
	}
	
	public void trackAdded(Timeline src, int index, Track track) {
		track.addTrackListener(this);
		
		UrmusicModel.makeCompositionDirty(Composition.this);
	}
	
	public void trackRemoved(Timeline src, int index, Track track) {
		track.addTrackListener(this);
		
		UrmusicModel.makeCompositionDirty(Composition.this);
	}
	
	public void lengthChanged(Timeline src) {
	}

	public void framerateChanged(Timeline src) {
	}
	
	public void rangesChanged(Track source) {
		UrmusicModel.makeCompositionDirty(Composition.this);
	}

	public void enabledStateChanged(Track source, boolean isEnabledNow) {
		UrmusicModel.makeCompositionDirty(Composition.this);
	}

	public void nameChanged(Track source, String newName) {
	}

}
