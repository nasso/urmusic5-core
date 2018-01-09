package io.github.nasso.urmusic.view.panels;

import java.awt.BorderLayout;

import javax.swing.JLabel;

import io.github.nasso.urmusic.view.components.UrmusicViewPane;

public class UrmusicTimelineView extends UrmusicViewPane {
	private static final long serialVersionUID = -5890250765481685754L;

	public UrmusicTimelineView() {
		this.setLayout(new BorderLayout());
		
		this.add(new JLabel("Timeline!", JLabel.CENTER), BorderLayout.CENTER);
	}

	public void dispose() {
	}
}
