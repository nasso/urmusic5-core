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
package io.gitlab.nasso.urmusic.view.panes.timeline;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import io.gitlab.nasso.urmusic.common.event.TrackListener;
import io.gitlab.nasso.urmusic.controller.UrmusicController;
import io.gitlab.nasso.urmusic.model.project.Timeline;
import io.gitlab.nasso.urmusic.model.project.Track;
import io.gitlab.nasso.urmusic.model.project.VideoEffect.VideoEffectInstance;
import io.gitlab.nasso.urmusic.view.components.UrmEditableLabel;
import io.gitlab.nasso.urmusic.view.components.UrmIconButton;
import io.gitlab.nasso.urmusic.view.data.UrmusicStrings;
import io.gitlab.nasso.urmusic.view.data.UrmusicUIRes;

public class TimelineTrackHead extends JPanel implements TrackListener {
	private Track track;
	private Timeline timeline;
	
	private UrmEditableLabel nameLabel;
	private JCheckBox enableCheckbox;
	private JButton deleteBtn;
	
	private JPopupMenu popup;
	
	public TimelineTrackHead(Track t, Timeline tl) {
		super(new BorderLayout());
		this.setBackground(Color.WHITE);
		this.setOpaque(true);
		
		this.timeline = tl;
		
		this.popup = new JPopupMenu();
		this.popup.add(new JMenuItem(new AbstractAction(UrmusicStrings.getString("view.timeline.track.menu.moveUp")) {
			public void actionPerformed(ActionEvent e) {
				UrmusicController.moveTrackUp(TimelineTrackHead.this.timeline, TimelineTrackHead.this.track);
			}
		}));
		this.popup.add(new JMenuItem(new AbstractAction(UrmusicStrings.getString("view.timeline.track.menu.moveDown")) {
			public void actionPerformed(ActionEvent e) {
				UrmusicController.moveTrackDown(TimelineTrackHead.this.timeline, TimelineTrackHead.this.track);
			}
		}));
		this.setComponentPopupMenu(this.popup);
		
		this.nameLabel = new UrmEditableLabel((l) -> {
			UrmusicController.renameTrack(t, l.getValue());
		});
		this.nameLabel.setValue(t.getName());
		this.nameLabel.setOpaque(false);
		
		JPanel nameContainer = new JPanel();
		nameContainer.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		nameContainer.setOpaque(false);
		nameContainer.setLayout(new BorderLayout());
		
		nameContainer.add(this.nameLabel, BorderLayout.CENTER);
		
		this.add(nameContainer, BorderLayout.CENTER);
		
		this.enableCheckbox = new JCheckBox();
		this.enableCheckbox.setSelected(t.isEnabled());
		this.enableCheckbox.setAlignmentX(1.0f);
		this.enableCheckbox.setBorder(null);
		this.enableCheckbox.setOpaque(false);
		this.enableCheckbox.addActionListener((e) -> {
			UrmusicController.setTrackEnabled(this.track, this.enableCheckbox.isSelected());
		});
		
		this.deleteBtn = new UrmIconButton(UrmusicUIRes.DELETE_ICON_S);
		this.deleteBtn.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		this.deleteBtn.setAlignmentX(1.0f);
		this.deleteBtn.addActionListener((e) -> {
			UrmusicController.deleteTrack(this.track);
		});
		
		JPanel lilPanel = new JPanel();
		lilPanel.setOpaque(false);
		lilPanel.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 2));
		
		lilPanel.setLayout(new BoxLayout(lilPanel, BoxLayout.Y_AXIS));
		lilPanel.add(this.deleteBtn);
		lilPanel.add(Box.createVerticalGlue());
		lilPanel.add(this.enableCheckbox);
		
		this.add(lilPanel, BorderLayout.EAST);

		this.nameLabel.setMaximumSize(new Dimension(TimelineView.CHANNEL_WIDTH - lilPanel.getPreferredSize().width - 16, TimelineView.CHANNEL_HEIGHT));
		
		this.setTrack(t);
	}

	public void dispose() {
		if(this.track != null) {
			this.track.removeTrackListener(this);
		}
	}

	public void setTrack(Track t) {
		if(this.track != null) {
			this.track.removeTrackListener(this);
		}
		
		this.track = t;
		
		if(this.track != null) {
			this.nameLabel.setValue(this.track.getName());
			
			this.track.addTrackListener(this);
		}
	}

	public void nameChanged(Track source, String newName) {
		SwingUtilities.invokeLater(() -> this.nameLabel.setValue(newName));
	}

	public void enabledStateChanged(Track source, boolean isEnabledNow) {
		SwingUtilities.invokeLater(() -> this.enableCheckbox.setSelected(isEnabledNow));
	}

	public void rangesChanged(Track source) {
	}

	public void effectAdded(Track source, VideoEffectInstance e, int pos) {
	}

	public void effectRemoved(Track source, VideoEffectInstance e, int pos) {
	}

	public void effectMoved(Track source, VideoEffectInstance e, int oldPos, int newPos) {
	}
}
