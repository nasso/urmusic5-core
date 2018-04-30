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
package io.github.nasso.urmusic.view.components;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JMenu;
import javax.swing.JPanel;

public abstract class UrmViewPane extends JPanel {
	private JPanel menuBar = new JPanel();
	
	public UrmViewPane() {
		this.menuBar.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
		this.menuBar.setLayout(new BoxLayout(this.menuBar, BoxLayout.X_AXIS));
		this.menuBar.setOpaque(false);
	}
	
	public void addMenu(UrmMenu menu) {
		menu.setMaximumSize(menu.getPreferredSize());
		
		this.menuBar.add(menu);
		this.menuBar.add(Box.createHorizontalStrut(4));
	}
	
	public JPanel getMenuBar() {
		return this.menuBar;
	}
	
	public void removeMenu(JMenu menu) {
		this.menuBar.remove(menu);
	}
	
	public abstract void dispose();
}
