package io.github.nasso.urmusic.view.panes.effectlist.controls;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.joml.Vector4f;
import org.joml.Vector4fc;

import io.github.nasso.urmusic.model.UrmusicModel;
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
			int frame = UrmusicModel.getFrameCursor();

			this._vec4.set(this.getParam().getValue(frame));
			this._vec4.x = f.getValue().floatValue();
			
			this.getParam().setValue(this._vec4, frame);
		});
		this.wField = new UrmEditableNumberField((f) -> {
			int frame = UrmusicModel.getFrameCursor();

			this._vec4.set(this.getParam().getValue(frame));
			this._vec4.z = f.getValue().floatValue();
			
			this.getParam().setValue(this._vec4, frame);
		});
		this.yField = new UrmEditableNumberField((f) -> {
			int frame = UrmusicModel.getFrameCursor();

			this._vec4.set(this.getParam().getValue(frame));
			this._vec4.y = f.getValue().floatValue();
			
			this.getParam().setValue(this._vec4, frame);
		});
		this.hField = new UrmEditableNumberField((f) -> {
			int frame = UrmusicModel.getFrameCursor();

			this._vec4.set(this.getParam().getValue(frame));
			this._vec4.w = f.getValue().floatValue();
			
			this.getParam().setValue(this._vec4, frame);
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

	public void updateControl(int frame) {
		Vector4fc val = this.getParam().getValue(frame);
		
		this.xField.setValue(val.x());
		this.yField.setValue(val.y());
		this.wField.setValue(val.z());
		this.hField.setValue(val.w());
	}
}
