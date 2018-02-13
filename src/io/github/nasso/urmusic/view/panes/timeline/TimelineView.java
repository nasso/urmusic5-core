package io.github.nasso.urmusic.view.panes.timeline;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;

import io.github.nasso.urmusic.controller.UrmusicController;
import io.github.nasso.urmusic.view.components.UrmMenu;
import io.github.nasso.urmusic.view.components.UrmViewPane;
import io.github.nasso.urmusic.view.data.UrmusicStrings;

public class TimelineView extends UrmViewPane {
	public static final String VIEW_NAME = "timeline";
	
	public static final int FRAME_CARET_HEADER_HEIGHT = 12;
	public static final int CHANNEL_HEIGHT = 40;
	public static final int CHANNEL_WIDTH = 140;
	
	private TimelineMainScrollable body;
	
	private float horizontalScale = 1;
	private float horizontalScroll = 0.0f;
	
	public TimelineView() {
		// -- menu -- 
		// Add
		this.addMenu(new UrmMenu(UrmusicStrings.getString("view." + VIEW_NAME + ".menu.add"),
			new JMenuItem(new AbstractAction(UrmusicStrings.getString("view." + VIEW_NAME + ".menu.add.track")) {
				public void actionPerformed(ActionEvent e) {
					UrmusicController.addTrack();
				}
			})
		));
		
		this.setLayout(new BorderLayout());
		
		this.buildUI();
	}
	
	private void buildUI() {
		this.body = new TimelineMainScrollable(this);
		
		JScrollPane scrollPane = new JScrollPane(this.body, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(null);
		this.add(scrollPane, BorderLayout.CENTER);
		this.setBackground(Color.LIGHT_GRAY);
	}
	
	public void dispose() {
		this.body.dispose();
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
