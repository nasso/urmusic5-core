package io.github.nasso.urmusic.model.project;

import java.util.ArrayList;
import java.util.List;

import io.github.nasso.urmusic.common.MutableRGBA32;
import io.github.nasso.urmusic.common.RGBA32;
import io.github.nasso.urmusic.common.event.CompositionListener;

public class Composition {
	private Timeline timeline = new Timeline();
	private MutableRGBA32 clearColor = new MutableRGBA32();
	private int width = 1280, height = 720;
	
	private List<CompositionListener> listeners = new ArrayList<>();
	
	public Composition() {
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

	public RGBA32 getClearColor() {
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
}
