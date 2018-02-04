package io.github.nasso.urmusic.view.components.panels.effectlist.controls;

import java.awt.BorderLayout;

import io.github.nasso.urmusic.model.project.control.Point2DParam;

public class Point2DParamUI extends EffectParamUI<Point2DParam> {
	private static final long serialVersionUID = -7232967232115752212L;

	
	
	public Point2DParamUI(Point2DParam param) {
		super(param);
		
		this.setLayout(new BorderLayout());
	}
	

	public void updateControl(int frame) {
	}
}
