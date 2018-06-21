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

import java.awt.Color;

import javax.swing.JComponent;

import io.gitlab.nasso.urmusic.common.MutableRGBA32;
import io.gitlab.nasso.urmusic.controller.UrmusicController;
import io.gitlab.nasso.urmusic.model.project.VideoEffect.VideoEffectInstance;
import io.gitlab.nasso.urmusic.model.project.param.RGBA32Param;
import io.gitlab.nasso.urmusic.view.components.UrmColorButton;

public class RGBA32ParamUI extends EffectParamUI<RGBA32Param> {
	private MutableRGBA32 _rgba32 = new MutableRGBA32();
	private UrmColorButton colorButton;
	
	public RGBA32ParamUI(VideoEffectInstance fx, RGBA32Param param) {
		super(fx, param);
	}

	public void updateControl() {
		this.colorButton.setColor(UrmusicController.getParamValueNow(this.getParam()));
	}

	public JComponent buildUI() {
		this.colorButton = new UrmColorButton((btn) -> {
			Color c = btn.getColor();
			this._rgba32.setRGBA(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
			UrmusicController.setParamValueNow(this.getParam(), this._rgba32);
		});
		
		return this.colorButton;
	}
}

