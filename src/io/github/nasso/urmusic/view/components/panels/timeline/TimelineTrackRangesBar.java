package io.github.nasso.urmusic.view.components.panels.timeline;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import io.github.nasso.urmusic.controller.UrmusicController;
import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.event.FocusListener;
import io.github.nasso.urmusic.model.event.TrackListener;
import io.github.nasso.urmusic.model.project.Track;
import io.github.nasso.urmusic.model.project.Track.TrackActivityRange;
import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;

public class TimelineTrackRangesBar extends JPanel implements
														TrackListener,
														MouseListener,
														MouseMotionListener,
														KeyListener {
	private static final long serialVersionUID = -1959492010632451757L;
	private static final Color COMP_BACKGROUND = new Color(0xcccccc);
	private static final Color COMP_FOCUSED_BACKGROUND = new Color(0xbbbbbb);
	private static final Color TRACK_BACKGROUND = new Color(0xffffff);
	private static final Color TRACK_FOCUSED_BACKGROUND = new Color(0xdddddd);
	private static final Color RANGE_COLOR = new Color(0xb0d0f2);
	private static final Color RANGE_FOCUS_COLOR = new Color(0x4da0f9); // or 0x6bb4f4
	private static final Color RANGE_BORDER_COLOR = new Color(0x057cfc);
	private static final Stroke RANGE_BORDER_STROKE = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
	
	private static enum RangeDragAction {
		MOVE,
		RESIZE_END
	}
	
	private TimelineView view;
	private Track track;
	
	private RangeDragAction rangeDragAction = RangeDragAction.MOVE;
	private TrackActivityRange pressedOnRange = null;
	private boolean button1Pressed = false;
	private int pressedAtX = 0;
	
	private FocusListener<TrackActivityRange> rangesFocusListener = (oldRange, newRange) -> { SwingUtilities.invokeLater(this::repaint); };
	private FocusListener<Track> trackFocusListener = (oldRange, newRange) -> { SwingUtilities.invokeLater(this::repaint); };
	
	public TimelineTrackRangesBar(TimelineView view, Track track) {
		this.setFocusable(true);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addKeyListener(this);
		
		this.setTrack(track);
		this.view = view;
	}

	public void dispose() {
		if(this.track != null) {
			this.track.removeTrackListener(this);
			UrmusicModel.removeTrackActivityRangeFocusListener(this.rangesFocusListener);
			UrmusicModel.removeTrackFocusListener(this.trackFocusListener);
		}
	}
	
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g.create();
		
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		int w = this.getWidth();
		int h = this.getHeight();
		int workingWidth = this.framesToPixels(UrmusicModel.getFocusedComposition().getLength());
		
		g2d.setColor(UrmusicModel.getFocusedTrack() == this.getTrack() ? COMP_FOCUSED_BACKGROUND : COMP_BACKGROUND);
		g2d.fillRect(0, 0, w, h);
		
		g2d.setColor(UrmusicModel.getFocusedTrack() == this.getTrack() ? TRACK_FOCUSED_BACKGROUND : TRACK_BACKGROUND);
		g2d.fillRect(0, 0, Math.min(workingWidth, w), h);
		
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
					(int) ((r.getLength() + 1) * s) - 3,
					this.getHeight() - 3
				);
				
				g2d.setColor(RANGE_BORDER_COLOR);
				g2d.drawRect(
					(int) (r.getStart() * s) + 1,
					1,
					(int) ((r.getLength() + 1) * s) - 3,
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
			this.track.removeTrackListener(this);
			UrmusicModel.removeTrackActivityRangeFocusListener(this.rangesFocusListener);
			UrmusicModel.removeTrackFocusListener(this.trackFocusListener);
		}
		
		this.track = track;
		
		if(this.track != null) {
			this.track.addTrackListener(this);
			UrmusicModel.addTrackActivityRangeFocusListener(this.rangesFocusListener);
			UrmusicModel.addTrackFocusListener(this.trackFocusListener);
		}
	}

	public void rangesChanged(Track source) {
		SwingUtilities.invokeLater(this::repaint);
	}

	private int framesToPixels(int f) {
		return (int) (f * this.view.getHorizontalScale() + this.view.getHorizontalScroll());
	}
	
	private int pixelToFrames(int x) {
		return (int) ((x - this.view.getHorizontalScroll()) / this.view.getHorizontalScale());
	}
	
	private TrackActivityRange findRangeEndAt(int x) {
		List<TrackActivityRange> ranges = this.getTrack().getActivityRangesLengths();
		
		TrackActivityRange r = null;
		for(int i = 0; i < ranges.size(); i++) {
			if(Math.abs(this.framesToPixels((r = ranges.get(i)).getEnd() + 1) - x) < 4)
				break;
			
			r = null;
		}
		
		return r;
	}
	
	public void mouseDragged(MouseEvent e) {
		if(this.button1Pressed) {
			if(this.pressedOnRange != null) {
				switch(this.rangeDragAction) {
					case MOVE:
						this.pressedOnRange.moveTo(this.pressedOnRange.getStart() + this.pixelToFrames(e.getX()) - this.pixelToFrames(this.pressedAtX));
						break;
					case RESIZE_END:
						this.pressedOnRange.setEnd(this.pixelToFrames(e.getX() - (int) (this.view.getHorizontalScale() / 2)));
						break;
				}
				
				this.pressedAtX = e.getX();
			}
		}
	}

	public void mouseMoved(MouseEvent e) {
		// Search for an end
		TrackActivityRange r = this.findRangeEndAt(e.getX());
		
		if(r != null) {
			this.setCursor(Cursor.getPredefinedCursor(e.getX() < this.framesToPixels(r.getEnd() + 1) ? Cursor.W_RESIZE_CURSOR : Cursor.E_RESIZE_CURSOR));
			return;
		}
		
		r = this.getTrack().getRangeAt(this.pixelToFrames(e.getX()));
		if(r != null) {
			if(this.getCursor().getType() != Cursor.MOVE_CURSOR) this.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			return;
		}
		
		if(this.isCursorSet()) {
			this.setCursor(null);
		}
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1) {
			this.button1Pressed = true;
			
			TrackActivityRange r = null;
			if((r = this.findRangeEndAt(e.getX())) != null) this.rangeDragAction = RangeDragAction.RESIZE_END;
			else if((r = this.getTrack().getRangeAt(this.pixelToFrames(e.getX()))) != null) this.rangeDragAction = RangeDragAction.MOVE;
			
			// Keep track of where we clicked
			this.pressedOnRange = r;
			this.pressedAtX = e.getX();
			
			// This will focus or unfocus if needed 
			UrmusicController.focusTrack(this.getTrack());
			UrmusicController.focusTrackActivityRange(r);
		}
		
		this.requestFocusInWindow();
	}

	public void mouseReleased(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1)
			this.button1Pressed = false;
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}
	
	public void focusChanged(Object oldFocus, Object newFocus) {
		SwingUtilities.invokeLater(() -> this.repaint());
	}
	
	public void keyTyped(KeyEvent e) {
	}
	
	public void keyPressed(KeyEvent e) {
		switch(e.getKeyCode()) {
			case KeyEvent.VK_DELETE:
				UrmusicController.deleteFocusedTrackActivityRange();
				break;
		}
	}
	
	public void keyReleased(KeyEvent e) {
	}

	public void effectAdded(Track source, TrackEffectInstance e, int pos) {
	}

	public void effectRemoved(Track source, TrackEffectInstance e, int pos) {
	}

	public void effectMoved(Track source, TrackEffectInstance e, int oldPos, int newPos) {
	}
	
	public void enabledStateChanged(Track source, boolean enabledNow) {
	}

	public void nameChanged(Track source, String newName) {
	}

	public void dirtyFlagged(Track source) {
	}
}
