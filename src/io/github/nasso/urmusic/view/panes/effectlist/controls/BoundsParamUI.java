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
		this.xField = new UrmEditableNumberField(true, (f) -> {
			this._vec4.set(UrmusicController.getParamValueNow(this.getParam()));
			this._vec4.x = f.getValue().floatValue();
			
			UrmusicController.setParamValueNow(this.getParam(), this._vec4);
		});
		
		this.wField = new UrmEditableNumberField(true, (f) -> {
			this._vec4.set(UrmusicController.getParamValueNow(this.getParam()));
			this._vec4.z = f.getValue().floatValue();

			UrmusicController.setParamValueNow(this.getParam(), this._vec4);
		});
		
		this.yField = new UrmEditableNumberField(true, (f) -> {
			this._vec4.set(UrmusicController.getParamValueNow(this.getParam()));
			this._vec4.y = f.getValue().floatValue();

			UrmusicController.setParamValueNow(this.getParam(), this._vec4);
		});
		
		this.hField = new UrmEditableNumberField(true, (f) -> {
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
