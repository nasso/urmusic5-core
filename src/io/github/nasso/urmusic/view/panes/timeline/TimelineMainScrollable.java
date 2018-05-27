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
package io.github.nasso.urmusic.view.panes.timeline;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import io.github.nasso.urmusic.common.MathUtils;
import io.github.nasso.urmusic.common.event.FocusListener;
import io.github.nasso.urmusic.common.event.FrameCursorListener;
import io.github.nasso.urmusic.common.event.MultiFocusListener;
import io.github.nasso.urmusic.common.event.TimelineListener;
import io.github.nasso.urmusic.common.event.VideoRendererListener;
import io.github.nasso.urmusic.controller.UrmusicController;
import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.project.Composition;
import io.github.nasso.urmusic.model.project.Timeline;
import io.github.nasso.urmusic.model.project.Track;
import io.github.nasso.urmusic.model.project.TrackEffect;
import io.github.nasso.urmusic.model.project.param.EffectParam;
import io.github.nasso.urmusic.view.layout.VListLayout;

public class TimelineMainScrollable extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener, TimelineListener, FrameCursorListener, VideoRendererListener, FocusListener<Composition>, MultiFocusListener<EffectParam<?>> {
	private TimelineView view;
	private JPanel infoPane, timelinePane;
	private TimelineCaretLayer caretPane;
	
	private JLayer<JPanel> timelineLayer;
	
	private boolean mouseButton1Pressed = false;
	
	public TimelineMainScrollable(TimelineView p) {
		this.view = p;
		
		this.infoPane = new JPanel(new VListLayout(1));
		this.timelinePane = new JPanel(new VListLayout(1));
		this.caretPane = new TimelineCaretLayer(this.view);
		
		this.infoPane.setBackground(Color.GRAY);
		this.timelinePane.setBackground(Color.LIGHT_GRAY);
		
		this.infoPane.setBorder(BorderFactory.createEmptyBorder(TimelineView.FRAME_CARET_HEADER_HEIGHT + 1, 1, 1, 1));
		this.timelinePane.setBorder(BorderFactory.createEmptyBorder(TimelineView.FRAME_CARET_HEADER_HEIGHT + 1, 1, 1, 1));
		
		this.infoPane.setPreferredSize(new Dimension(TimelineView.CHANNEL_WIDTH, 0));
		
		this.setLayout(new BorderLayout());
		this.add(this.infoPane, BorderLayout.WEST);
		this.add(this.timelineLayer = new JLayer<>(this.timelinePane, this.caretPane), BorderLayout.CENTER);
		
		Timeline tl = UrmusicController.getFocusedComposition().getTimeline();
		List<Track> tracks = tl.getTracks();
		for(int i = 0; i < tracks.size(); i++) {
			this.addTrack(i, tracks.get(i), tl);
		}
		
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addMouseWheelListener(this);
		
		UrmusicController.getFocusedComposition().getTimeline().addTimelineListener(this);
		UrmusicController.addFrameCursorListener(this);
		UrmusicController.addCompositionFocusListener(this);
		UrmusicController.addEffectParameterFocusListener(this);
		UrmusicModel.getVideoRenderer().addVideoRendererListener(this);
	}
	
	public void dispose() {
		UrmusicController.getFocusedComposition().getTimeline().removeTimelineListener(this);
		UrmusicController.removeFrameCursorListener(this);
		UrmusicController.removeCompositionFocusListener(this);
		UrmusicController.removeEffectParameterFocusListener(this);
		UrmusicModel.getVideoRenderer().removeRendererListener(this);
		
		for(Component c : this.infoPane.getComponents()) {
			if(c instanceof TimelineTrackHead) ((TimelineTrackHead) c).dispose();
		}
		
		for(Component c : this.timelinePane.getComponents()) {
			if(c instanceof TimelineTrackRangesBar) ((TimelineTrackRangesBar) c).dispose();
		}
	}
	
