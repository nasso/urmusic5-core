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
package io.github.nasso.urmusic.view.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

public class VListLayout implements LayoutManager {
	private int gap = 0;
	
	public VListLayout(int gap) {
		this.setGap(gap);
	}

	public int getGap() {
		return this.gap;
	}

	public void setGap(int gap) {
		this.gap = gap;
	}

	public void addLayoutComponent(String name, Component comp) { }

	public void removeLayoutComponent(Component comp) { }

	public Dimension preferredLayoutSize(Container parent) {
		int w = 0, h = 0;
		
		for(Component c : parent.getComponents()) {
			Dimension ps = c.getPreferredSize();
			w = Math.max(w, ps.width);
			h += ps.height;
		}
		
		h += this.getGap() * (parent.getComponentCount() - 1);
		
		Insets insets = parent.getInsets();
		w += insets.left + insets.right;
		h += insets.top + insets.bottom;
		
		return new Dimension(w, h);
	}

	public Dimension minimumLayoutSize(Container parent) {
		int w = 0, h = 0;
		
		for(Component c : parent.getComponents()) {
			Dimension ms = c.getMinimumSize();
			w = Math.max(w, ms.width);
			h += ms.height;
		}
		
		h += this.getGap() * (parent.getComponentCount() - 1);
		
		Insets insets = parent.getInsets();
		w += insets.left + insets.right;
		h += insets.top + insets.bottom;
		
		return new Dimension(w, h);
	}

	public void layoutContainer(Container parent) {
		Insets insets = parent.getInsets();
		int w = parent.getWidth();
		int iw = w - insets.left - insets.right;
		
		int x = insets.left, y = insets.top;
		
		for(Component c : parent.getComponents()) {
			Dimension ps = c.getPreferredSize();
			c.setBounds(x, y, iw, ps.height);
			
			y += ps.height + this.getGap();
		}
	}
}
