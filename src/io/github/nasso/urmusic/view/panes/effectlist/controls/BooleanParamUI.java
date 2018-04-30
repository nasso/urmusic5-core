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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JComponent;

import io.github.nasso.urmusic.common.BoolValue;
import io.github.nasso.urmusic.controller.UrmusicController;
import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.github.nasso.urmusic.model.project.param.BooleanParam;

public class BooleanParamUI extends EffectParamUI<BooleanParam> {
	private JCheckBox box;
	
	public BooleanParamUI(TrackEffectInstance fx, BooleanParam param) {
		super(fx, param);
		
		this.setLayout(new BorderLayout());
		this.add(this.box, BorderLayout.EAST);
	}

	public void updateControl() {
		this.box.setSelected(UrmusicController.getParamValueNow(this.getParam()) == BoolValue.TRUE);
	}

	public JComponent buildUI() {
		this.box = new JCheckBox(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				UrmusicController.setParamValueNow(BooleanParamUI.this.getParam(), BooleanParamUI.this.box.isSelected() ? BoolValue.TRUE : BoolValue.FALSE);
			}
		});
		this.box.setOpaque(false);
		
		return this.box;
	}
}
