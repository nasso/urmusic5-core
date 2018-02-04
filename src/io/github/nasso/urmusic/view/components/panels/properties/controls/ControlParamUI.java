package io.github.nasso.urmusic.view.components.panels.properties.controls;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import io.github.nasso.urmusic.model.project.control.ControlParam;

public abstract class ControlParamUI<T extends ControlParam<?>> extends JPanel {
	private static final long serialVersionUID = 4381903239053495291L;

	private final T param;
	
	public ControlParamUI(T param) {
		this.param = param;
		
		this.setOpaque(false);
	}
	
	public abstract void updateControl(int frame);
	
	public void frameChanged(int oldPosition, int newPosition) {
		SwingUtilities.invokeLater(() -> this.updateControl(newPosition));
	}
	
	public T getParam() {
		return this.param;
	}
}
