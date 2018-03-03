package io.github.nasso.urmusic.view.panes.effectlist.controls;

import javax.swing.JComponent;

import io.github.nasso.urmusic.controller.UrmusicController;
import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.github.nasso.urmusic.model.project.param.IntParam;
import io.github.nasso.urmusic.view.components.UrmEditableIntegerField;

public class IntParamUI extends EffectParamUI<IntParam> {
	private UrmEditableIntegerField field;
	
	public IntParamUI(TrackEffectInstance fx, IntParam param) {
		super(fx, param);
	}
	
	public void updateControl() {
		this.field.setValue(UrmusicController.getParamValueNow(this.getParam()));
	}

	public JComponent buildUI() {
		this.field = new UrmEditableIntegerField(f -> UrmusicController.setParamValueNow(this.getParam(), f.getValue().intValue()));
		this.field.setStep(this.getParam().getStep());
		
		return this.field;
	}
}
