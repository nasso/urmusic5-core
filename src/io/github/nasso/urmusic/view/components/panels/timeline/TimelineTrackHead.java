package io.github.nasso.urmusic.view.components.panels.timeline;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import io.github.nasso.urmusic.controller.UrmusicController;
import io.github.nasso.urmusic.model.event.TrackListener;
import io.github.nasso.urmusic.model.project.Track;
import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.github.nasso.urmusic.view.components.UrmIconButton;
import io.github.nasso.urmusic.view.data.UrmusicUIRes;

public class TimelineTrackHead extends JPanel implements TrackListener {
	private static final long serialVersionUID = -7262310150252521358L;

	private Track track;
	
	private JLabel nameLabel;
	private JCheckBox enableCheckbox;
	private JButton deleteBtn;
	
	public TimelineTrackHead(Track t) {
		super(new BorderLayout());
		this.setBackground(Color.WHITE);
		this.setOpaque(true);
		
		this.nameLabel = new JLabel(t.getName(), JLabel.CENTER);
		this.nameLabel.setOpaque(false);
		this.add(this.nameLabel, BorderLayout.CENTER);
		
		this.enableCheckbox = new JCheckBox();
		this.enableCheckbox.setSelected(t.isEnabled());
		this.enableCheckbox.setAlignmentX(1.0f);
		this.enableCheckbox.setBorder(null);
		this.enableCheckbox.setOpaque(false);
		this.enableCheckbox.addActionListener((e) -> {
			t.setEnabled(this.enableCheckbox.isSelected());
		});
		
		this.deleteBtn = new UrmIconButton(UrmusicUIRes.DELETE_ICON_S);
		this.deleteBtn.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		this.deleteBtn.setAlignmentX(1.0f);
		this.deleteBtn.addActionListener((e) -> {
			UrmusicController.deleteTrack(this.track);
		});
		
		JPanel lilPanel = new JPanel();
		lilPanel.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 2));
		lilPanel.setOpaque(false);
		
		BoxLayout bl = new BoxLayout(lilPanel, BoxLayout.Y_AXIS);
		
		lilPanel.setLayout(bl);
		lilPanel.add(this.deleteBtn);
		lilPanel.add(Box.createVerticalGlue());
		lilPanel.add(this.enableCheckbox);
		
		this.add(lilPanel, BorderLayout.EAST);
		
		this.setTrack(t);
	}

	public void dispose() {
		if(this.track != null) {
			this.track.removeTrackListener(this);
		}
	}
	
	public Track getTrack() {
		return this.track;
	}

	public void setTrack(Track t) {
		if(this.track != null) {
			this.track.removeTrackListener(this);
		}
		
		this.track = t;
		
		if(this.track != null) {
			this.nameLabel.setText(this.track.getName());
			
			this.track.addTrackListener(this);
		}
	}

	public void nameChanged(Track source, String newName) {
		SwingUtilities.invokeLater(() -> this.nameLabel.setText(newName));
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
