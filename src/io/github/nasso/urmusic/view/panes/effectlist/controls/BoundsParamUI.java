package io.github.nasso.urmusic.view.panes.effectlist.controls;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.joml.Vector4f;
import org.joml.Vector4fc;

import io.github.nasso.urmusic.controller.UrmusicController;
import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.github.nasso.urmusic.model.project.param.BoundsParam;
import io.github.nasso.urmusic.view.components.UrmEditableNumberField;

public class BoundsParamUI extends EffectParamUI<BoundsParam> {
	private Vector4f _vec4 = new Vector4f();
	private UrmEditableNumberField xField, wField, yField, hField;
	
	public BoundsParamUI(TrackEffectInstance fx, BoundsParam param) {
		super(fx, param);
	}

	public JComponent buildUI() {
		this.xField = new UrmEditableNumberField((f) -> {
			this._vec4.set(UrmusicController.getParamValueNow(this.getParam()));
			this._vec4.x = f.getValue().floatValue();
			
			UrmusicController.setParamValueNow(this.getParam(), this._vec4);
		});
		
		this.wField = new UrmEditableNumberField((f) -> {
			this._vec4.set(UrmusicController.getParamValueNow(this.getParam()));
			this._vec4.z = f.getValue().floatValue();

			UrmusicController.setParamValueNow(this.getParam(), this._vec4);
		});
		
		this.yField = new UrmEditableNumberField((f) -> {
			this._vec4.set(UrmusicController.getParamValueNow(this.getParam()));
			this._vec4.y = f.getValue().floatValue();

			UrmusicController.setParamValueNow(this.getParam(), this._vec4);
		});
		
		this.hField = new UrmEditableNumberField((f) -> {
			this._vec4.set(UrmusicController.getParamValueNow(this.getParam()));
			this._vec4.w = f.getValue().floatValue();

			UrmusicController.setParamValueNow(this.getParam(), this._vec4);
		});
		
		this.xField.setStep(this.getParam().getStep().x());
		this.yField.setStep(this.getParam().getStep().y());
		this.wField.setStep(this.getParam().getStep().z());
		this.hField.setStep(this.getParam().getStep().w());

		JPanel container = new JPanel();
		container.setOpaque(false);
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		
		JPanel fieldsPane;
		
		// -- XW
		fieldsPane = new JPanel();
		fieldsPane.setOpaque(false);
		fieldsPane.setLayout(new BoxLayout(fieldsPane, BoxLayout.X_AXIS));
		fieldsPane.add(this.xField);
		fieldsPane.add(new JLabel(" , "));
		fieldsPane.add(this.wField);
		container.add(fieldsPane);
		
		// -- YH
		fieldsPane = new JPanel();
		fieldsPane.setOpaque(false);
		fieldsPane.setLayout(new BoxLayout(fieldsPane, BoxLayout.X_AXIS));
		fieldsPane.add(this.yField);
		fieldsPane.add(new JLabel(" , "));
		fieldsPane.add(this.hField);
		container.add(fieldsPane);
		
		return container;
	}

	public void updateControl() {
		Vector4fc val = UrmusicController.getParamValueNow(this.getParam());
		
		this.xField.setValue(val.x());
		this.yField.setValue(val.y());
		this.wField.setValue(val.z());
		this.hField.setValue(val.w());
	}
}
