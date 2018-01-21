package io.github.nasso.urmusic.view.components.panels.properties;

import java.awt.BorderLayout;

import javax.swing.JLabel;

import io.github.nasso.urmusic.view.components.UrmViewPane;

public class PropertiesView extends UrmViewPane {
	private static final long serialVersionUID = -896247777042870529L;

	public PropertiesView() {
		this.setLayout(new BorderLayout());
		
		this.add(new JLabel("Properties!", JLabel.CENTER), BorderLayout.CENTER);
	}

	public void dispose() {
	}
}
