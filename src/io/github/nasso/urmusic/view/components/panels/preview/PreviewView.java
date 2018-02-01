package io.github.nasso.urmusic.view.components.panels.preview;
	
import java.awt.BorderLayout;
import java.awt.Component;

import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.event.RendererListener;
import io.github.nasso.urmusic.model.project.Composition;
import io.github.nasso.urmusic.view.components.UrmViewPane;

public class PreviewView extends UrmViewPane implements RendererListener {
	private static final long serialVersionUID = -761158235222787214L;

	private Component glPane;
	
	public PreviewView() {
		this.setLayout(new BorderLayout());
		
		this.glPane = UrmusicModel.getRenderer().createGLJPanelPreview();
		
		this.add(this.glPane, BorderLayout.CENTER);
		
		UrmusicModel.getRenderer().addRendererListener(this);
	}

	public void dispose() {
		UrmusicModel.getRenderer().removeRendererListener(this);
	}

	public void frameRendered(Composition comp, int frame) {
		this.glPane.repaint();
	}
}
