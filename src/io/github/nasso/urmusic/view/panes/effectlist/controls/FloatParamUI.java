package io.github.nasso.urmusic.view.panes.effectlist.controls;

import javax.swing.JComponent;

import io.github.nasso.urmusic.controller.UrmusicController;
import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.github.nasso.urmusic.model.project.param.FloatParam;
import io.github.nasso.urmusic.view.components.UrmEditableNumberField;

public class FloatParamUI extends EffectParamUI<FloatParam> {
	private UrmEditableNumberField field;
	
	public FloatParamUI(TrackEffectInstance fx, FloatParam param) {
		super(fx, param);
	}
	
	public void updateControl() {
		this.field.setValue(UrmusicController.getParamValueNow(this.getParam()));
	}

	public JComponent buildUI() {
		this.field = new UrmEditableNumberField(f -> UrmusicController.setParamValueNow(this.getParam(), f.getValue().floatValue()));
		this.field.setStep(this.getParam().getStep());
		
		return this.field;
	}
}
