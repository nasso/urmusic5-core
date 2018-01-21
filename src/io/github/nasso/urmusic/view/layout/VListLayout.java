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
