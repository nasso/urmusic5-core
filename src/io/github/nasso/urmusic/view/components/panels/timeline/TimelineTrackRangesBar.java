package io.github.nasso.urmusic.view.components.panels.timeline;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import io.github.nasso.urmusic.controller.UrmusicController;
import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.event.FocusListener;
import io.github.nasso.urmusic.model.event.TrackRangesListener;
import io.github.nasso.urmusic.model.project.Track;
import io.github.nasso.urmusic.model.project.Track.TrackActivityRange;

public class TimelineTrackRangesBar extends JPanel implements
														TrackRangesListener,
														MouseListener,
														MouseMotionListener {
	private static final long serialVersionUID = -1959492010632451757L;
	private static final Color TRACK_BACKGROUND = new Color(0xffffff);
	private static final Color TRACK_FOCUSED_BACKGROUND = new Color(0xdddddd);
	private static final Color RANGE_COLOR = new Color(0xb0d0f2);
	private static final Color RANGE_FOCUS_COLOR = new Color(0x4da0f9); // or 0x6bb4f4
	private static final Color RANGE_BORDER_COLOR = new Color(0x057cfc);
	private static final Stroke RANGE_BORDER_STROKE = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
	
	private TimelineView view;
	private Track track;
	
	private TrackActivityRange pressedOnRange = null;
	private int pressedAtX = 0;
	
	private FocusListener<TrackActivityRange> rangesFocusListener = (oldRange, newRange) -> { this.repaint(); };
	private FocusListener<Track> trackFocusListener = (oldRange, newRange) -> { this.repaint(); };
	
	public TimelineTrackRangesBar(TimelineView view, Track track) {
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		
		this.setTrack(track);
		this.view = view;
	}

	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g.create();
		
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		int w = this.getWidth();
		int h = this.getHeight();
		g2d.setColor(UrmusicModel.getFocusedTrack() == this.getTrack() ? TRACK_FOCUSED_BACKGROUND : TRACK_BACKGROUND);
		g2d.fillRect(0, 0, w, h);
		
		if(this.track != null) {
			List<TrackActivityRange> ranges = this.track.getActivityRangesLengths();
			
			float s = this.view.getHorizontalScale();
			
			g2d.translate(this.view.getHorizontalScroll(), 0);
			
			g2d.setStroke(RANGE_BORDER_STROKE);
			for(int i = 0; i < ranges.size(); i++) {
				TrackActivityRange r = ranges.get(i);

				g2d.setColor(UrmusicModel.getFocusedTrackActivityRange() == r ? RANGE_FOCUS_COLOR : RANGE_COLOR);
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
		if(this.track != null) {
			this.track.removeTrackRangesListener(this);
			UrmusicModel.removeTrackActivityRangeFocusListener(this.rangesFocusListener);
			UrmusicModel.removeTrackFocusListener(this.trackFocusListener);
		}
		
		this.track = track;
		
		if(this.track != null) {
			this.track.addTrackRangesListener(this);
			UrmusicModel.addTrackActivityRangeFocusListener(this.rangesFocusListener);
			UrmusicModel.addTrackFocusListener(this.trackFocusListener);
		}
	}

	public void rangesChanged(Track source) {
		SwingUtilities.invokeLater(this::repaint);
	}

	private int pixelToFrames(int x) {
		return (int) ((x - this.view.getHorizontalScroll()) / this.view.getHorizontalScale());
	}
	
	public void mouseDragged(MouseEvent e) {
		if(this.pressedOnRange != null) {
			this.pressedOnRange.moveTo(this.pressedOnRange.getStart() + this.pixelToFrames(e.getX()) - this.pixelToFrames(this.pressedAtX));
			this.pressedAtX = e.getX();
		}
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1) {
			TrackActivityRange r = this.getTrack().getRangeAt(this.pixelToFrames(e.getX()));
			this.pressedOnRange = r;
			this.pressedAtX = e.getX();
			
			if(r != null) {
				UrmusicController.focusTrackActivityRange(r);
			}
		}
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}
	
	public void focusChanged(Object oldFocus, Object newFocus) {
		this.repaint();
	}
}
