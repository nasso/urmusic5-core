/*******************************************************************************
 * urmusic - The Free and Open Source Music Visualizer Tool
 * Copyright (C) 2018  nasso (https://github.com/nasso)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * Contact "nasso": nassomails -at- gmail dot com
 ******************************************************************************/
package io.github.nasso.urmusic.view.components;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import io.github.nasso.urmusic.view.UrmusicView;
import io.github.nasso.urmusic.view.data.UrmusicSplittedPaneState;
import io.github.nasso.urmusic.view.data.UrmusicStrings;
import io.github.nasso.urmusic.view.data.UrmusicUIRes;
import io.github.nasso.urmusic.view.panes.effectlist.EffectListView;
import io.github.nasso.urmusic.view.panes.preview.PreviewView;
import io.github.nasso.urmusic.view.panes.script_editor.ScriptEditorView;
import io.github.nasso.urmusic.view.panes.timeline.TimelineView;

public class SplittablePane extends JPanel {
	private static final Color CONTROL_BAR_BG = Color.LIGHT_GRAY;
	private static final String CARD_MAIN = "main";
	private static final String CARD_SPLIT = "split";
	
	private static final class ViewPaneEntry {
		private Class<? extends UrmViewPane> cls;
		private String displayName;

		private UrmViewPane viewInstance;
		
		public ViewPaneEntry(Class<? extends UrmViewPane> cls, String displayName) {
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
			new ViewPaneEntry(PreviewView.class, UrmusicStrings.getString("view." + PreviewView.VIEW_NAME + ".name")),
			new ViewPaneEntry(EffectListView.class, UrmusicStrings.getString("view." + EffectListView.VIEW_NAME + ".name")),
			new ViewPaneEntry(TimelineView.class, UrmusicStrings.getString("view." + TimelineView.VIEW_NAME + ".name")),
			new ViewPaneEntry(ScriptEditorView.class, UrmusicStrings.getString("view." + ScriptEditorView.VIEW_NAME + ".name")),
	};
	
	private CardLayout cardLayout;
	private JPanel bodyContainer;
	private JPanel controlBar;
	private JPanel menuBarContainer;
	private JSplitPane splitPane;
	private JButton popupButton;
	private JButton verticalSplitButton;
	private JButton horizontalSplitButton;
	private JButton unsplitButton;
	private JComboBox<ViewPaneEntry> viewCombo;
	
	private JFrame ownerFrame;
	private SplittablePane ownerPane;
	private SplittablePane childA, childB;
	
	private boolean split = false;
	
	public SplittablePane() {
		this(null);
	}
	
	public SplittablePane(SplittablePane owner) {
		this(owner, null);
	}
	
	public SplittablePane(SplittablePane owner, JFrame popup) {
		this.ownerPane = owner;
		this.ownerFrame = popup;
		this.buildUI(owner == null ? 0 : owner.viewCombo.getSelectedIndex());
	}

	private void buildUI(int viewID) {
		// View
		this.menuBarContainer = new JPanel(new BorderLayout());
		this.menuBarContainer.setOpaque(false);
		
		this.viewCombo = new JComboBox<>();
		this.viewCombo.setFont(this.viewCombo.getFont().deriveFont(Font.PLAIN, 11f));
		for(int i = 0; i < this.viewPaneEntries.length; i++) this.viewCombo.addItem(this.viewPaneEntries[i]);
		this.viewCombo.addItemListener((e) -> {
			SplittablePane.this.updateView(e);
		});
		this.viewCombo.setMaximumSize(this.viewCombo.getPreferredSize());
		
		this.popupButton = new UrmIconButton(UrmusicUIRes.POPUP_ICON);
		this.verticalSplitButton = new UrmIconButton(UrmusicUIRes.VERTICAL_SPLIT_ICON);
		this.horizontalSplitButton = new UrmIconButton(UrmusicUIRes.HORIZONTAL_SPLIT_ICON);
		this.unsplitButton = new UrmIconButton(UrmusicUIRes.DELETE_ICON);
		this.popupButton.addActionListener((e) -> this.popup());
		this.verticalSplitButton.addActionListener((e) -> this.splitVertically());
		this.horizontalSplitButton.addActionListener((e) -> this.splitHorizontally());
		this.unsplitButton.addActionListener((e) -> {
			if(this.ownerPane != null)
				this.ownerPane.unsplit();
			else if(this.ownerFrame != null)
				this.ownerFrame.dispatchEvent(new WindowEvent(this.ownerFrame, WindowEvent.WINDOW_CLOSING));
		});
		
		this.controlBar = new JPanel();
		BoxLayout ctrlLayout = new BoxLayout(this.controlBar, BoxLayout.X_AXIS);
		this.controlBar.setLayout(ctrlLayout);
		
		this.controlBar.setBorder(new EmptyBorder(2, 2, 2, 2));
		this.controlBar.setBackground(SplittablePane.CONTROL_BAR_BG);

		this.controlBar.add(this.viewCombo);
		this.controlBar.add(Box.createHorizontalStrut(2));
		this.controlBar.add(this.menuBarContainer);
		this.controlBar.add(Box.createHorizontalStrut(2));
		this.controlBar.add(this.popupButton);
		this.controlBar.add(Box.createHorizontalStrut(2));
		this.controlBar.add(this.horizontalSplitButton);
		this.controlBar.add(Box.createHorizontalStrut(2));
		this.controlBar.add(this.verticalSplitButton);
		this.controlBar.add(Box.createHorizontalStrut(2));
		this.controlBar.add(this.unsplitButton);
		
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
		this.add(this.bodyContainer, SplittablePane.CARD_MAIN);
		this.add(this.splitPane, SplittablePane.CARD_SPLIT);
		
		this.cardLayout.show(this, SplittablePane.CARD_MAIN);
		if(viewID == 0) this.updateView(null);
		else this.viewCombo.setSelectedIndex(viewID); 
	}
	
