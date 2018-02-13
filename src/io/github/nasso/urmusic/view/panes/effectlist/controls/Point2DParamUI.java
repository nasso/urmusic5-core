package io.github.nasso.urmusic.view.panes.effectlist.controls;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.joml.Vector2f;
import org.joml.Vector2fc;

import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.project.control.Point2DParam;
import io.github.nasso.urmusic.view.components.UrmEditableNumberField;

public class Point2DParamUI extends EffectParamUI<Point2DParam> {
	private Vector2f _vec2 = new Vector2f();
	private UrmEditableNumberField xField, yField;
	
	public Point2DParamUI(Point2DParam param) {
		super(param);
		
		JPanel fieldsPane = new JPanel();
		fieldsPane.setOpaque(false);
		
		this.xField = new UrmEditableNumberField((f) -> {
			int frame = UrmusicModel.getFrameCursor();

			Vector2fc val = this.getParam().getValue(frame);
			
			this.getParam().setValue(this._vec2.set(f.getValue().floatValue(), val.y()), frame);
		});
		this.yField = new UrmEditableNumberField((f) -> {
			int frame = UrmusicModel.getFrameCursor();

			Vector2fc val = this.getParam().getValue(frame);
			
			this.getParam().setValue(this._vec2.set(val.x(), f.getValue().floatValue()), frame);
		});
		
		BoxLayout bl = new BoxLayout(fieldsPane, BoxLayout.X_AXIS);
		
		fieldsPane.setLayout(bl);
		fieldsPane.add(this.xField);
		fieldsPane.add(new JLabel(" , "));
		fieldsPane.add(this.yField);
		
		this.setLayout(new BorderLayout());
		this.add(fieldsPane, BorderLayout.EAST);
	}
	

	public void updateControl(int frame) {
		Vector2fc val = this.getParam().getValue(frame);
		
		this.xField.setValue(val.x());
		this.yField.setValue(val.y());
	}
}
