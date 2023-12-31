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
package io.gitlab.nasso.urmusic.view.panes.effectlist;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import io.gitlab.nasso.urmusic.model.UrmusicModel;
import io.gitlab.nasso.urmusic.model.project.VideoEffect;
import io.gitlab.nasso.urmusic.view.data.UrmusicStrings;

public class EffectPickerDialog extends JDialog {
	private static class TrackEffectListCellRenderer extends JLabel implements ListCellRenderer<VideoEffect> {
		private static Color BACKGROUND_SELECTED = new Color(0x99b0d0f2, true);
		private static Color BACKGROUND_SELECTED_FOCUSED = new Color(0xAAb0d0f2, true);
		private static Border UNFOCUS_BORDER = BorderFactory.createEmptyBorder(3, 4, 3, 4);
		private static Border FOCUS_BORDER = BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(0x4da0f9)),
			BorderFactory.createEmptyBorder(2, 3, 2, 3)	
		);
		
		private TrackEffectListCellRenderer() {
		}
		
		public Component getListCellRendererComponent(JList<? extends VideoEffect> list, VideoEffect value, int index, boolean isSelected, boolean cellHasFocus) {
			this.setText(UrmusicStrings.getString(UrmusicModel.getSourcePackage(value), "effect." + value.getEffectClassID() + ".name"));
			
			this.setOpaque(isSelected);
			this.setBackground(cellHasFocus ? BACKGROUND_SELECTED_FOCUSED : BACKGROUND_SELECTED);			
			this.setBorder(cellHasFocus ? FOCUS_BORDER : UNFOCUS_BORDER);
			
			return this;
		}
	}
	
	private static class TrackEffectListModel implements ListModel<VideoEffect> {
		private List<VideoEffect> entries = new ArrayList<>();
		private List<ListDataListener> listeners = new ArrayList<>();
		
		public TrackEffectListModel() {
		}
		
		public void update() {
			Set<String> loadedEffectsIds = UrmusicModel.getLoadedEffects().keySet();
			
			if(this.entries.size() < loadedEffectsIds.size()) {
				this.entries.clear();
				
				for(String id : loadedEffectsIds) {
					this.entries.add(UrmusicModel.getLoadedEffects().get(id));
				}
				
				for(ListDataListener l : this.listeners) {
					l.contentsChanged(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, this.entries.size() - 1));
				}
			}
		}
		
		public int getSize() {
			return this.entries.size();
		}

		public VideoEffect getElementAt(int index) {
			return this.entries.get(index);
		}

		public void addListDataListener(ListDataListener l) {
			this.listeners.add(l);
		}

		public void removeListDataListener(ListDataListener l) {
			this.listeners.remove(l);
		}
	}
	
	private Consumer<List<VideoEffect>> onPick;
	
	private JList<VideoEffect> selectionList;
	
	private TrackEffectListCellRenderer listCellRenderer = new TrackEffectListCellRenderer();
	private TrackEffectListModel listModel = new TrackEffectListModel();
	
	public EffectPickerDialog(String title, Consumer<List<VideoEffect>> onPick) {
		this.onPick = onPick;
		
		this.setTitle(title);
		this.buildUI();
		this.setMinimumSize(new Dimension(360, 340));
		this.setLocationRelativeTo(null);
	}
	
	public void showDialog() {
		this.listModel.update();
		this.setVisible(true);
	}
	
	private void sendResults() {
		this.setVisible(false);
		this.onPick.accept(this.selectionList.getSelectedValuesList());
	}
	
	private void buildUI() {
		this.selectionList = new JList<>(this.listModel);
		this.selectionList.setBorder(null);
		this.selectionList.setOpaque(false);
		this.selectionList.setCellRenderer(this.listCellRenderer);
		this.selectionList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		JPanel container = new JPanel(new BorderLayout(0, 8));
		container.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		
		JPanel controlPane = new JPanel();
		controlPane.setLayout(new BoxLayout(controlPane, BoxLayout.X_AXIS));
		
		controlPane.add(new JButton(new AbstractAction(UrmusicStrings.getString("dialog.global.cancel")) {
			public void actionPerformed(ActionEvent e) {
				EffectPickerDialog.this.setVisible(false);
			}
		}));
		controlPane.add(Box.createHorizontalGlue());
		controlPane.add(new JButton(new AbstractAction(UrmusicStrings.getString("dialog.global.ok")) {
			public void actionPerformed(ActionEvent e) {
				EffectPickerDialog.this.sendResults();
			}
		}));
		
		JScrollPane scrollListPane = new JScrollPane(this.selectionList);
		scrollListPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		
		container.add(scrollListPane, BorderLayout.CENTER);
		container.add(controlPane, BorderLayout.SOUTH);
		
		this.setContentPane(container);
	}
}
