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

import javax.swing.JComponent;

import io.github.nasso.urmusic.controller.UrmusicController;
import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.github.nasso.urmusic.model.project.param.IntParam;
import io.github.nasso.urmusic.view.components.UrmEditableIntegerField;

public class IntParamUI extends EffectParamUI<IntParam> {
	private UrmEditableIntegerField field;
	
	public IntParamUI(TrackEffectInstance fx, IntParam param) {
		super(fx, param);
	}
	
	public void updateControl() {
		this.field.setValue(UrmusicController.getParamValueNow(this.getParam()));
	}

	public JComponent buildUI() {
		this.field = new UrmEditableIntegerField(f -> UrmusicController.setParamValueNow(this.getParam(), f.getValue().intValue()));
		this.field.setStep(this.getParam().getStep());
		
		return this.field;
	}
}
