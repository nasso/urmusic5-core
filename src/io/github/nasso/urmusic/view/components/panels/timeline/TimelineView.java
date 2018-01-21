package io.github.nasso.urmusic.view.components.panels.timeline;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JScrollPane;

import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.view.components.UrmViewPane;

public class TimelineView extends UrmViewPane {
	private static final long serialVersionUID = -5890250765481685754L;
	
	private TimelineMainScrollable body;
	
	public TimelineView() {
		this.setLayout(new BorderLayout());
		
		this.buildUI();
		
		UrmusicModel.getTimeline().addTracklistListener(this.body);
	}

	private void buildUI() {
		this.body = new TimelineMainScrollable();
		
		JScrollPane scrollPane = new JScrollPane(this.body, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(null);
		this.add(scrollPane, BorderLayout.CENTER);
		this.setBackground(Color.LIGHT_GRAY);
	}
	
	public void dispose() {
		UrmusicModel.getTimeline().removeTracklistListener(this.body);
	}
}
