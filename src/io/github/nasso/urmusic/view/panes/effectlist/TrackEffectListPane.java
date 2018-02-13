package io.github.nasso.urmusic.view.panes.effectlist;

import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import io.github.nasso.urmusic.common.event.TrackListener;
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
	}

	private void addEffectPane(TrackEffectInstance e, int pos) {
		this.add(new TrackEffectPane(e), pos);
		
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

	public void dirtyFlagged(Track source) {
	}
}
