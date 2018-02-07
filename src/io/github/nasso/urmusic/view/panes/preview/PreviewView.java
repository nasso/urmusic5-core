package io.github.nasso.urmusic.view.panes.preview;
	
import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;

import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.event.EffectParamListener;
import io.github.nasso.urmusic.model.event.FocusListener;
import io.github.nasso.urmusic.model.event.RendererListener;
import io.github.nasso.urmusic.model.project.Composition;
import io.github.nasso.urmusic.model.project.TrackEffect;
import io.github.nasso.urmusic.model.project.control.EffectParam;
import io.github.nasso.urmusic.model.project.control.KeyFrame;
import io.github.nasso.urmusic.view.components.UrmViewPane;

@SuppressWarnings({"rawtypes", "unchecked"})
public class PreviewView extends UrmViewPane implements RendererListener, FocusListener<EffectParam<?>>, EffectParamListener {
	private static final long serialVersionUID = -761158235222787214L;
	public static final String VIEW_NAME = "preview";

	private PreviewParameterControlsPane controlPane;
	private JComponent glPane;
	
	public PreviewView() {
		this.setOpaque(true);
		this.setBackground(Color.WHITE);
		
		OverlayLayout ol = new OverlayLayout(this);
		this.setLayout(ol);
		
		this.glPane = UrmusicModel.getRenderer().createGLJPanelPreview();
		
		this.add(this.controlPane = new PreviewParameterControlsPane(), Integer.valueOf(1));
		this.add(this.glPane, Integer.valueOf(2));
		
		UrmusicModel.getRenderer().addRendererListener(this);
		UrmusicModel.addEffectParameterFocusListener(this);
		
		if(UrmusicModel.getFocusedEffectParameter() != null)
			UrmusicModel.getFocusedEffectParameter().addEffectParamListener(this);
	}

	public void dispose() {
		UrmusicModel.getRenderer().removeRendererListener(this);
		UrmusicModel.removeEffectParameterFocusListener(this);
		
		this.controlPane.dispose();
	}

	public void frameRendered(Composition comp, int frame) {
		if(frame != UrmusicModel.getFrameCursor()) return;
		
		SwingUtilities.invokeLater(this.controlPane::update);
		SwingUtilities.invokeLater(this::repaint);
	}

	public void effectLoaded(TrackEffect fx) {
	}

	public void effectUnloaded(TrackEffect fx) {
	}

	public void focusChanged(EffectParam<?> oldFocus, EffectParam<?> newFocus) {
		SwingUtilities.invokeLater(() -> {
			if(oldFocus != null) {
				oldFocus.removeEffectParamListener(this);
				this.controlPane.removeParameter(oldFocus);
			}
			
			if(newFocus != null) {
				newFocus.addEffectParamListener(this);
				this.controlPane.addParameter(newFocus);
			}
		});
	}

	public void valueChanged(EffectParam source, Object newVal) {
		SwingUtilities.invokeLater(this.controlPane::update);
	}

	public void keyFrameAdded(EffectParam source, KeyFrame kf) {
		SwingUtilities.invokeLater(this.controlPane::update);
	}

	public void keyFrameRemoved(EffectParam source, KeyFrame kf) {
		SwingUtilities.invokeLater(this.controlPane::update);
	}
}
