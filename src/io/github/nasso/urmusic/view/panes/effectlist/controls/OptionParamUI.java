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

import java.awt.Font;
import java.awt.event.ItemEvent;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import io.github.nasso.urmusic.controller.UrmusicController;
import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.github.nasso.urmusic.model.project.param.OptionParam;
import io.github.nasso.urmusic.view.data.UrmusicStrings;

public class OptionParamUI extends EffectParamUI<OptionParam> {
	private JComboBox<String> combo;
	
	public OptionParamUI(TrackEffectInstance fx, OptionParam param) {
		super(fx, param);
	}

	public void updateControl() {
		this.combo.setSelectedIndex(UrmusicController.getParamValueNow(this.getParam()));
	}

	public JComponent buildUI() {
		this.combo = new JComboBox<>();
		this.combo.setFont(this.combo.getFont().deriveFont(Font.PLAIN, 11f));
		
		OptionParam p = this.getParam();
		for(int i = 0; i < p.getOptionCount(); i++)
			this.combo.addItem(
					UrmusicStrings.getString(
							"effect." + this.getEffectInstance().getEffectClass().getEffectClassID() +
							".param." + p.getID() +
							".option." + p.getOptionName(i)));
		
		this.combo.addItemListener((e) -> {
			if(e.getStateChange() == ItemEvent.SELECTED)
				UrmusicController.setParamValueNow(this.getParam(), this.combo.getSelectedIndex());
		});
		
		return this.combo;
	}
}
