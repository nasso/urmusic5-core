/*******************************************************************************
 * urmusic - The Free and Open Source Music Visualizer Tool
 * Copyright (C) 2018  nasso (https://github.com/nasso)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * Contact "nasso": nassomails -at- gmail dot com
 ******************************************************************************/
package io.gitlab.nasso.urmusic.view.panes.effectlist.controls;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.joml.Vector2f;
import org.joml.Vector2fc;

import io.gitlab.nasso.urmusic.controller.UrmusicController;
import io.gitlab.nasso.urmusic.model.project.VideoEffect.VideoEffectInstance;
import io.gitlab.nasso.urmusic.model.project.param.Vector2DParam;
import io.gitlab.nasso.urmusic.view.components.UrmEditableNumberField;

public class Vector2DParamUI extends EffectParamUI<Vector2DParam> {
	private Vector2f _vec2 = new Vector2f();
	private UrmEditableNumberField xField, yField;
	
	public Vector2DParamUI(VideoEffectInstance fx, Vector2DParam param) {
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
		
		this.xField = new UrmEditableNumberField(true, (f) -> {
			Vector2fc val = UrmusicController.getParamValueNow(this.getParam());

			UrmusicController.setParamValueNow(this.getParam(), this._vec2.set(f.getValue().floatValue(), val.y()));
		});
		this.yField = new UrmEditableNumberField(true, (f) -> {
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
