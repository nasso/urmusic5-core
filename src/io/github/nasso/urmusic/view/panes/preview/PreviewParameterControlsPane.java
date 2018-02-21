package io.github.nasso.urmusic.view.panes.preview;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.project.param.EffectParam;
import io.github.nasso.urmusic.view.panes.preview.controls.PreviewParamControl;

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
			
			int frame = UrmusicModel.getFrameCursor();
			
			for(int i = 0; i < comps.length; i++) {
				Component c = comps[i];
				
				if(c instanceof PreviewParamControl<?>) {
					((PreviewParamControl<?>) c).updateComponentLayout(frame);
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
