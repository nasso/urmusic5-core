package io.github.nasso.urmusic.view.panes.effectlist.controls;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import io.github.nasso.urmusic.model.project.control.BooleanParam;
import io.github.nasso.urmusic.model.project.control.EffectParam;
import io.github.nasso.urmusic.model.project.control.FloatParam;
import io.github.nasso.urmusic.model.project.control.Point2DParam;
import io.github.nasso.urmusic.model.project.control.RGBA32Param;

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
	
	public static EffectParamUI<?> getParamUI(EffectParam<?> param) {
		if(param instanceof FloatParam) {
			return new FloatParamUI((FloatParam) param);
		} else if(param instanceof RGBA32Param) {
			return new RGBA32ParamUI((RGBA32Param) param);
		} else if(param instanceof Point2DParam) {
			return new Point2DParamUI((Point2DParam) param);
		} else if(param instanceof BooleanParam) {
			return new BooleanParamUI((BooleanParam) param);
		}
		
		return null;
	}
}
