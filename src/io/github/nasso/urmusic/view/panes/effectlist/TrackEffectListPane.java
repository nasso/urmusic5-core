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
package io.github.nasso.urmusic.view.panes.effectlist;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import io.github.nasso.urmusic.common.MathUtils;
import io.github.nasso.urmusic.common.event.TrackListener;
import io.github.nasso.urmusic.controller.UrmusicController;
import io.github.nasso.urmusic.model.project.Track;
import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.github.nasso.urmusic.view.layout.VListLayout;

public class TrackEffectListPane extends JPanel implements TrackListener {
	private static int global_counter = Integer.MIN_VALUE;

	private Track track;
	
	public TrackEffectListPane(Track t) {
		this.setName("listpane_" + Integer.toHexString(global_counter++));
		
		this.setLayout(new VListLayout(0));
		
		this.setTrack(t);
		
		this.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				if(MathUtils.boxContains(e.getX(), e.getY(), 0, 0, e.getComponent().getWidth(), e.getComponent().getHeight())) {
					UrmusicController.focusTrackEffectInstance(null);
					UrmusicController.toggleFocusEffectParameter(null, false);
				}
			}
		});
	}

	private void addEffectPane(TrackEffectInstance e, int pos) {
		this.add(new TrackEffectPane(this.track, e), pos);
		
		this.revalidate();
		this.repaint();
	}
	
	private void removeEffectPane(int pos) {
		TrackEffectPane fxPane = (TrackEffectPane) this.getComponent(pos);
		fxPane.dispose();
		this.remove(fxPane);
		
		this.revalidate();
		this.repaint();
	}
	
	public void dispose() {
		if(this.track != null) {
			this.track.removeTrackListener(this);
			
			for(Component c : this.getComponents()) {
				if(c instanceof TrackEffectPane)
					((TrackEffectPane) c).dispose();
			}
		}
	}
	
	public Track getTrack() {
		return this.track;
	}

	public void setTrack(Track t) {
		if(this.track != null) {
			this.track.removeTrackListener(this);
			this.removeAll();
		}
		
		this.track = t;
		
		if(this.track != null) {
			for(int i = 0; i < this.track.getEffectCount(); i++) {
				this.addEffectPane(this.track.getEffect(i), i);
			}
			
			this.track.addTrackListener(this);
		}
	}

	public void nameChanged(Track source, String newName) {
	}

	public void enabledStateChanged(Track source, boolean isEnabledNow) {
	}

	public void rangesChanged(Track source) {
	}

	public void effectAdded(Track source, TrackEffectInstance e, int pos) {
		SwingUtilities.invokeLater(() -> {
			this.addEffectPane(e, pos);
		});
	}

	public void effectRemoved(Track source, TrackEffectInstance e, int pos) {
		SwingUtilities.invokeLater(() -> {
			this.removeEffectPane(pos);
		});
	}

	public void effectMoved(Track source, TrackEffectInstance e, int oldPos, int newPos) {
		SwingUtilities.invokeLater(() -> {
			Component fx = this.getComponent(oldPos);
			this.remove(fx);
			this.add(fx, newPos);
			
			this.revalidate();
			this.repaint();
		});
	}
}
