package io.github.nasso.urmusic.view.components.panels.properties.controls;

import java.awt.BorderLayout;

import javax.swing.JLabel;

import io.github.nasso.urmusic.model.project.control.FloatParam;

public class FloatParamUI extends ControlParamUI<FloatParam> {
	private static final long serialVersionUID = 8290439490941369516L;
	
	private JLabel valueLabel;
	
	public FloatParamUI(FloatParam param) {
		super(param);
		
		this.valueLabel = new JLabel();

		this.setLayout(new BorderLayout());
		this.add(this.valueLabel, BorderLayout.EAST);
	}

	public void updateControl(int frame) {
		this.valueLabel.setText(String.valueOf(this.getParam().getValue(frame)));
	}
}
