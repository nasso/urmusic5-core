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
package io.gitlab.nasso.urmusic.common;

import java.util.ArrayList;
import java.util.List;

import io.gitlab.nasso.urmusic.common.event.ValueChangeListener;

public class ObservableValue<T> {
	private List<ValueChangeListener<T>> listeners = new ArrayList<>();
	private T value;
	
	public final ObservableValue<T> set(T value) {
		T old = this.value;
		this.value = value;
		this.notifyChange(old, this.value);
		return this;
	}
	
	public T get() {
		return this.value;
	}
	
	public void addListener(ValueChangeListener<T> l) {
		if(this.listeners.contains(l)) return;
		this.listeners.add(l);
	}
	
	public void removeListener(ValueChangeListener<T> l) {
		this.listeners.remove(l);
	}
	
	protected void notifyChange(T oldValue, T newValue) {
		for(ValueChangeListener<T> l : this.listeners)
			l.valueChanged(oldValue, newValue);
	}
}
