package io.github.nasso.urmusic.view.panes.effectlist.controls;

import java.awt.BorderLayout;

import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.project.control.FloatParam;
import io.github.nasso.urmusic.view.components.UrmEditableNumberField;

public class FloatParamUI extends EffectParamUI<FloatParam> {
	private UrmEditableNumberField field;
	
	public FloatParamUI(FloatParam param) {
		super(param);

		this.field = new UrmEditableNumberField((f) -> {
			int frame = UrmusicModel.getFrameCursor();
			
			this.getParam().setValue(f.getValue().floatValue(), frame);
		});
		this.field.setStep(param.getStep());
		
		this.setLayout(new BorderLayout());
		this.add(this.field, BorderLayout.EAST);
	}
	
	public void updateControl(int frame) {
		this.field.setValue(this.getParam().getValue(frame));
	}
}
