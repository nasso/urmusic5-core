package io.github.nasso.urmusic.view.panes.preview.controls;

import javax.swing.JComponent;

import io.github.nasso.urmusic.model.project.param.BoundsParam;
import io.github.nasso.urmusic.model.project.param.EffectParam;
import io.github.nasso.urmusic.model.project.param.Point2DParam;
import io.github.nasso.urmusic.view.panes.preview.PreviewView;

public abstract class PreviewParamControl<T extends EffectParam<?>> extends JComponent {
	private PreviewView view;
	private T param;
	
	public PreviewParamControl(PreviewView view, T param) {
		this.view = view;
		this.param = param;
	}
	
	public abstract void updateComponentLayout();
	public abstract void dispose();
	
	public T getParam() {
		return this.param;
	}

	public float xUIToPos(int x) {
		return this.view.xUIToPos(x);
	}

	public float yUIToPos(int y) {
		return this.view.yUIToPos(y);
	}

	public int xPosToUI(float x) {
		return this.view.xPosToUI(x);
	}
	
	public int yPosToUI(float y) {
		return this.view.yPosToUI(y);
	}
	
	public static PreviewParamControl<?> createControl(PreviewView view, EffectParam<?> param) {
		if(param instanceof Point2DParam) return new Point2DControl(view, (Point2DParam) param);
		if(param instanceof BoundsParam) return new BoundsControl(view, (BoundsParam) param);
		
		return null;
	}
}
