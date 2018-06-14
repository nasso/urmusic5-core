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
package io.gitlab.nasso.urmusic.view.panes.preview;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import io.gitlab.nasso.urmusic.model.project.param.EffectParam;
import io.gitlab.nasso.urmusic.view.panes.preview.controls.PreviewParamControl;

public class PreviewParameterControlsPane extends JComponent {
	private class ControlsLayout implements LayoutManager {
		public void addLayoutComponent(String name, Component comp) {
		}

		public void removeLayoutComponent(Component comp) {
		}

		public Dimension preferredLayoutSize(Container parent) {
			return new Dimension(0, 0);
		}

		public Dimension minimumLayoutSize(Container parent) {
			return new Dimension(0, 0);
		}

		public void layoutContainer(Container parent) {
			Component[] comps = parent.getComponents();
			
			for(int i = 0; i < comps.length; i++) {
				Component c = comps[i];
				
				if(c instanceof PreviewParamControl<?>) {
					((PreviewParamControl<?>) c).updateComponentLayout();
				}
			}
		}
	}
	
	private final PreviewView view;
	private List<PreviewParamControl<?>> controls = new ArrayList<>();
	
	public PreviewParameterControlsPane(PreviewView view) {
		this.view = view;
		
		this.setOpaque(false);
		
		this.setLayout(new ControlsLayout());
	}
	
	public void update() {
		this.revalidate();
		this.repaint();
	}
	
	public void addParameter(EffectParam<?> param) {
		PreviewParamControl<?> ctrl = PreviewParamControl.createControl(this.view, param);
		
		if(ctrl != null) {
			this.add(ctrl);
			this.controls.add(ctrl); 
			
			this.revalidate();
			this.repaint();
		}
	}
	
	public void removeParameter(EffectParam<?> param) {
		PreviewParamControl<?> ctrl = this.getControlFor(param);
		
		if(ctrl != null) {
			this.remove(ctrl);
			this.controls.remove(ctrl);
			
			ctrl.dispose();
			
			this.revalidate();
			this.repaint();
		}
	}
	
	private PreviewParamControl<?> getControlFor(EffectParam<?> param) {
		for(PreviewParamControl<?> ctrl : this.controls) {
			if(ctrl.getParam() == param) return ctrl;
		}
		
		return null;
	}
	
	public void dispose() {
	}
}
