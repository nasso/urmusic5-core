package io.github.nasso.urmusic.view.components;

import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

public class UrmIconButton extends JButton {
	public UrmIconButton(ImageIcon icon) {
		super(icon);
		
		this.setOpaque(false);
		this.setMargin(new Insets(0, 0, 0, 0));
		this.setBorder(new EmptyBorder(2, 2, 2, 2));
	}
}
