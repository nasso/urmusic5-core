package io.github.nasso.urmusic.view.panes.preview;
	
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;

import io.github.nasso.urmusic.common.MathUtils;
import io.github.nasso.urmusic.common.event.EffectParamListener;
import io.github.nasso.urmusic.common.event.FocusListener;
import io.github.nasso.urmusic.common.event.RendererListener;
import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.project.Composition;
import io.github.nasso.urmusic.model.project.TrackEffect;
import io.github.nasso.urmusic.model.project.param.EffectParam;
import io.github.nasso.urmusic.model.project.param.KeyFrame;
import io.github.nasso.urmusic.model.renderer.GLPreviewRenderer.ViewMode;
import io.github.nasso.urmusic.model.renderer.GLPreviewer;
import io.github.nasso.urmusic.view.components.UrmMenu;
import io.github.nasso.urmusic.view.components.UrmViewPane;
import io.github.nasso.urmusic.view.data.UrmusicStrings;

@SuppressWarnings({"rawtypes", "unchecked"})
public class PreviewView extends UrmViewPane implements
													RendererListener,
													FocusListener<EffectParam<?>>,
													EffectParamListener,
													MouseListener,
													MouseMotionListener,
													MouseWheelListener {
	public static final String VIEW_NAME = "preview";

	private PreviewParameterControlsPane controlPane;
	private GLPreviewer previewer;
	private Component glPane;
	
	public PreviewView() {
		this.addMenu(new UrmMenu(UrmusicStrings.getString("view." + VIEW_NAME + ".menu.view"),
			new JMenuItem(new AbstractAction(UrmusicStrings.getString("view." + VIEW_NAME + ".menu.view.fit")) {
				public void actionPerformed(ActionEvent e) {
					PreviewView.this.setViewMode(ViewMode.FIT);
				}
			}),
			new JMenuItem(new AbstractAction(UrmusicStrings.getString("view." + VIEW_NAME + ".menu.view.fitMax")) {
				public void actionPerformed(ActionEvent e) {
					PreviewView.this.setViewMode(ViewMode.FIT_MAX);
				}
			}),
			new JMenuItem(new AbstractAction(UrmusicStrings.getString("view." + VIEW_NAME + ".menu.view.original")) {
				public void actionPerformed(ActionEvent e) {
					PreviewView.this.setViewMode(ViewMode.ORIGINAL);
				}
			}),
			new JSeparator(),
			new JMenuItem(new AbstractAction(UrmusicStrings.getString("view." + VIEW_NAME + ".menu.view.center")) {
				public void actionPerformed(ActionEvent e) {
					PreviewView.this.centerView();
				}
			})
		));
		
		this.setOpaque(true);
		this.setBackground(Color.WHITE);
		
		OverlayLayout ol = new OverlayLayout(this);
		this.setLayout(ol);
		
		this.previewer = UrmusicModel.getRenderer().createGLJPanelPreview();
		this.glPane = this.previewer.getPanel();
		
		this.add(this.controlPane = new PreviewParameterControlsPane(this), Integer.valueOf(1));
		this.add(this.glPane, Integer.valueOf(2));
		
		this.addMouseListener(this);
		this.addMouseWheelListener(this);
		this.addMouseMotionListener(this);
		
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
		
		SwingUtilities.invokeLater(() -> {
			this.controlPane.update();
			this.repaint();
		});
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

	public int xPosToUI(float x) {
		float sw = this.getWidth();
		float sh = this.getHeight();
		float rw = UrmusicModel.getFocusedComposition().getWidth();
		float rh = UrmusicModel.getFocusedComposition().getHeight();
		
		float s = Math.min(sw / rw, sh / rh);
		float w = rw * s;
		float bx = (sw - w) * 0.5f;
		float uix = bx + (x / rw + 0.5f) * w;
		
		uix -= sw * 0.5f;
		uix *= this.camZoom;
		uix += sw * 0.5f;
		uix -= this.camX;
		
		return (int) uix;
	}
	
	public int yPosToUI(float y) {
		float sw = this.getWidth();
		float sh = this.getHeight();
		float rw = UrmusicModel.getFocusedComposition().getWidth();
		float rh = UrmusicModel.getFocusedComposition().getHeight();
		
		float s = Math.min(sw / rw, sh / rh);
		float h = rh * s;
		float by = (sh - h) * 0.5f;
		float uiy = by + (-y / rh + 0.5f) * h;

		uiy -= sh * 0.5f;
		uiy *= this.camZoom;
		uiy += sh * 0.5f;
		uiy -= this.camY;
		
		return (int) uiy;
	}
	
	public float xUIToPos(int x) {
		float sw = this.getWidth();
		float sh = this.getHeight();
		float rw = UrmusicModel.getFocusedComposition().getWidth();
		float rh = UrmusicModel.getFocusedComposition().getHeight();
		
		float s = Math.min(sw / rw, sh / rh);
		float w = rw * s;
		float bx = (sw - w) / 2f;
		float uix = x + this.camX;
		
		uix -= sw * 0.5f;
		uix /= this.camZoom;
		uix += sw * 0.5f;
		
		return ((uix - bx) / w - 0.5f) * rw;
	}
	
	public float yUIToPos(int y) {
		float sw = this.getWidth();
		float sh = this.getHeight();
		float rw = UrmusicModel.getFocusedComposition().getWidth();
		float rh = UrmusicModel.getFocusedComposition().getHeight();
		
		float s = Math.min(sw / rw, sh / rh);
		float h = rh * s;
		float by = (sh - h) / 2f;
		float uiy = y + this.camY;
		
		uiy -= sh * 0.5f;
		uiy /= this.camZoom;
		uiy += sh * 0.5f;
		
		return -((uiy - by) / h - 0.5f) * rh;
	}
	
	// -- Camera controls
	private float camX = 0.0f, camY = 0.0f, camZoom = 1.0f;
	
	private boolean dragButton = false;
	private Point pressPoint;
	
	private void updateCamera() {
		this.camZoom = MathUtils.clamp(this.camZoom, 0.5f, 10.0f);

		this.previewer.setViewMode(ViewMode.CUSTOM);
		this.previewer.updateCamera(this.camX, this.camY, this.camZoom);
		
		this.controlPane.revalidate();
		this.repaint();
	}

	private void setViewMode(ViewMode mode) {
		this.camZoom = 1.0f;
		this.camX = this.camY = 0.0f;
		
		this.previewer.setViewMode(mode);
		this.previewer.updateCamera(0.0f, 0.0f, 1.0f);
		
		this.controlPane.revalidate();
		this.repaint();
	}
	
	private void centerView() {
		this.camX = this.camY = 0.0f;
		
		this.updateCamera();
	}
	
	public void mouseDragged(MouseEvent e) {
		if(this.dragButton) {
			this.camX -= e.getX() - this.pressPoint.x;
			this.camY -= e.getY() - this.pressPoint.y;
			
			this.pressPoint = e.getPoint();
			
			this.updateCamera();
		}
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON2) {
			this.pressPoint = e.getPoint();
			this.dragButton = true;
		}
	}

	public void mouseReleased(MouseEvent e) {
		this.dragButton &= e.getButton() != MouseEvent.BUTTON2;
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		this.camZoom -= e.getPreciseWheelRotation() * 0.5f;
		this.updateCamera();
	}
}
