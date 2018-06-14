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

import javax.swing.JComponent;

import io.gitlab.nasso.urmusic.controller.UrmusicController;
import io.gitlab.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.gitlab.nasso.urmusic.model.project.param.FloatParam;
import io.gitlab.nasso.urmusic.view.components.UrmEditableNumberField;

public class FloatParamUI extends EffectParamUI<FloatParam> {
	private UrmEditableNumberField field;
	
	public FloatParamUI(TrackEffectInstance fx, FloatParam param) {
		super(fx, param);
	}
	
	public void updateControl() {
		this.field.setValue(UrmusicController.getParamValueNow(this.getParam()));
	}

	public JComponent buildUI() {
		this.field = new UrmEditableNumberField(true, f -> UrmusicController.setParamValueNow(this.getParam(), f.getValue().floatValue()));
		this.field.setStep(this.getParam().getStep());
		
		return this.field;
	}
}
