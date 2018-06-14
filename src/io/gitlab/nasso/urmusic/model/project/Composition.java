/*******************************************************************************
 * urmusic - The Free and Open Source Music Visualizer Tool
 * Copyright (C) 2018  nasso (https://github.com/nasso)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * Contact "nasso": nassomails -at- gmail dot com
 ******************************************************************************/
package io.gitlab.nasso.urmusic.model.project;

import java.util.ArrayList;
import java.util.List;

import io.gitlab.nasso.urmusic.common.MutableRGBA32;
import io.gitlab.nasso.urmusic.common.RGBA32;
import io.gitlab.nasso.urmusic.common.event.CompositionListener;

public class Composition {
	private String name;
	
	private Timeline timeline;
	private MutableRGBA32 clearColor = new MutableRGBA32();
	private int width = 1280, height = 720;
	
	private List<CompositionListener> listeners = new ArrayList<>();
	
	public Composition() {
		this("Composition", new Timeline());
	}
	
	public Composition(String name) {
		this(name, new Timeline());
	}
	
	public Composition(Timeline tl) {
		this("Composition", tl);
	}
	
	public Composition(String name, Timeline tl) {
		this.timeline = tl;
		this.name = name;
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

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		if(this.name.equals(name)) return;
		
		this.name = name;
		this.notifyRenamed();
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
	
	private void notifyRenamed() {
		for(CompositionListener l : this.listeners) {
			l.nameChanged(this);
		}
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
