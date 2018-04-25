package io.github.nasso.urmusic.view.panes.effectlist;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import io.github.nasso.urmusic.common.event.FocusListener;
import io.github.nasso.urmusic.common.event.TimelineListener;
import io.github.nasso.urmusic.controller.UrmusicController;
import io.github.nasso.urmusic.model.project.Timeline;
import io.github.nasso.urmusic.model.project.Track;
import io.github.nasso.urmusic.view.components.UrmMenu;
import io.github.nasso.urmusic.view.components.UrmViewPane;
import io.github.nasso.urmusic.view.data.UrmusicStrings;

public class EffectListView extends UrmViewPane implements TimelineListener, FocusListener<Track> {
	public static final String VIEW_NAME = "effectList";

	private Map<Track, TrackEffectListPane> listPanes = new HashMap<>();
	
	private JPanel effectListContainer = new JPanel();
	private CardLayout effectListCards = new CardLayout();
	
	private EffectPickerDialog addEffectDialog = new EffectPickerDialog(UrmusicStrings.getString("view.effectList.dialog.add.title"), (e) -> {
		UrmusicController.addEffects(e);
	});
	
	public EffectListView() {
		// -- menu -- 
		// Add
		this.addMenu(new UrmMenu(UrmusicStrings.getString("view." + EffectListView.VIEW_NAME + ".menu.add"),
			new JMenuItem(new AbstractAction(UrmusicStrings.getString("view." + EffectListView.VIEW_NAME + ".menu.add.effect")) {
				public void actionPerformed(ActionEvent e) {
					EffectListView.this.showAddEffectDialog();
				}
			})
		));
		
		// -- view --
		this.setLayout(new BorderLayout());
		
		this.buildUI();
		
		Track t = UrmusicController.getFocusedTrack();
		if(t != null)
			this.effectListCards.show(this.effectListContainer, this.getPaneFor(t).getName());
		
		UrmusicController.addTrackFocusListener(this);
	}
	
	private void showAddEffectDialog() {
		this.addEffectDialog.showDialog();
	}
	
	private TrackEffectListPane getPaneFor(Track track) {
		if(this.listPanes.containsKey(track)) {
			return this.listPanes.get(track);
		}
		
		return this.addTrack(track);
	}
	
	private TrackEffectListPane addTrack(Track track) {
		TrackEffectListPane fxpane = new TrackEffectListPane(track);
		
		this.effectListContainer.add(fxpane, fxpane.getName());
		this.effectListContainer.revalidate();
		
		this.listPanes.put(track, fxpane);
		
		return fxpane;
	}
	
	private void removeTrack(Track track) {
		TrackEffectListPane fxpane = this.listPanes.remove(track);
		
		this.effectListContainer.remove(fxpane);
		
		fxpane.dispose();
	}
	
	public void dispose() {
		UrmusicController.removeTrackFocusListener(this);
		
		for(Track t : this.listPanes.keySet()) {
			this.listPanes.get(t).dispose();
		}
	}
	
	private void buildUI() {
		this.effectListContainer.setLayout(this.effectListCards);
		
		this.add(new JScrollPane(this.effectListContainer, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
	}

	public void trackAdded(Timeline src, int index, Track track) {
		SwingUtilities.invokeLater(() -> this.addTrack(track));
	}

	public void trackRemoved(Timeline src, int index, Track track) {
		SwingUtilities.invokeLater(() -> this.removeTrack(track));
	}
	
	public void focusChanged(Track oldFocus, Track newFocus) {
		SwingUtilities.invokeLater(() -> {
			this.effectListCards.show(this.effectListContainer, this.getPaneFor(newFocus).getName());
		});
	}
	

	public void durationChanged(Timeline src) {
	}
	

	public void framerateChanged(Timeline src) {
	}

	public void audioSampleRateChanged(Timeline src) {
	}
}
