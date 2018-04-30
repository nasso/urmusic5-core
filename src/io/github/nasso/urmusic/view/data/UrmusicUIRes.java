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
package io.github.nasso.urmusic.view.data;

import javax.swing.ImageIcon;

import io.github.nasso.urmusic.Urmusic;

public class UrmusicUIRes {
	public static final ImageIcon VERTICAL_SPLIT_ICON = new ImageIcon(Urmusic.class.getResource("/res/ui/icons8-split-vertical-16.png"));
	public static final ImageIcon HORIZONTAL_SPLIT_ICON = new ImageIcon(Urmusic.class.getResource("/res/ui/icons8-divider-16.png"));
	public static final ImageIcon DELETE_ICON = new ImageIcon(Urmusic.class.getResource("/res/ui/icons8-delete-16.png"));
	public static final ImageIcon DELETE_ICON_S = new ImageIcon(Urmusic.class.getResource("/res/ui/icons8-delete-8.png"));
	public static final ImageIcon POPUP_ICON = new ImageIcon(Urmusic.class.getResource("/res/ui/icons8-open-in-popup-16.png"));

	public static final ImageIcon TRI_RIGHT_ICON = new ImageIcon(Urmusic.class.getResource("/res/ui/icons8-forward-12.png"));
	public static final ImageIcon TRI_DOWN_ICON = new ImageIcon(Urmusic.class.getResource("/res/ui/icons8-expand-arrow-12.png"));
	public static final ImageIcon KEY_FRAME_ICON = new ImageIcon(Urmusic.class.getResource("/res/ui/icons8-camera-automation-16.png"));
	public static final ImageIcon KEY_FRAME_ICON_BLUE = new ImageIcon(Urmusic.class.getResource("/res/ui/icons8-camera-automation-16-blue.png"));
	public static final ImageIcon KEY_FRAME_ICON_RED = new ImageIcon(Urmusic.class.getResource("/res/ui/icons8-camera-automation-16-red.png"));
	public static final ImageIcon SORT_UP_ICON = new ImageIcon(Urmusic.class.getResource("/res/ui/icons8-sort-up-16.png"));
	public static final ImageIcon SORT_DOWN_ICON = new ImageIcon(Urmusic.class.getResource("/res/ui/icons8-sort-down-16.png"));
	public static final ImageIcon CODE_ICON = new ImageIcon(Urmusic.class.getResource("/res/ui/icons8-source-code-16.png"));
	
	public static final ImageIcon ERROR_ICON = new ImageIcon(Urmusic.class.getResource("/res/ui/icons8-close-window-16.png"));
	
	public static final void init() { }
	
	private UrmusicUIRes() { }
}
