package io.github.nasso.urmusic.view.components.panels.effectlist.controls;

import java.awt.BorderLayout;

import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.project.control.FloatParam;

public class FloatParamUI extends EffectParamUI<FloatParam> {
	private static final long serialVersionUID = 8290439490941369516L;
	
	private NumberField field;
	
	public FloatParamUI(FloatParam param) {
		super(param);

		this.field = new NumberField();
		this.field.setOnValueChange((f) -> {
			int frame = UrmusicModel.getFrameCursor();
			
			this.getParam().setValue(f.getValue().floatValue(), frame);
		});
		
		this.setLayout(new BorderLayout());
		this.add(this.field, BorderLayout.EAST);
	}
	
	public void updateControl(int frame) {
		this.field.setValue(this.getParam().getValue(frame));
	}
}
