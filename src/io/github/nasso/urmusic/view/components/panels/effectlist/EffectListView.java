package io.github.nasso.urmusic.view.components.panels.effectlist;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.event.FocusListener;
import io.github.nasso.urmusic.model.event.TracklistListener;
import io.github.nasso.urmusic.model.project.Track;
import io.github.nasso.urmusic.view.components.UrmViewPane;

public class EffectListView extends UrmViewPane implements TracklistListener, FocusListener<Track> {
	private static final long serialVersionUID = -896247777042870529L;

	private Map<Track, TrackEffectListPane> listPanes = new HashMap<>();
	
	private JPanel effectListContainer = new JPanel();
	private CardLayout effectListCards = new CardLayout();
	
	public EffectListView() {
		this.setLayout(new BorderLayout());
		
		this.buildUI();
		
		List<Track> tracks = UrmusicModel.getFocusedComposition().getTimeline().getTracks();
		for(int i = 0; i < tracks.size(); i++) {
			this.addTrack(tracks.get(i));
		}
		
		Track t = UrmusicModel.getFocusedTrack();
		if(t != null)
			this.effectListCards.show(this.effectListContainer, this.listPanes.get(t).getName());
		
		UrmusicModel.addTrackFocusListener(this);
	}
	
	private void addTrack(Track track) {
		TrackEffectListPane fxpane = new TrackEffectListPane(track);
		
		this.effectListContainer.add(fxpane, fxpane.getName());
		
		this.listPanes.put(track, fxpane);
	}
	
	private void removeTrack(Track track) {
		TrackEffectListPane fxpane = this.listPanes.remove(track);
		
		this.effectListContainer.remove(fxpane);
		
		fxpane.dispose();
	}
	
	public void dispose() {
		UrmusicModel.removeTrackFocusListener(this);
		
		for(Track t : this.listPanes.keySet()) {
			this.listPanes.get(t).dispose();
		}
	}
	
	private void buildUI() {
		this.effectListContainer.setLayout(this.effectListCards);
		
		this.add(new JScrollPane(this.effectListContainer, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
	}

	public void trackAdded(int index, Track track) {
		SwingUtilities.invokeLater(() -> this.addTrack(track));
	}

	public void trackRemoved(int index, Track track) {
		SwingUtilities.invokeLater(() -> this.removeTrack(track));
	}

	public void focusChanged(Track oldFocus, Track newFocus) {
		SwingUtilities.invokeLater(() -> {
			this.effectListCards.show(this.effectListContainer, this.listPanes.get(newFocus).getName());
		});
	}
}
