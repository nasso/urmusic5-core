package io.github.nasso.urmusic.view.components.panels.timeline;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JScrollPane;

import io.github.nasso.urmusic.view.components.UrmViewPane;

public class TimelineView extends UrmViewPane {
	private static final long serialVersionUID = -5890250765481685754L;
	
	public static final int FRAME_CARET_HEADER_HEIGHT = 12;
	public static final int CHANNEL_HEIGHT = 40;
	public static final int CHANNEL_WIDTH = 140;
	
	private TimelineMainScrollable body;
	
	private float horizontalScale = 1;
	private float horizontalScroll = 0.0f;
	
	public TimelineView() {
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
