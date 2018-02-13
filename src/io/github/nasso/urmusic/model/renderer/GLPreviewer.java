package io.github.nasso.urmusic.model.renderer;

import java.awt.Component;

import io.github.nasso.urmusic.model.renderer.GLPreviewRenderer.ViewMode;

public class GLPreviewer {
	Component panel;
	GLPreviewRenderer renderer;
	
	GLPreviewer(GLRenderer glRenderer) {
		this.renderer = new GLPreviewRenderer(glRenderer);
	}
	
	public Component getPanel() {
		return this.panel;
	}
	
	public void updateCamera(float camX, float camY, float camZoom) {
		this.renderer.updateCamera(camX, camY, camZoom);
	}
	
	public ViewMode getViewMode() {
		return this.renderer.getViewMode();
	}

	public void setViewMode(ViewMode viewMode) {
		this.renderer.setViewMode(viewMode);
	}
}
