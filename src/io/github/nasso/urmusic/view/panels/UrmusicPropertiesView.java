package io.github.nasso.urmusic.view.panels;

import java.awt.BorderLayout;

import javax.swing.JLabel;

import io.github.nasso.urmusic.view.components.UrmusicViewPane;

public class UrmusicPropertiesView extends UrmusicViewPane {
	private static final long serialVersionUID = -896247777042870529L;

	public UrmusicPropertiesView() {
		this.setLayout(new BorderLayout());
		
		this.add(new JLabel("Properties!", JLabel.CENTER), BorderLayout.CENTER);
	}

	public void dispose() {
	}
}
