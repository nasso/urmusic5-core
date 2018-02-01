package io.github.nasso.urmusic.view.components.panels.timeline;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

import javax.swing.JScrollPane;

import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.event.CompositionFocusListener;
import io.github.nasso.urmusic.model.project.Composition;
import io.github.nasso.urmusic.view.components.UrmViewPane;

public class TimelineView extends UrmViewPane implements CompositionFocusListener, KeyEventDispatcher {
	private static final long serialVersionUID = -5890250765481685754L;
	
	private TimelineMainScrollable body;
	
	private float horizontalScale = 1;
	private float horizontalScroll = 0.0f;
	
	public TimelineView() {
		this.setLayout(new BorderLayout());
		
		this.buildUI();
		
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
		UrmusicModel.addCompositionFocusListener(this);
		UrmusicModel.addFrameCursorListener(this.body);
	}
	
	private void buildUI() {
		this.body = new TimelineMainScrollable(this);
		
		JScrollPane scrollPane = new JScrollPane(this.body, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(null);
		this.add(scrollPane, BorderLayout.CENTER);
		this.setBackground(Color.LIGHT_GRAY);
	}
	
	public void dispose() {
		KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
		UrmusicModel.removeCompositionFocusListener(this);
		UrmusicModel.removeFrameCursorListener(this.body);
		if(UrmusicModel.getFocusedComposition() != null) UrmusicModel.getFocusedComposition().getTimeline().removeTracklistListener(this.body);
	}
	
	public void focusedCompositionChanged(Composition oldFocus, Composition newFocus) {
		if(oldFocus != null) oldFocus.getTimeline().removeTracklistListener(this.body);
		if(newFocus != null) newFocus.getTimeline().addTracklistListener(this.body);
	}
	
	public float getHorizontalScale() {
		return this.horizontalScale;
	}

	public void setHorizontalScale(float horizontalScale) {
		this.horizontalScale = Math.max(Math.min(horizontalScale, 100f), 0.1f);
	}

	public float getHorizontalScroll() {
		return this.horizontalScroll;
	}

	public void setHorizontalScroll(float horizontalScroll) {
		this.horizontalScroll = Math.min(horizontalScroll, 0);
	}

	public boolean dispatchKeyEvent(KeyEvent e) {
		if(e.getID() == KeyEvent.KEY_PRESSED) {
			switch(e.getKeyCode()) {
				case KeyEvent.VK_RIGHT:
					UrmusicModel.setFrameCursor(UrmusicModel.getFrameCursor() + 1);
					break;
				case KeyEvent.VK_LEFT:
					UrmusicModel.setFrameCursor(UrmusicModel.getFrameCursor() - 1);
					break;
			}
		}
		
		return false;
	}
}
