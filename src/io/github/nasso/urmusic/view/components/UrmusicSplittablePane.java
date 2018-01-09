package io.github.nasso.urmusic.view.components;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;

import io.github.nasso.urmusic.view.UrmusicView;
import io.github.nasso.urmusic.view.data.UrmusicIcons;
import io.github.nasso.urmusic.view.data.UrmusicStrings;
import io.github.nasso.urmusic.view.panels.UrmusicInfoView;
import io.github.nasso.urmusic.view.panels.UrmusicPreviewView;
import io.github.nasso.urmusic.view.panels.UrmusicPropertiesView;
import io.github.nasso.urmusic.view.panels.UrmusicTimelineView;

public class UrmusicSplittablePane extends JPanel {
	private static final long serialVersionUID = 2803211023410018498L;

	private static final Color CONTROL_BAR_BG = new Color(0xBBBBBB);
	private static final String CARD_MAIN = "main";
	private static final String CARD_SPLIT = "split";
	
	private static final class ViewPaneEntry {
		private Class<? extends UrmusicViewPane> cls;
		private String displayName;

		private UrmusicViewPane viewInstance;
		
		public ViewPaneEntry(Class<? extends UrmusicViewPane> cls, String displayName) {
			this.cls = cls;
			this.displayName = displayName;
		}
		
		public String toString() {
			return this.displayName;
		}
		
		public void dispose() {
			if(this.viewInstance != null) this.viewInstance.dispose();
		}
	}
	
	private ViewPaneEntry[] viewPaneEntries = {
			new ViewPaneEntry(UrmusicInfoView.class, UrmusicStrings.getString("view.info.name")),
			new ViewPaneEntry(UrmusicPreviewView.class, UrmusicStrings.getString("view.preview.name")),
			new ViewPaneEntry(UrmusicPropertiesView.class, UrmusicStrings.getString("view.properties.name")),
			new ViewPaneEntry(UrmusicTimelineView.class, UrmusicStrings.getString("view.timeline.name")),
	};
	
	private CardLayout cardLayout;
	private JPanel bodyContainer;
	private JPanel controlBar;
	private JSplitPane splitPane;
	private JButton popupButton;
	private JButton verticalSplitButton;
	private JButton horizontalSplitButton;
	private JButton unsplitButton;
	private JComboBox<ViewPaneEntry> viewCombo;
	
	private JFrame ownerFrame;
	private UrmusicSplittablePane ownerPane;
	private UrmusicSplittablePane childA, childB;
	
	private boolean split = false;
	
	public UrmusicSplittablePane() {
		this(null);
	}
	
	public UrmusicSplittablePane(UrmusicSplittablePane owner) {
		this(owner, null);
	}
	
	public UrmusicSplittablePane(UrmusicSplittablePane owner, JFrame popup) {
		this.ownerPane = owner;
		this.ownerFrame = popup;
		this.buildUI(owner == null ? 0 : owner.viewCombo.getSelectedIndex());
	}

	private void buildUI(int viewID) {
		// View
		this.viewCombo = new JComboBox<>();
		this.viewCombo.setFont(this.viewCombo.getFont().deriveFont(Font.PLAIN, 11f));
		for(int i = 0; i < this.viewPaneEntries.length; i++) this.viewCombo.addItem(this.viewPaneEntries[i]);
		this.viewCombo.addItemListener((e) -> {
			UrmusicSplittablePane.this.updateView(e);
		});
		
		this.popupButton = new UrmIconButton(UrmusicIcons.POPUP_ICON);
		this.verticalSplitButton = new UrmIconButton(UrmusicIcons.VERTICAL_SPLIT_ICON);
		this.horizontalSplitButton = new UrmIconButton(UrmusicIcons.HORIZONTAL_SPLIT_ICON);
		this.unsplitButton = new UrmIconButton(UrmusicIcons.CLOSE_ICON);
		this.popupButton.addActionListener((e) -> this.popup());
		this.verticalSplitButton.addActionListener((e) -> this.splitVertically());
		this.horizontalSplitButton.addActionListener((e) -> this.splitHorizontally());
		this.unsplitButton.addActionListener((e) -> {
			if(this.ownerPane != null)
				this.ownerPane.unsplit();
			else if(this.ownerFrame != null)
				this.ownerFrame.dispatchEvent(new WindowEvent(this.ownerFrame, WindowEvent.WINDOW_CLOSING));
		});
		
		JPanel rightControls = new JPanel(new GridLayout(1, 0, 2, 0));
		rightControls.setOpaque(false);
		rightControls.add(this.popupButton);
		rightControls.add(this.horizontalSplitButton);
		rightControls.add(this.verticalSplitButton);
		rightControls.add(this.unsplitButton);
		
		JPanel leftControls = new JPanel(new GridLayout(1, 0, 2, 0));
		leftControls.setOpaque(false);
		leftControls.add(this.viewCombo);
		
		this.controlBar = new JPanel(new BorderLayout(0, 0));
		this.controlBar.setBorder(new EmptyBorder(2, 2, 2, 2));
		this.controlBar.setBackground(CONTROL_BAR_BG);
		this.controlBar.add(rightControls, BorderLayout.EAST);
		this.controlBar.add(leftControls, BorderLayout.WEST);
		this.controlBar.setPreferredSize(new Dimension(200, 24));
		
		this.bodyContainer = new JPanel(new BorderLayout(0, 0));
		this.bodyContainer.add(this.controlBar, BorderLayout.SOUTH);
		this.bodyContainer.setMinimumSize(new Dimension(200, 24));
		
		// Split view
		this.splitPane = new JSplitPane();
		this.splitPane.setBorder(null);
		this.splitPane.setDividerSize(4);
		this.splitPane.setContinuousLayout(true);
		
		// Main
		this.setLayout(this.cardLayout = new CardLayout());
		this.add(this.bodyContainer, CARD_MAIN);
		this.add(this.splitPane, CARD_SPLIT);
		
		this.cardLayout.show(this, CARD_MAIN);
		if(viewID == 0) this.updateView(null);
		else this.viewCombo.setSelectedIndex(viewID); 
	}
	
