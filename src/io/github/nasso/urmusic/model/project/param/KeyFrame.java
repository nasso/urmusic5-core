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
package io.github.nasso.urmusic.model.project.param;

import java.util.ArrayList;
import java.util.List;

import io.github.nasso.urmusic.common.easing.EasingFunction;
import io.github.nasso.urmusic.common.event.KeyFrameListener;

public class KeyFrame<T> {
	private List<KeyFrameListener<T>> listeners = new ArrayList<>();
	
	private T value = null;
	private float time = 0;
	private EasingFunction easing = EasingFunction.LINEAR;
	
	public KeyFrame() {
	}
	
	public KeyFrame(float position, T value, EasingFunction interp) {
		this.setPosition(position);
		this.setValue(value);
		this.setEasingFunction(interp);
	}

	public T getValue() {
		return this.value;
	}

	public void setValue(T value) {
		if(this.value == value) return;
		this.value = value;
		this.notifyValueChanged(this.value);
	}

	public float getPosition() {
		return this.time;
	}

	public void setPosition(float time) {
		if(this.time == time) return;
		this.time = time;
		this.notifyPositionChanged(this.time);
	}

	public EasingFunction getEasingFunction() {
		return this.easing;
	}

	public void setEasingFunction(EasingFunction interp) {
		if(this.easing == interp) return;
		this.easing = interp;
		this.notifyInterpChanged(this.easing);
	}
	
	public void addKeyFrameListener(KeyFrameListener<T> l) {
		this.listeners.add(l);
	}
	
	public void removeKeyFrameListener(KeyFrameListener<T> l) {
		this.listeners.remove(l);
	}
	
	private void notifyValueChanged(T newValue) {
		for(KeyFrameListener<T> l : this.listeners)
			l.valueChanged(this, newValue);
	}
	
	private void notifyPositionChanged(float newValue) {
		for(KeyFrameListener<T> l : this.listeners)
			l.positionChanged(this, newValue);
	}
	
	private void notifyInterpChanged(EasingFunction newValue) {
		for(KeyFrameListener<T> l : this.listeners)
			l.interpChanged(this, newValue);
	}
}
