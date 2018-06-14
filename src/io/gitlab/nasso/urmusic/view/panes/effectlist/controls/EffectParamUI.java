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

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import io.gitlab.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.gitlab.nasso.urmusic.model.project.param.BooleanParam;
import io.gitlab.nasso.urmusic.model.project.param.BoundsParam;
import io.gitlab.nasso.urmusic.model.project.param.EffectParam;
import io.gitlab.nasso.urmusic.model.project.param.FileParam;
import io.gitlab.nasso.urmusic.model.project.param.FloatParam;
import io.gitlab.nasso.urmusic.model.project.param.IntParam;
import io.gitlab.nasso.urmusic.model.project.param.OptionParam;
import io.gitlab.nasso.urmusic.model.project.param.RGBA32Param;
import io.gitlab.nasso.urmusic.model.project.param.Vector2DParam;

public abstract class EffectParamUI<T extends EffectParam<?>> extends JPanel {
	private final T param;
	private final TrackEffectInstance fx;
	
	public EffectParamUI(TrackEffectInstance fx, T param) {
		this.fx = fx;
		this.param = param;
		
		this.setOpaque(false);
		
		this.setLayout(new BorderLayout());
		this.add(this.buildUI(), BorderLayout.EAST);
	}
	
	public abstract JComponent buildUI();
	
	public abstract void updateControl();
	
	public void frameChanged(int oldPosition, int newPosition) {
		SwingUtilities.invokeLater(() -> this.updateControl());
	}
	
	public TrackEffectInstance getEffectInstance() {
		return this.fx;
	}
	
	public T getParam() {
		return this.param;
	}
	
	public static EffectParamUI<?> createParamUI(TrackEffectInstance fx, EffectParam<?> param) {
		if(param instanceof FloatParam) {
			return new FloatParamUI(fx, (FloatParam) param);
		} else if(param instanceof IntParam) {
			return new IntParamUI(fx, (IntParam) param);
		} else if(param instanceof RGBA32Param) {
			return new RGBA32ParamUI(fx, (RGBA32Param) param);
		} else if(param instanceof Vector2DParam) {
			return new Vector2DParamUI(fx, (Vector2DParam) param);
		} else if(param instanceof BooleanParam) {
			return new BooleanParamUI(fx, (BooleanParam) param);
		} else if(param instanceof OptionParam) {
			return new OptionParamUI(fx, (OptionParam) param);
		} else if(param instanceof FileParam) {
			return new FileParamUI(fx, (FileParam) param);
		} else if(param instanceof BoundsParam) {
			return new BoundsParamUI(fx, (BoundsParam) param);
		}
		
		return null;
	}
}
