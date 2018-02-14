package io.github.nasso.urmusic.view.panes.effectlist.controls;

import javax.swing.JComponent;

import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.github.nasso.urmusic.model.project.control.FloatParam;
import io.github.nasso.urmusic.view.components.UrmEditableNumberField;

public class FloatParamUI extends EffectParamUI<FloatParam> {
	private UrmEditableNumberField field;
	
	public FloatParamUI(TrackEffectInstance fx, FloatParam param) {
		super(fx, param);
	}
	
	public void updateControl(int frame) {
		this.field.setValue(this.getParam().getValue(frame));
	}

	public JComponent buildUI() {
		this.field = new UrmEditableNumberField((f) -> {
			int frame = UrmusicModel.getFrameCursor();
			
			this.getParam().setValue(f.getValue().floatValue(), frame);
		});
		this.field.setStep(this.getParam().getStep());
		
		return this.field;
	}
}
