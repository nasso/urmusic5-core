package io.github.nasso.urmusic.model.project;

import java.util.ArrayList;
import java.util.List;

import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.event.CompositionListener;
import io.github.nasso.urmusic.model.event.TrackEffectsListener;
import io.github.nasso.urmusic.model.event.TracklistListener;
import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.github.nasso.urmusic.utils.MutableRGBA32;
import io.github.nasso.urmusic.utils.RGBA32;

public class Composition implements TracklistListener, TrackEffectsListener {
	private Timeline timeline = new Timeline();
	private MutableRGBA32 clearColor = new MutableRGBA32();
	private int length = 2400; // 1:20 @ 30fps
	private int width = 1280, height = 720;
	private int framerate = 30;
	
	private List<CompositionListener> listeners = new ArrayList<>();
	
	public Composition() {
		this.timeline.addTracklistListener(this);
	}
	
	public Timeline getTimeline() {
		return this.timeline;
	}

	public int getLength() {
		return this.length;
	}

	public void setLength(int length) {
		if(this.length == length) return;
		this.length = length;
		this.notifyLengthChanged();
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

	public int getFramerate() {
		return this.framerate;
	}

	public void setFramerate(int framerate) {
		if(this.framerate == framerate) return;
		this.framerate = framerate;
		this.notifyFramerateChanged();
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
	
	private void notifyLengthChanged() {
		for(CompositionListener l : this.listeners) {
			l.lengthChanged(this);
		}
	}
	
	private void notifyResized() {
		for(CompositionListener l : this.listeners) {
			l.resize(this);
		}
	}
	
	private void notifyFramerateChanged() {
		for(CompositionListener l : this.listeners) {
			l.framerateChanged(this);
		}
	}
	
	public void effectAdded(TrackEffectInstance e, int pos) {
		UrmusicModel.makeCompositionDirty(Composition.this);
	}
	
	public void effectRemoved(TrackEffectInstance e, int pos) {
		UrmusicModel.makeCompositionDirty(Composition.this);
	}
	
	public void effectMoved(TrackEffectInstance e, int oldPos, int newPos) {
		UrmusicModel.makeCompositionDirty(Composition.this);
	}
	
	public void trackAdded(int index, Track<?> track) {
		track.addEffectsListener(this);
		
		UrmusicModel.makeCompositionDirty(Composition.this);
	}
	
	public void trackRemoved(int index, Track<?> track) {
		track.removeEffectsListener(this);
		
		UrmusicModel.makeCompositionDirty(Composition.this);
	}
}
