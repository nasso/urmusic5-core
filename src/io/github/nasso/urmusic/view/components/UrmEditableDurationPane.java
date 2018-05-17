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
package io.github.nasso.urmusic.view.components;

import java.awt.Color;
import java.awt.FlowLayout;
import java.util.function.Consumer;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class UrmEditableDurationPane extends JPanel {
	private UrmEditableIntegerField minField;
	private UrmEditableIntegerField secField;
	private UrmEditableIntegerField msField;

	private Consumer<UrmEditableDurationPane> onChange;
	
	public UrmEditableDurationPane() {
		this(false);
	}

	public UrmEditableDurationPane(boolean blockKeyEvents) {
		this(blockKeyEvents, null);
	}
	
	public UrmEditableDurationPane(boolean blockKeyEvents, Consumer<UrmEditableDurationPane> onChange) {
		super(new FlowLayout(FlowLayout.LEFT, 2, 0));
		
		this.onChange = onChange;
		
		this.add(this.minField = new UrmEditableIntegerField(blockKeyEvents, this::onFieldChange, 1));
		
		JLabel lbl = new JLabel(":");
		lbl.setForeground(new Color(0, 0, 0, 128));
		this.add(lbl);
		
		this.add(this.secField = new UrmEditableIntegerField(blockKeyEvents, this::onFieldChange, 2));

		lbl = new JLabel(".");
		lbl.setForeground(new Color(0, 0, 0, 128));
		this.add(lbl);
		
		this.add(this.msField = new UrmEditableIntegerField(blockKeyEvents, this::onFieldChange, 3));
	}
	
	private void onMinFieldChange() {
		if(this.minField.getValue().intValue() < 0) this.minField.setValue(0);
	}
	
	private void onSecFieldChange() {
		float mins = this.secField.getValue().intValue() / 60.0f;
		
		if(mins >= 1.0f) {
			this.secField.setValue((mins % 1) * 60);
			this.minField.setValue(this.minField.getValue().intValue() + ((int) Math.floor(mins)));
		} else if(mins < 0) {
			if(this.minField.getValue().intValue() > 0) {
				this.secField.setValue((mins % 1) * 60 + 60);
				this.minField.setValue(this.minField.getValue().intValue() + ((int) Math.floor(mins)));
				this.onMinFieldChange();
			} else {
				this.secField.setValue(0);
			}
		}
	}
	
	private void onMSFieldChange() {
		float secs = this.msField.getValue().intValue() / 1000.0f;
		
		if(secs >= 1.0f) {
			this.msField.setValue((secs % 1) * 1000);
			this.secField.setValue(this.secField.getValue().intValue() + ((int) Math.floor(secs)));
			this.onSecFieldChange();
		} else if(secs < 0) {
			if(this.secField.getValue().intValue() > 0 || this.minField.getValue().intValue() > 0) {
				this.msField.setValue((secs % 1) * 1000 + 1000);
				
				this.secField.setValue(this.secField.getValue().intValue() + ((int) Math.floor(secs)));
				this.onSecFieldChange();
			} else {
				this.msField.setValue(0);
			}
		}
	}
	
	private void onFieldChange(UrmEditableIntegerField o) {
		if(o == this.minField) this.onMinFieldChange();
		if(o == this.secField) this.onSecFieldChange();
		if(o == this.msField) this.onMSFieldChange();
		
		if(this.onChange != null) this.onChange.accept(this);
	}
	
	public long getMillis() {
		return this.msField.getValue().longValue() + this.secField.getValue().longValue() * 1000l + this.minField.getValue().longValue() * 60000l;
	}
	
	public float getSeconds() {
		return this.getMillis() / 1000.0f;
	}
	
	public void setMillis(long millis) {
		int ms = (int) (millis % 1000);
		int sec = (int) (millis / 1000);
		int mins = sec / 60;
		sec %= 60;
		
		this.minField.setValue(mins);
		this.secField.setValue(sec);
		this.msField.setValue(ms);
	}
	
	public void setSeconds(float timeSec) {
		this.setMillis((long) (timeSec * 1000l));
	}
}
