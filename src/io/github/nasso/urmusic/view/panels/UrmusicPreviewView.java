package io.github.nasso.urmusic.view.panels;
	
import java.awt.BorderLayout;

import javax.swing.JLabel;

import io.github.nasso.urmusic.view.components.UrmusicViewPane;

public class UrmusicPreviewView extends UrmusicViewPane {
	private static final long serialVersionUID = -761158235222787214L;

	public UrmusicPreviewView() {
		this.setLayout(new BorderLayout());
		
		this.add(new JLabel("Preview!", JLabel.CENTER), BorderLayout.CENTER);
	}

	public void dispose() {
	}
}
