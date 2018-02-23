package io.github.nasso.urmusic.view.panes.timeline;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import io.github.nasso.urmusic.common.event.TrackListener;
import io.github.nasso.urmusic.controller.UrmusicController;
import io.github.nasso.urmusic.model.project.Track;
import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.github.nasso.urmusic.view.components.UrmEditableLabel;
import io.github.nasso.urmusic.view.components.UrmIconButton;
import io.github.nasso.urmusic.view.data.UrmusicUIRes;

public class TimelineTrackHead extends JPanel implements TrackListener {
	private Track track;
	
	private UrmEditableLabel nameLabel;
	private JCheckBox enableCheckbox;
	private JButton deleteBtn;
	
	public TimelineTrackHead(Track t) {
		super(new BorderLayout());
		this.setBackground(Color.WHITE);
		this.setOpaque(true);
		
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

	public void effectAdded(Track source, TrackEffectInstance e, int pos) {
	}

	public void effectRemoved(Track source, TrackEffectInstance e, int pos) {
	}

	public void effectMoved(Track source, TrackEffectInstance e, int oldPos, int newPos) {
	}
}