	public void focusChanged(Composition oldFocus, Composition newFocus) {
		Composition oldFocusComp = oldFocus;
		Composition newFocusComp = newFocus;
		
		if(oldFocusComp != null) oldFocusComp.getTimeline().removeTimelineListener(this);
		if(newFocusComp != null) newFocusComp.getTimeline().addTimelineListener(this);
		
		this.removeAllTracks();
		
		if(newFocus != null) {
			List<Track> tracks = newFocus.getTimeline().getTracks();
			for(int i = 0; i < tracks.size(); i++) {
				this.addTrack(i, tracks.get(i), newFocus.getTimeline());
			}
		}
	}
	
	public void focused(EffectParam<?> o) {
		SwingUtilities.invokeLater(this.timelineLayer::repaint);
	}
	
	public void unfocused(EffectParam<?> o) {
		SwingUtilities.invokeLater(this.timelineLayer::repaint);
	}
	
	public Dimension getPreferredScrollableViewportSize() {
		return this.getPreferredSize();
	}
	
	private void addTrack(int index, Track track, Timeline tl) {
		TimelineTrackHead head = new TimelineTrackHead(track, tl);
		head.setPreferredSize(new Dimension(TimelineView.CHANNEL_WIDTH, TimelineView.CHANNEL_HEIGHT));
		this.infoPane.add(head);
		
		TimelineTrackRangesBar ranges = new TimelineTrackRangesBar(this.view, track);
		ranges.setPreferredSize(new Dimension(0, TimelineView.CHANNEL_HEIGHT));
		this.timelinePane.add(ranges);
		
		this.revalidate();
		this.repaint();
	}
	
	private void removeAllTracks() {
		while(this.infoPane.getComponentCount() != 0) {
			TimelineTrackHead head = (TimelineTrackHead) this.infoPane.getComponent(0);
			head.dispose();
			
			this.infoPane.remove(0);
		}
		
		while(this.timelinePane.getComponentCount() != 0) {
			TimelineTrackRangesBar ranges = (TimelineTrackRangesBar) this.timelinePane.getComponent(0);
			ranges.dispose();
			
			this.timelinePane.remove(0);
		}
		
		this.revalidate();
		this.repaint();
	}
	
	private void removeTrack(int index) {
		TimelineTrackHead head = (TimelineTrackHead) this.infoPane.getComponent(index);
		head.dispose();
		
		TimelineTrackRangesBar ranges = (TimelineTrackRangesBar) this.timelinePane.getComponent(index);
		ranges.dispose();
		
		this.infoPane.remove(index);
		this.timelinePane.remove(index);
		
		this.revalidate();
		this.repaint();
	}
	
	private void moveTrack(int oldIndex, int newIndex) {
		TimelineTrackHead head = (TimelineTrackHead) this.infoPane.getComponent(oldIndex);
		TimelineTrackRangesBar ranges = (TimelineTrackRangesBar) this.timelinePane.getComponent(oldIndex);
		
		this.infoPane.remove(oldIndex);
		this.timelinePane.remove(oldIndex);
		
		this.infoPane.add(head, newIndex);
		this.timelinePane.add(ranges, newIndex);
	}
	
	public void trackAdded(Timeline src, int index, Track track) {
		SwingUtilities.invokeLater(() -> this.addTrack(index, track, src));
	}
	
	public void trackRemoved(Timeline src, int index, Track track) {
		SwingUtilities.invokeLater(() -> this.removeTrack(index));
	}

	public void trackMoved(Timeline src, int oldIndex, int newIndex, Track track) {
		SwingUtilities.invokeLater(() -> this.moveTrack(oldIndex, newIndex));
	}
	
	public void durationChanged(Timeline src) {
	}

	public void framerateChanged(Timeline src) {
	}
	
