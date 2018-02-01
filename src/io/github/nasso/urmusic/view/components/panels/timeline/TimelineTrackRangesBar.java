package io.github.nasso.urmusic.view.components.panels.timeline;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import io.github.nasso.urmusic.model.event.TrackRangesListener;
import io.github.nasso.urmusic.model.project.Track;
import io.github.nasso.urmusic.utils.IntRange;

public class TimelineTrackRangesBar extends JPanel implements TrackRangesListener {
	private static final long serialVersionUID = -1959492010632451757L;
	private static final Color RANGE_COLOR = new Color(0xeeeeee);
	// private static final Color RANGE_SELECTED_COLOR = new Color(0x6bb4f4);
	private static final Color RANGE_BORDER_COLOR = new Color(0x057cfc);
	private static final Stroke RANGE_BORDER_STROKE = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
	
	private TimelineView view;
	private Track track;
	
	public TimelineTrackRangesBar(TimelineView view, Track track) {
		this.setTrack(track);
		this.view = view;
	}

	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g.create();
		
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		int w = this.getWidth();
		int h = this.getHeight();
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, w, h);
		
		if(this.track != null) {
			List<IntRange> ranges = this.track.getActivityRangesLengths();
			
			float s = this.view.getHorizontalScale();
			
			g2d.translate(this.view.getHorizontalScroll(), 0);
			
			g2d.setStroke(RANGE_BORDER_STROKE);
			for(int i = 0; i < ranges.size(); i++) {
				IntRange r = ranges.get(i);

				g2d.setColor(RANGE_COLOR);
				g2d.fillRect(
					(int) (r.getStart() * s) + 1,
					1,
					(int) ((r.getEnd() - r.getStart()) * s) - 3,
					this.getHeight() - 3
				);
				
				g2d.setColor(RANGE_BORDER_COLOR);
				g2d.drawRect(
					(int) (r.getStart() * s) + 1,
					1,
					(int) ((r.getEnd() - r.getStart()) * s) - 3,
					this.getHeight() - 3
				);
			}
		}
		
		g2d.dispose();
	}
	
	public Track getTrack() {
		return this.track;
	}

	public void setTrack(Track track) {
		if(this.track != null) this.track.removeTrackRangesListener(this);
		this.track = track;
		if(this.track != null) this.track.addTrackRangesListener(this);
	}

	public void rangesChanged(Track source) {
		SwingUtilities.invokeLater(this::repaint);
	}

}
