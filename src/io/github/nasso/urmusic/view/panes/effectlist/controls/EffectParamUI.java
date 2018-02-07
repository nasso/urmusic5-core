package io.github.nasso.urmusic.view.panes.effectlist.controls;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import io.github.nasso.urmusic.model.project.control.EffectParam;

public abstract class EffectParamUI<T extends EffectParam<?>> extends JPanel {
	private static final long serialVersionUID = 4381903239053495291L;

	private final T param;
	
	public EffectParamUI(T param) {
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
