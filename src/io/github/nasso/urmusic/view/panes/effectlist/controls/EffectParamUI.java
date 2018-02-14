package io.github.nasso.urmusic.view.panes.effectlist.controls;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.github.nasso.urmusic.model.project.control.BooleanParam;
import io.github.nasso.urmusic.model.project.control.EffectParam;
import io.github.nasso.urmusic.model.project.control.FloatParam;
import io.github.nasso.urmusic.model.project.control.OptionParam;
import io.github.nasso.urmusic.model.project.control.Point2DParam;
import io.github.nasso.urmusic.model.project.control.RGBA32Param;

public abstract class EffectParamUI<T extends EffectParam<?>> extends JPanel {
	private final T param;
	private final TrackEffectInstance fx;
	
	public EffectParamUI(TrackEffectInstance fx, T param) {
		this.fx = fx;
		this.param = param;
		
		this.setOpaque(false);
		
		this.setLayout(new BorderLayout());
		this.add(this.buildUI(), BorderLayout.EAST);
	}
	
	public abstract JComponent buildUI();
	
	public abstract void updateControl(int frame);
	
	public void frameChanged(int oldPosition, int newPosition) {
		SwingUtilities.invokeLater(() -> this.updateControl(newPosition));
	}
	
	public TrackEffectInstance getEffectInstance() {
		return this.fx;
	}
	
	public T getParam() {
		return this.param;
	}
	
	public static EffectParamUI<?> createParamUI(TrackEffectInstance fx, EffectParam<?> param) {
		if(param instanceof FloatParam) {
			return new FloatParamUI(fx, (FloatParam) param);
		} else if(param instanceof RGBA32Param) {
			return new RGBA32ParamUI(fx, (RGBA32Param) param);
		} else if(param instanceof Point2DParam) {
			return new Point2DParamUI(fx, (Point2DParam) param);
		} else if(param instanceof BooleanParam) {
			return new BooleanParamUI(fx, (BooleanParam) param);
		} else if(param instanceof OptionParam) {
			return new OptionParamUI(fx, (OptionParam) param);
		}
		
		return null;
	}
}
