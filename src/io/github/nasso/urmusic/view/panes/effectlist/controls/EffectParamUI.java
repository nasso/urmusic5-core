package io.github.nasso.urmusic.view.panes.effectlist.controls;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.github.nasso.urmusic.model.project.param.BooleanParam;
import io.github.nasso.urmusic.model.project.param.BoundsParam;
import io.github.nasso.urmusic.model.project.param.EffectParam;
import io.github.nasso.urmusic.model.project.param.FileParam;
import io.github.nasso.urmusic.model.project.param.FloatParam;
import io.github.nasso.urmusic.model.project.param.IntParam;
import io.github.nasso.urmusic.model.project.param.OptionParam;
import io.github.nasso.urmusic.model.project.param.RGBA32Param;
import io.github.nasso.urmusic.model.project.param.Vector2DParam;

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
	
	public abstract void updateControl();
	
	public void frameChanged(int oldPosition, int newPosition) {
		SwingUtilities.invokeLater(() -> this.updateControl());
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
		} else if(param instanceof IntParam) {
			return new IntParamUI(fx, (IntParam) param);
		} else if(param instanceof RGBA32Param) {
			return new RGBA32ParamUI(fx, (RGBA32Param) param);
		} else if(param instanceof Vector2DParam) {
			return new Vector2DParamUI(fx, (Vector2DParam) param);
		} else if(param instanceof BooleanParam) {
			return new BooleanParamUI(fx, (BooleanParam) param);
		} else if(param instanceof OptionParam) {
			return new OptionParamUI(fx, (OptionParam) param);
		} else if(param instanceof FileParam) {
			return new FileParamUI(fx, (FileParam) param);
		} else if(param instanceof BoundsParam) {
			return new BoundsParamUI(fx, (BoundsParam) param);
		}
		
		return null;
	}
}
