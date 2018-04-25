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
