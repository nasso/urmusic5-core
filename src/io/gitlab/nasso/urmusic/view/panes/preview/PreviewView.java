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
package io.gitlab.nasso.urmusic.view.panes.preview;
	
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

import io.gitlab.nasso.urmusic.common.MathUtils;
import io.gitlab.nasso.urmusic.common.event.EffectParamListener;
import io.gitlab.nasso.urmusic.common.event.MultiFocusListener;
import io.gitlab.nasso.urmusic.common.event.VideoRendererListener;
import io.gitlab.nasso.urmusic.controller.UrmusicController;
import io.gitlab.nasso.urmusic.model.UrmusicModel;
import io.gitlab.nasso.urmusic.model.project.Composition;
import io.gitlab.nasso.urmusic.model.project.VideoEffect;
import io.gitlab.nasso.urmusic.model.project.param.EffectParam;
import io.gitlab.nasso.urmusic.model.project.param.KeyFrame;
import io.gitlab.nasso.urmusic.model.renderer.video.GLPreviewer;
import io.gitlab.nasso.urmusic.model.renderer.video.GLPreviewer.ViewMode;
import io.gitlab.nasso.urmusic.view.components.UrmMenu;
import io.gitlab.nasso.urmusic.view.components.UrmViewPane;
import io.gitlab.nasso.urmusic.view.data.UrmusicStrings;

@SuppressWarnings({"rawtypes", "unchecked"})
public class PreviewView extends UrmViewPane implements
													VideoRendererListener,
													MultiFocusListener<EffectParam<?>>,
													EffectParamListener,
													MouseListener,
													MouseMotionListener,
													MouseWheelListener {
	public static final String VIEW_NAME = "preview";

	private PreviewParameterControlsPane controlPane;
	private GLPreviewer previewer;
	private Component glPane;
	
	public PreviewView() {
		this.addMenu(new UrmMenu(UrmusicStrings.getString("view." + PreviewView.VIEW_NAME + ".menu.view"),
			new JMenuItem(new AbstractAction(UrmusicStrings.getString("view." + PreviewView.VIEW_NAME + ".menu.view.fit")) {
				public void actionPerformed(ActionEvent e) {
					PreviewView.this.setViewMode(ViewMode.FIT);
				}
			}),
			new JMenuItem(new AbstractAction(UrmusicStrings.getString("view." + PreviewView.VIEW_NAME + ".menu.view.fitMax")) {
				public void actionPerformed(ActionEvent e) {
					PreviewView.this.setViewMode(ViewMode.FIT_MAX);
				}
			}),
			new JMenuItem(new AbstractAction(UrmusicStrings.getString("view." + PreviewView.VIEW_NAME + ".menu.view.original")) {
				public void actionPerformed(ActionEvent e) {
					PreviewView.this.setViewMode(ViewMode.ORIGINAL);
				}
			}),
			new JSeparator(),
			new JMenuItem(new AbstractAction(UrmusicStrings.getString("view." + PreviewView.VIEW_NAME + ".menu.view.center")) {
				public void actionPerformed(ActionEvent e) {
					PreviewView.this.centerView();
				}
			})
		));
		
		this.setOpaque(true);
		this.setBackground(Color.WHITE);
		
		OverlayLayout ol = new OverlayLayout(this);
		this.setLayout(ol);
		
		this.previewer = UrmusicModel.getVideoRenderer().createGLJPanelPreview();
		this.glPane = this.previewer.getPanel();
		
		this.add(this.controlPane = new PreviewParameterControlsPane(this), Integer.valueOf(1));
		this.add(this.glPane, Integer.valueOf(2));
		
		this.addMouseListener(this);
		this.addMouseWheelListener(this);
		this.addMouseMotionListener(this);
		
		UrmusicModel.getVideoRenderer().addVideoRendererListener(this);
		UrmusicController.addEffectParameterFocusListener(this);
		
		for(EffectParam<?> param : UrmusicController.getFocusedEffectParameters()) {
			param.addEffectParamListener(this);
		}
	}
	
	public void dispose() {
		UrmusicModel.getVideoRenderer().removeRendererListener(this);
		UrmusicController.removeEffectParameterFocusListener(this);
		
		this.previewer.dispose();
		this.controlPane.dispose();
	}

	public void frameRendered(Composition comp, float time) {
		if(time != UrmusicController.getTimePosition()) return;
		
		SwingUtilities.invokeLater(() -> {
			this.controlPane.update();
			this.repaint();
		});
	}

	public void effectLoaded(VideoEffect fx) {
	}

	public void effectUnloaded(VideoEffect fx) {
	}

	public void focused(EffectParam<?> o) {
		SwingUtilities.invokeLater(() -> {
			o.addEffectParamListener(this);
			this.controlPane.addParameter(o);
		});
	}

	public void unfocused(EffectParam<?> o) {
		SwingUtilities.invokeLater(() -> {
			o.removeEffectParamListener(this);
			this.controlPane.removeParameter(o);
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
		return this.previewer.xPosToUI(x, this.getWidth(), this.getHeight());
	}
	
	public int yPosToUI(float y) {
		return this.previewer.yPosToUI(y, this.getWidth(), this.getHeight());
	}
	
	public float xUIToPos(int x) {
		return this.previewer.xUIToPos(x, this.getWidth(), this.getHeight());
	}
	
	public float yUIToPos(int y) {
		return this.previewer.yUIToPos(y, this.getWidth(), this.getHeight());
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
		this.camZoom -= e.getPreciseWheelRotation() * 0.25f;
		
		this.updateCamera();
	}
}
