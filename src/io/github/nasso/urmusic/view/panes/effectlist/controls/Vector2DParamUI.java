package io.github.nasso.urmusic.view.panes.effectlist.controls;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.joml.Vector2f;
import org.joml.Vector2fc;

import io.github.nasso.urmusic.controller.UrmusicController;
import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.github.nasso.urmusic.model.project.param.Vector2DParam;
import io.github.nasso.urmusic.view.components.UrmEditableNumberField;

public class Vector2DParamUI extends EffectParamUI<Vector2DParam> {
	private Vector2f _vec2 = new Vector2f();
	private UrmEditableNumberField xField, yField;
	
	public Vector2DParamUI(TrackEffectInstance fx, Vector2DParam param) {
		super(fx, param);
	}

	public void updateControl() {
		Vector2fc val = UrmusicController.getParamValueNow(this.getParam());
		
		this.xField.setValue(val.x());
		this.yField.setValue(val.y());
	}

	public JComponent buildUI() {
		JPanel fieldsPane = new JPanel();
		fieldsPane.setOpaque(false);
		
		this.xField = new UrmEditableNumberField((f) -> {
			Vector2fc val = UrmusicController.getParamValueNow(this.getParam());

			UrmusicController.setParamValueNow(this.getParam(), this._vec2.set(f.getValue().floatValue(), val.y()));
		});
		this.yField = new UrmEditableNumberField((f) -> {
			Vector2fc val = UrmusicController.getParamValueNow(this.getParam());
			
			UrmusicController.setParamValueNow(this.getParam(), this._vec2.set(val.x(), f.getValue().floatValue()));
		});
		
		this.xField.setStep(this.getParam().getStep().x());
		this.yField.setStep(this.getParam().getStep().y());
		
		BoxLayout bl = new BoxLayout(fieldsPane, BoxLayout.X_AXIS);
		
		fieldsPane.setLayout(bl);
		fieldsPane.add(this.xField);
		fieldsPane.add(new JLabel(" , "));
		fieldsPane.add(this.yField);
		
		return fieldsPane;
	}
}
