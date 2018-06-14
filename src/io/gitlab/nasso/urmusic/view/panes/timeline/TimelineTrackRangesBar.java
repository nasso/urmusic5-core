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

import io.gitlab.nasso.urmusic.common.event.FocusListener;
import io.gitlab.nasso.urmusic.common.event.TrackListener;
import io.gitlab.nasso.urmusic.controller.UrmusicController;
import io.gitlab.nasso.urmusic.model.project.Track;
import io.gitlab.nasso.urmusic.model.project.Track.TrackActivityRange;
import io.gitlab.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;

public class TimelineTrackRangesBar extends JPanel implements
														TrackListener,
														MouseListener,
														MouseMotionListener,
														KeyListener {
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
			UrmusicController.removeTrackActivityRangeFocusListener(this.rangesFocusListener);
			UrmusicController.removeTrackFocusListener(this.trackFocusListener);
		}
	}
	
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D) g.create();
		
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		int w = this.getWidth();
		int h = this.getHeight();
		int workingWidth = this.timePosToPixels(UrmusicController.getFocusedComposition().getTimeline().getDuration());
		
		g2d.setColor(UrmusicController.getFocusedTrack() == this.track ? TimelineTrackRangesBar.COMP_FOCUSED_BACKGROUND : TimelineTrackRangesBar.COMP_BACKGROUND);
		g2d.fillRect(0, 0, w, h);
		
		g2d.setColor(UrmusicController.getFocusedTrack() == this.track ? TimelineTrackRangesBar.TRACK_FOCUSED_BACKGROUND : TimelineTrackRangesBar.TRACK_BACKGROUND);
		g2d.fillRect(0, 0, Math.min(workingWidth, w), h);
		
		float framerate = UrmusicController.getFocusedComposition().getTimeline().getFramerate();
		if(this.track != null) {
			List<TrackActivityRange> ranges = this.track.getActivityRanges();
			
			float s = this.view.getHorizontalScale();
			
			g2d.translate(this.view.getHorizontalScroll(), 0);
			
			g2d.setStroke(TimelineTrackRangesBar.RANGE_BORDER_STROKE);
			for(int i = 0; i < ranges.size(); i++) {
				TrackActivityRange r = ranges.get(i);

				float start = r.getStart() * framerate;
				float len = r.getLength() * framerate;
				
				g2d.setColor(UrmusicController.getFocusedTrackActivityRange() == r ? TimelineTrackRangesBar.RANGE_FOCUS_COLOR : TimelineTrackRangesBar.RANGE_COLOR);
				g2d.fillRoundRect(
					(int) (start * s),
					1,
					(int) (len * s),
					this.getHeight() - 3,
					8, 8
				);
				
				g2d.setColor(TimelineTrackRangesBar.RANGE_BORDER_COLOR);
				g2d.drawRoundRect(
					(int) (start * s),
					1,
					(int) (len * s),
					this.getHeight() - 3,
					8, 8
				);
			}
		}
		
		g2d.dispose();
	}
	
	public void setTrack(Track track) {
		if(this.track != null) {
			this.track.removeTrackListener(this);
			UrmusicController.removeTrackActivityRangeFocusListener(this.rangesFocusListener);
			UrmusicController.removeTrackFocusListener(this.trackFocusListener);
		}
		
		this.track = track;
		
		if(this.track != null) {
			this.track.addTrackListener(this);
			UrmusicController.addTrackActivityRangeFocusListener(this.rangesFocusListener);
			UrmusicController.addTrackFocusListener(this.trackFocusListener);
		}
	}

	public void rangesChanged(Track source) {
		SwingUtilities.invokeLater(this::repaint);
	}

	private int timePosToPixels(float t) {
		return (int) (t * UrmusicController.getFocusedComposition().getTimeline().getFramerate() * this.view.getHorizontalScale() + this.view.getHorizontalScroll());
	}
	
	private float pixelToTimePos(int x) {
		return (x - this.view.getHorizontalScroll()) / this.view.getHorizontalScale() / UrmusicController.getFocusedComposition().getTimeline().getFramerate();
	}
	
	private TrackActivityRange findRangeEndAt(float x) {
		List<TrackActivityRange> ranges = this.track.getActivityRanges();
		
		TrackActivityRange r = null;
		for(int i = 0; i < ranges.size(); i++) {
			if(Math.abs(this.timePosToPixels((r = ranges.get(i)).getEnd()) - x) < 4)
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
						UrmusicController.moveTrackActivityRange(
							this.pressedOnRange,
							this.pressedOnRange.getStart() + this.pixelToTimePos(e.getX()) - this.pixelToTimePos(this.pressedAtX)
						);
						break;
					case RESIZE_END:
						UrmusicController.setTrackActivityRangeEnd(
							this.pressedOnRange,
							this.pixelToTimePos(e.getX())
						);
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
			this.setCursor(Cursor.getPredefinedCursor(e.getX() < this.timePosToPixels(r.getEnd()) ? Cursor.W_RESIZE_CURSOR : Cursor.E_RESIZE_CURSOR));
			return;
		}
		
		r = this.track.getRangeAt(this.pixelToTimePos(e.getX()));
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
			else if((r = this.track.getRangeAt(this.pixelToTimePos(e.getX()))) != null) this.rangeDragAction = RangeDragAction.MOVE;
			
			// Keep track of where we clicked
			this.pressedOnRange = r;
			this.pressedAtX = e.getX();
			
			// This will focus or unfocus if needed 
			UrmusicController.focusTrack(this.track);
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
}