	public void popup() {
		popupNew(this);
	}
	
	public void splitVertically() {
		if(this.split) return;
		this.split = true;
		
		this.splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		this.splitPane.setTopComponent(this.childA = new UrmusicSplittablePane(this));
		this.splitPane.setBottomComponent(this.childB = new UrmusicSplittablePane(this));
		
		this.splitPane.setDividerLocation(0.5f);
		
		this.cardLayout.show(this, CARD_SPLIT);
	}
	
	public void splitHorizontally() {
		if(this.split) return;
		this.split = true;
		
		this.splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		this.splitPane.setLeftComponent(this.childA = new UrmusicSplittablePane(this));
		this.splitPane.setRightComponent(this.childB = new UrmusicSplittablePane(this));
		
		this.splitPane.setDividerLocation(0.5f);
		
		this.cardLayout.show(this, CARD_SPLIT);
	}
	
	public void unsplit() {
		if(!this.split) return;
		this.split = false;

		this.cardLayout.show(this, CARD_MAIN);
		
		this.splitPane.setLeftComponent(null);
		this.splitPane.setRightComponent(null);
		
		this.childA.dispose();
		this.childB.dispose();
		this.childA = null;
		this.childB = null;
	}
	
	public void saveState(OutputStream out) {
		// TODO saveState
	}
	
	public void loadState(InputStream in) {
		// TODO loadState
	}
	
	private void updateView(ItemEvent event) {
		if(event == null || event.getStateChange() == ItemEvent.SELECTED) {
			ViewPaneEntry entry = (ViewPaneEntry) this.viewCombo.getSelectedItem();
			
			if(entry.viewInstance == null) {
				try {
					entry.viewInstance = entry.cls.newInstance();
				} catch(InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			
			this.bodyContainer.add(entry.viewInstance, BorderLayout.CENTER);
			this.bodyContainer.revalidate();
			this.bodyContainer.repaint();
		} else if(event.getStateChange() == ItemEvent.DESELECTED) {
			ViewPaneEntry entry = (ViewPaneEntry) event.getItem();
			
			if(entry.viewInstance != null) {
				this.bodyContainer.remove(entry.viewInstance);
				this.bodyContainer.revalidate();
				this.bodyContainer.repaint();
			}
		}
	}
	
	private void dispose() {
		this.ownerPane = null;
		this.ownerFrame = null;
		
		if(this.childA != null) {
			this.childA.dispose();
			this.childA = null;
		}
		
		if(this.childB != null) {
			this.childB.dispose();
			this.childB = null;
		}
		
		for(int i = 0; i < this.viewPaneEntries.length; i++) this.viewPaneEntries[i].dispose();
	}
	
	public static UrmusicSplittablePane popupNew() {
		return popupNew(null);
	}
	
	public static UrmusicSplittablePane popupNew(UrmusicSplittablePane owner) {
		UrmusicSplittablePane newPane; 
		
		JFrame popup = new JFrame(UrmusicStrings.getString("title"));
		popup.setSize(800, 600); // TODO: Maybe custom size for popups?
		popup.setLocationRelativeTo(null);
		popup.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		popup.setContentPane(newPane = new UrmusicSplittablePane(owner, popup));
		popup.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				newPane.dispose();
			}
		});
		UrmusicView.registerFrame(popup);
		popup.setVisible(true);
		
		return newPane;
	}
}