	public void frameChanged(int oldPosition, int newPosition) {
		// Auto scroll
		int cursorXPos = this.framesToPixels(newPosition);
		
		if(cursorXPos > this.getWidth()) {
			this.view.setHorizontalScroll(this.view.getHorizontalScroll() - cursorXPos + TimelineView.CHANNEL_WIDTH);
		} else if(cursorXPos < TimelineView.CHANNEL_WIDTH) {
			this.view.setHorizontalScroll(this.view.getHorizontalScroll() - cursorXPos + TimelineView.CHANNEL_WIDTH);
		}
		
		SwingUtilities.invokeLater(this.timelineLayer::repaint);
	}
	
	public void frameRendered(Composition comp, float time) {
		SwingUtilities.invokeLater(this.timelineLayer::repaint);
	}
	
	public void effectLoaded(TrackEffect fx) {
	}
	
	public void effectUnloaded(TrackEffect fx) {
	}
	
	private int framesToPixels(int f) {
		return (int) (f * this.view.getHorizontalScale() + this.view.getHorizontalScroll() + TimelineView.CHANNEL_WIDTH);
	}
	
	private int pixelToFrames(int x) {
		return Math.max((int) ((x - TimelineView.CHANNEL_WIDTH - this.view.getHorizontalScroll()) / this.view.getHorizontalScale()), 0);
	}
	
	private void moveFrameCursorClick(int clickX, int clickY) {
		UrmusicController.setFramePosition(this.pixelToFrames(clickX + (int) (this.view.getHorizontalScale() / 2)));
	}
	
	public void mouseDragged(MouseEvent e) {
		if(this.mouseButton1Pressed) this.moveFrameCursorClick(e.getX(), e.getY());
	}
	
	public void mouseMoved(MouseEvent e) {
	}
	
	public void mouseClicked(MouseEvent e) {
	}
	
	public void mousePressed(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1 && e.getY() <= TimelineView.FRAME_CARET_HEADER_HEIGHT) {
			this.mouseButton1Pressed = true;
			this.moveFrameCursorClick(e.getX(), e.getY());
		}
	}
	
	public void mouseReleased(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1) this.mouseButton1Pressed = false;
	}
	
	public void mouseEntered(MouseEvent e) {
	}
	
	public void mouseExited(MouseEvent e) {
	}
	
	public void mouseWheelMoved(MouseWheelEvent e) {
		if(e.isControlDown()) {
			float f = 1.5f;
			if(e.getPreciseWheelRotation() > 0) {
				f = 1 / f;
			}
			
			/*
			Local zooming:
			
			Zooms on the timeline but also changes the hscroll so
			it zooms "where the cursor is". Solving the equation:
			
			fa(x) = (x - cw - scrollA) / scaleA; < gives the frame index at the mouse x coord before the zoom
			fb(x) = (x - cw - scrollB) / scaleB; < gives the frame index at the mouse x coord after the zoom
			
			fb(x) = fa(x)
			(x - cw - scrollB) / scaleB = fa(x)
			x - cw - scrollB = fa(x) * scaleB
			-cw - scrollB = fa(x) * scaleB - x
			-scrollB = fa(x) * scaleB - x + cw
			scrollB = -fa(x) * scaleB + x - cw
			*/
			
			int mx = MathUtils.clamp(e.getX(), TimelineView.CHANNEL_WIDTH, this.getWidth());
			int ffx = this.pixelToFrames(mx);
			
			this.view.setHorizontalScale(this.view.getHorizontalScale() * f);
			this.view.setHorizontalScroll(-ffx * this.view.getHorizontalScale() + mx - TimelineView.CHANNEL_WIDTH);
		} else {
			float s = (float) (10 * e.getPreciseWheelRotation());
			
			TimelineMainScrollable.this.view.setHorizontalScroll(TimelineMainScrollable.this.view.getHorizontalScroll() + s);
		}
		
		TimelineMainScrollable.this.repaint();
	}
}
