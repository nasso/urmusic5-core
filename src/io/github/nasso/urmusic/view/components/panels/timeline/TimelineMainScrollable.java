package io.github.nasso.urmusic.view.components.panels.timeline;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import io.github.nasso.urmusic.controller.UrmusicController;
import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.event.FrameCursorListener;
import io.github.nasso.urmusic.model.event.RendererListener;
import io.github.nasso.urmusic.model.event.TracklistListener;
import io.github.nasso.urmusic.model.project.Composition;
import io.github.nasso.urmusic.model.project.Track;
import io.github.nasso.urmusic.view.layout.VListLayout;

public class TimelineMainScrollable extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener, TracklistListener, FrameCursorListener, RendererListener  {
	private static final long serialVersionUID = 1008513031790674759L;
	
	public static final int CHANNEL_HEIGHT = 30;
	public static final int CHANNEL_WIDTH = 150;
	
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
		
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addMouseWheelListener(this);
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
		TimelineTrackRangesBar ranges = (TimelineTrackRangesBar) this.timelinePane.getComponent(index);
		ranges.setTrack(null);
		
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

	public void frameRendered(Composition comp, int frame) {
		this.timelineLayer.repaint();
	}

	private int getFrameFromXPos(int x) {
		return (int) ((x - CHANNEL_WIDTH - this.view.getHorizontalScroll()) / this.view.getHorizontalScale());
	}
	
	private void moveFrameCursorClick(int clickX, int clickY) {
		if(clickX >= CHANNEL_WIDTH) {
			UrmusicController.setFramePosition(this.getFrameFromXPos(clickX));
		}
	}
	
	public void mouseDragged(MouseEvent e) {
		if(this.mouseButton1Pressed)
			this.moveFrameCursorClick(e.getX(), e.getY());
	}

	public void mouseMoved(MouseEvent e) {
	}
	
	public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1 && e.getY() <= 10) {
			this.mouseButton1Pressed = true;
			this.moveFrameCursorClick(e.getX(), e.getY());
		}
	}

	public void mouseReleased(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1)
			this.mouseButton1Pressed = false;
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}
	
	public void mouseWheelMoved(MouseWheelEvent e) {
		if(e.isControlDown()) {
			float f = 1.5f;
			if(e.getWheelRotation() > 0) {
				f = 1 / f;
			}
			
			float hscroll = this.view.getHorizontalScroll();
			float hscale = this.view.getHorizontalScale();
			
			/*
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
			
			hscale *= f;
			hscroll = -this.getFrameFromXPos(e.getX()) * hscale + e.getX() - CHANNEL_WIDTH;
			
			this.view.setHorizontalScroll(hscroll);
			this.view.setHorizontalScale(hscale);
		} else {
			float s = e.getWheelRotation() > 0 ? 10 : -10;
			
			TimelineMainScrollable.this.view.setHorizontalScroll(TimelineMainScrollable.this.view.getHorizontalScroll() + s);
		}
		
		TimelineMainScrollable.this.repaint();
	}

}