	public void popup() {
		SplittablePane.popupNew(this);
	}
	
	public void splitVertically() {
		if(this.split) return;
		this.split = true;
		
		this.splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		this.splitPane.setTopComponent(this.childA = new SplittablePane(this));
		this.splitPane.setBottomComponent(this.childB = new SplittablePane(this));
		
		this.splitPane.setDividerLocation(0.5f);
		
		this.cardLayout.show(this, SplittablePane.CARD_SPLIT);
	}
	
	public void splitHorizontally() {
		if(this.split) return;
		this.split = true;
		
		this.splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		this.splitPane.setLeftComponent(this.childA = new SplittablePane(this));
		this.splitPane.setRightComponent(this.childB = new SplittablePane(this));
		
		this.splitPane.setDividerLocation(0.5f);
		
		this.cardLayout.show(this, SplittablePane.CARD_SPLIT);
	}
	
	public void unsplit() {
		if(!this.split) return;
		this.split = false;

		this.cardLayout.show(this, SplittablePane.CARD_MAIN);
		
		this.splitPane.setLeftComponent(null);
		this.splitPane.setRightComponent(null);
		
		this.childA.dispose();
		this.childB.dispose();
		this.childA = null;
		this.childB = null;
	}
	
	public UrmusicSplittedPaneState saveState() {
		UrmusicSplittedPaneState state = new UrmusicSplittedPaneState();
		
		state.setSplitted(this.split);
		state.setVertical(this.splitPane.getOrientation() == JSplitPane.VERTICAL_SPLIT);
		state.setViewID(this.viewCombo.getSelectedIndex());
		state.setSplitLocation(this.splitPane.getDividerLocation());
		
		if(this.ownerFrame != null) {
			Point p = this.ownerFrame.getLocationOnScreen();
			state.setPosX(p.x);
			state.setPosY(p.y);
			state.setWidth(this.ownerFrame.getWidth());
			state.setHeight(this.ownerFrame.getHeight());
			state.setExtendedState(this.ownerFrame.getExtendedState());
		}
		
		if(this.split && this.childA != null) {
			state.setStateA(this.childA.saveState());
		}
		
		if(this.split && this.childB != null) {
			state.setStateB(this.childB.saveState());
		}
		
		return state;
	}
	
	public void loadState(UrmusicSplittedPaneState state) {
		if(this.ownerFrame != null) {
			this.ownerFrame.setLocation(state.getPosX(), state.getPosY());
			this.ownerFrame.setSize(state.getWidth(), state.getHeight());
			this.ownerFrame.setExtendedState(state.getExtendedState());
		}
		
		if(state.isSplitted()) {
			if(state.isVertical()) this.splitVertically();
			else this.splitHorizontally();
			
			this.childA.loadState(state.getStateA());
			this.childB.loadState(state.getStateB());
			this.splitPane.setDividerLocation(state.getSplitLocation());
		} else {
			this.viewCombo.setSelectedIndex(state.getViewID());
		}
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
			this.menuBarContainer.add(entry.viewInstance.getMenuBar(), BorderLayout.CENTER);
			
			SwingUtilities.invokeLater(() -> {
				this.bodyContainer.revalidate();
				this.bodyContainer.repaint();
			});
		} else if(event.getStateChange() == ItemEvent.DESELECTED) {
			ViewPaneEntry entry = (ViewPaneEntry) event.getItem();
			
			if(entry.viewInstance != null) {
				this.bodyContainer.remove(entry.viewInstance);
				this.menuBarContainer.remove(entry.viewInstance.getMenuBar());

				SwingUtilities.invokeLater(() -> {
					this.bodyContainer.revalidate();
					this.bodyContainer.repaint();
				});
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
	
	public static SplittablePane popupNew() {
		return SplittablePane.popupNew(null);
	}
	
	public static SplittablePane popupNew(SplittablePane owner) {
		SplittablePane newPane; 

		JFrame popup = new JFrame();
		popup.setVisible(true);
		popup.setSize(800, 600);
		popup.setLocationRelativeTo(null);
		popup.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		popup.setContentPane(newPane = new SplittablePane(owner, popup));
		popup.addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
				newPane.dispose();
			}
		});
		UrmusicView.registerFrame(popup);
		// popup.setVisible(true);
		
		return newPane;
	}
}
