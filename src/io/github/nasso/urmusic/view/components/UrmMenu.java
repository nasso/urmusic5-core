package io.github.nasso.urmusic.view.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class UrmMenu extends JPanel implements MouseListener {
	private static final long serialVersionUID = -4589860633748681474L;
	private static final Color MENU_BACKGROUND = Color.LIGHT_GRAY;
	private static final Color MENU_HOVER_BACKGROUND = new Color(0xAAAAAA);
	
	private static boolean global_open = false;
	private boolean open = false;
	
	private JPopupMenu popup;
	
	public UrmMenu(String label, JComponent... items) {
		this.popup = new JPopupMenu();
		for(int i = items.length - 1; i >= 0; i--)
			this.popup.add(items[i]);
		
		this.popup.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				SwingUtilities.invokeLater(() -> {
					global_open = UrmMenu.this.open = true;
				});
			}
			
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				SwingUtilities.invokeLater(() -> {
					global_open = UrmMenu.this.open = false;
				});
			}
			
			public void popupMenuCanceled(PopupMenuEvent e) {
				SwingUtilities.invokeLater(() -> {
					global_open = UrmMenu.this.open = false;
				});
			}
		});
		
		this.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
		this.setLayout(new BorderLayout());
		this.add(new JLabel(label));
		
		this.setBackground(MENU_BACKGROUND);
		
		this.addMouseListener(this);
	}

	private void popup() {
		this.popup.show(this, 0, -this.popup.getPreferredSize().height);
	}
	
	public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1) {
			if(!this.open) this.popup();
		}
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
		if(global_open)
			this.popup();
		
		this.setBackground(MENU_HOVER_BACKGROUND);
		this.repaint();
	}

	public void mouseExited(MouseEvent e) {
		this.setBackground(MENU_BACKGROUND);
		this.repaint();
	}
}
