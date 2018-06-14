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
package io.gitlab.nasso.urmusic.view.panes.preview.controls;

import javax.swing.JComponent;

import io.gitlab.nasso.urmusic.model.project.param.BoundsParam;
import io.gitlab.nasso.urmusic.model.project.param.EffectParam;
import io.gitlab.nasso.urmusic.model.project.param.Point2DParam;
import io.gitlab.nasso.urmusic.view.panes.preview.PreviewView;

public abstract class PreviewParamControl<T extends EffectParam<?>> extends JComponent {
	private PreviewView view;
	private T param;
	
	public PreviewParamControl(PreviewView view, T param) {
		this.view = view;
		this.param = param;
	}
	
	public abstract void updateComponentLayout();
	public abstract void dispose();
	
	public T getParam() {
		return this.param;
	}

	public float xUIToPos(int x) {
		return this.view.xUIToPos(x);
	}

	public float yUIToPos(int y) {
		return this.view.yUIToPos(y);
	}

	public int xPosToUI(float x) {
		return this.view.xPosToUI(x);
	}
	
	public int yPosToUI(float y) {
		return this.view.yPosToUI(y);
	}
	
	public static PreviewParamControl<?> createControl(PreviewView view, EffectParam<?> param) {
		if(param instanceof Point2DParam) return new Point2DControl(view, (Point2DParam) param);
		if(param instanceof BoundsParam) return new BoundsControl(view, (BoundsParam) param);
		
		return null;
	}
}
