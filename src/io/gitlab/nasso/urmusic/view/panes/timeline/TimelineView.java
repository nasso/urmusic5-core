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
package io.gitlab.nasso.urmusic.view.panes.timeline;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import io.gitlab.nasso.urmusic.controller.UrmusicController;
import io.gitlab.nasso.urmusic.view.components.UrmMenu;
import io.gitlab.nasso.urmusic.view.components.UrmViewPane;
import io.gitlab.nasso.urmusic.view.data.UrmusicStrings;

public class TimelineView extends UrmViewPane {
	public static final String VIEW_NAME = "timeline";
	
	public static final int FRAME_CARET_HEADER_HEIGHT = 12;
	public static final int CHANNEL_HEIGHT = 40;
	public static final int CHANNEL_WIDTH = 140;
	
	private TimelineMainScrollable body;
	private TimelineStatusBar statusBar;
	
	private float horizontalScale = 1;
	private float horizontalScroll = 0.0f;
	
	public TimelineView() {
		// -- menu -- 
		// Add
		this.addMenu(new UrmMenu(UrmusicStrings.getString("view." + VIEW_NAME + ".menu.add"),
			new JMenuItem(new AbstractAction(UrmusicStrings.getString("view." + VIEW_NAME + ".menu.add.track.empty")) {
				public void actionPerformed(ActionEvent e) {
					UrmusicController.addEmptyTrack();
				}
			})
		));
		
		this.setLayout(new BorderLayout());
		
		this.buildUI();
	}
	
	private void buildUI() {
		this.body = new TimelineMainScrollable(this);
		
		JScrollPane scrollPane = new JScrollPane(this.body, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(null);
		this.add(scrollPane, BorderLayout.CENTER);
		this.setBackground(Color.LIGHT_GRAY);
		
		this.add(this.statusBar = new TimelineStatusBar(this), BorderLayout.NORTH);
	}
	
	public void dispose() {
		this.body.dispose();
		this.statusBar.dispose();
	}
	
	public float getHorizontalScale() {
		return this.horizontalScale;
	}
	
	public void setHorizontalScale(float horizontalScale) {
		this.horizontalScale = Math.max(Math.min(horizontalScale, 100f), 0.1f);
	}
	
	public float getHorizontalScroll() {
		return this.horizontalScroll;
	}
	
	public void setHorizontalScroll(float horizontalScroll) {
		this.horizontalScroll = Math.min(horizontalScroll, 0);
	}
}
