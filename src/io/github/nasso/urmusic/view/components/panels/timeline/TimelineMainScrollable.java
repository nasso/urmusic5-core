package io.github.nasso.urmusic.view.components.panels.timeline;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.event.FrameCursorListener;
import io.github.nasso.urmusic.model.event.TracklistListener;
import io.github.nasso.urmusic.model.project.Track;
import io.github.nasso.urmusic.view.layout.VListLayout;

public class TimelineMainScrollable extends JPanel implements TracklistListener, FrameCursorListener  {
	private static final long serialVersionUID = 1008513031790674759L;
	
	public static final int CHANNEL_HEIGHT = 30;
	public static final int CHANNEL_WIDTH = 150;
	
	private TimelineView view;
	private JPanel infoPane, timelinePane;
	private TimelineCaretLayer caretPane;
	
	private JLayer<JPanel> timelineLayer;
	
	public TimelineMainScrollable(TimelineView p) {
		this.view = p;
		
		this.infoPane = new JPanel(new VListLayout(1));
		this.timelinePane = new JPanel(new VListLayout(1));
		this.caretPane = new TimelineCaretLayer(this.view);
		
		this.infoPane.setBackground(Color.GRAY);
		this.timelinePane.setBackground(Color.LIGHT_GRAY);
		
		this.infoPane.setBorder(BorderFactory.createEmptyBorder(11, 1, 1, 1));
		this.timelinePane.setBorder(BorderFactory.createEmptyBorder(11, 1, 1, 1));
		
		this.infoPane.setPreferredSize(new Dimension(CHANNEL_WIDTH, 0));
		
		this.setLayout(new BorderLayout());
		this.add(this.infoPane, BorderLayout.WEST);
		this.add(this.timelineLayer = new JLayer<>(this.timelinePane, this.caretPane), BorderLayout.CENTER);
		
		List<Track> tracks = UrmusicModel.getFocusedComposition().getTimeline().getTracks();
		for(int i = 0; i < tracks.size(); i++) {
			this.addTrack(i, tracks.get(i));
		}
		
		this.addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent e) {
				if(e.isControlDown()) {
					float f = 1.5f;
					if(e.getWheelRotation() > 0) {
						f = 1 / f;
					}
					
					TimelineMainScrollable.this.view.setHorizontalScale(TimelineMainScrollable.this.view.getHorizontalScale() * f);
				} else {
					float s = e.getWheelRotation() > 0 ? 10 : -10;
					
					TimelineMainScrollable.this.view.setHorizontalScroll(TimelineMainScrollable.this.view.getHorizontalScroll() + s);
				}
				
				TimelineMainScrollable.this.repaint();
			}
		});
	}
	
	public Dimension getPreferredScrollableViewportSize() {
		return this.getPreferredSize();
	}

	private void addTrack(int index, Track track) {
		JLabel nameLabel = new JLabel(track.getName(), JLabel.CENTER);
		nameLabel.setPreferredSize(new Dimension(CHANNEL_WIDTH, CHANNEL_HEIGHT));
		nameLabel.setBackground(Color.WHITE);
		nameLabel.setOpaque(true);
		this.infoPane.add(nameLabel);
		
		TimelineTrackRangesBar ranges = new TimelineTrackRangesBar(this.view, track);
		ranges.setPreferredSize(new Dimension(0, CHANNEL_HEIGHT));
		this.timelinePane.add(ranges);	
	}
	
	private void removeTrack(int index) {
		this.infoPane.remove(index);
		this.timelinePane.remove(index);
	}
	
	public void trackAdded(int index, Track track) {
		SwingUtilities.invokeLater(() -> {
			this.addTrack(index, track);
		});	
	}

	public void trackRemoved(int index, Track track) {
		SwingUtilities.invokeLater(() -> {
			this.removeTrack(index);
		});
	}


	public void frameChanged(int oldPosition, int newPosition) {
		this.timelineLayer.repaint();
	}

}
