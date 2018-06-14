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

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import io.gitlab.nasso.urmusic.common.MathUtils;
import io.gitlab.nasso.urmusic.common.event.CompositionListener;
import io.gitlab.nasso.urmusic.common.event.FocusListener;
import io.gitlab.nasso.urmusic.common.event.FrameCursorListener;
import io.gitlab.nasso.urmusic.common.event.TimelineListener;
import io.gitlab.nasso.urmusic.controller.UrmusicController;
import io.gitlab.nasso.urmusic.model.project.Composition;
import io.gitlab.nasso.urmusic.model.project.Timeline;
import io.gitlab.nasso.urmusic.model.project.Track;
import io.gitlab.nasso.urmusic.view.data.UrmusicStrings;

public class TimelineStatusBar extends JPanel implements FocusListener<Composition>, CompositionListener, FrameCursorListener, TimelineListener {
	private Composition currentComp;
	
	private JLabel positionLabel;
	private JLabel compName;
	private JLabel compClearColor;
	private JLabel compSize;
	private JLabel fpsLabel;
	
	public TimelineStatusBar(TimelineView timelineView) {
		this.setBackground(Color.WHITE);
		this.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY),
				BorderFactory.createEmptyBorder(4, 4, 4, 4)
		));
		
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.add(Box.createHorizontalStrut(16));
		this.add(this.positionLabel = new JLabel());
		this.add(Box.createHorizontalGlue());
		this.add(this.compName = new JLabel());
		this.add(Box.createHorizontalStrut(32));
		this.add(this.compClearColor = new JLabel());
		this.add(Box.createHorizontalStrut(32));
		this.add(this.compSize = new JLabel());
		this.add(Box.createHorizontalStrut(32));
		this.add(this.fpsLabel = new JLabel());
		this.add(Box.createHorizontalStrut(16));
		
		Font fnt = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
		this.positionLabel.setFont(fnt);
		this.compName.setFont(fnt);
		this.compClearColor.setFont(fnt);
		this.compSize.setFont(fnt);
		this.fpsLabel.setFont(fnt);
		
		this.positionLabel.setForeground(Color.DARK_GRAY);
		this.compName.setForeground(Color.DARK_GRAY);
		this.compClearColor.setForeground(Color.DARK_GRAY);
		this.compSize.setForeground(Color.DARK_GRAY);
		this.fpsLabel.setForeground(Color.DARK_GRAY);
		
		Composition comp = UrmusicController.getFocusedComposition();
		if(comp != null)
			this.focusChanged(null, comp);
		
		UrmusicController.addCompositionFocusListener(this);
		UrmusicController.addFrameCursorListener(this);
	}
	
	public void dispose() {
		UrmusicController.removeCompositionFocusListener(this);
		
		if(this.currentComp != null)
			this.currentComp.removeListener(this);
	}
	
	private void updateCompNameLabel() {
		this.compName.setText(this.currentComp == null ? UrmusicStrings.getString("global.na") : this.currentComp.getName());
	}
	
	private void updateClearColorLabel() {
		this.compClearColor.setText(this.currentComp == null ? UrmusicStrings.getString("global.na") : this.currentComp.getClearColor().toString());
	}
	
	private void updateSizeLabel() {
		this.compSize.setText(this.currentComp == null ? UrmusicStrings.getString("global.na") : (this.currentComp.getWidth() + "x" + this.currentComp.getHeight()));
	}
	
	public void nameChanged(Composition comp) {
		this.updateCompNameLabel();
	}

	public void clearColorChanged(Composition comp) {
		this.updateClearColorLabel();
	}

	public void resize(Composition comp) {
		this.updateSizeLabel();
	}

	public void dispose(Composition comp) {
	}

	public void focusChanged(Composition oldFocus, Composition newFocus) {
		if(this.currentComp != null) {
			this.currentComp.removeListener(this);
			this.currentComp.getTimeline().removeTimelineListener(this);
		}
		
		if(oldFocus != null && oldFocus != this.currentComp) {
			oldFocus.removeListener(this);
			oldFocus.getTimeline().removeTimelineListener(this);
		}

		this.currentComp = newFocus;
		
		if(newFocus != null) {
			newFocus.addListener(this);
			newFocus.getTimeline().addTimelineListener(this);
		}
		
		this.updateCompNameLabel();
		this.updateClearColorLabel();
		this.updateSizeLabel();
		this.updatePositionLabels();
		this.updateFPSLabel();
	}

	private void updatePositionLabels() {
		int frame = UrmusicController.getFrameCursor();
		float time = UrmusicController.getTimePosition();
		
		int frameCount = this.currentComp == null ? -1 : this.currentComp.getTimeline().getTotalFrameCount();
		float duration = this.currentComp == null ? -1 : this.currentComp.getTimeline().getDuration();
		
		if(this.currentComp == null) {
			this.positionLabel.setText(MathUtils.prettyTime(time, true) + " (" + frame + ")");
		} else {
			this.positionLabel.setText(
				MathUtils.prettyTime(time, true) + " (" + frame + ")  /  " + MathUtils.prettyTime(duration, true) + " (" + frameCount + ")"
			);
		}
	}
	
	private void updateFPSLabel() {
		this.fpsLabel.setText((this.currentComp == null ? UrmusicStrings.getString("global.na") : this.currentComp.getTimeline().getFramerate()) + " FPS");
	}
	
	public void frameChanged(int oldPosition, int newPosition) {
		this.updatePositionLabels();
	}

	public void durationChanged(Timeline src) {
		this.updatePositionLabels();
	}

	public void framerateChanged(Timeline src) {
		this.updatePositionLabels();
		this.updateFPSLabel();
	}

	public void trackAdded(Timeline src, int index, Track track) { }
	public void trackRemoved(Timeline src, int index, Track track) { }
	public void trackMoved(Timeline src, int oldIndex, int newIndex, Track track) { }
}
