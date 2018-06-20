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
package io.gitlab.nasso.urmusic.view.panes.effectlist;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import io.gitlab.nasso.urmusic.common.MathUtils;
import io.gitlab.nasso.urmusic.common.easing.EasingFunction;
import io.gitlab.nasso.urmusic.common.event.EffectParamListener;
import io.gitlab.nasso.urmusic.common.event.FrameCursorListener;
import io.gitlab.nasso.urmusic.common.event.KeyFrameListener;
import io.gitlab.nasso.urmusic.common.event.MultiFocusListener;
import io.gitlab.nasso.urmusic.controller.UrmusicController;
import io.gitlab.nasso.urmusic.model.UrmusicModel;
import io.gitlab.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.gitlab.nasso.urmusic.model.project.param.EffectParam;
import io.gitlab.nasso.urmusic.model.project.param.KeyFrame;
import io.gitlab.nasso.urmusic.view.data.UrmusicStrings;
import io.gitlab.nasso.urmusic.view.data.UrmusicUIRes;
import io.gitlab.nasso.urmusic.view.panes.effectlist.controls.EffectParamUI;

@SuppressWarnings({"rawtypes", "unchecked"})
public class EffectParamPane extends JPanel implements FrameCursorListener, EffectParamListener, MouseListener, MultiFocusListener<EffectParam<?>>, KeyFrameListener {
	private static final Color PARAM_LINE_COLOR = new Color(0xffffff);
	private static final Color PARAM_LINE_SELECTED_COLOR = new Color(0xdddddd);

	private EffectParam<?> param;
	private EffectParamUI<?> controlui = null;
	
	private JLabel keyframeIconLabel, controlNameLabel;
	
	public EffectParamPane(TrackEffectInstance fx, EffectParam<?> param, int i) {
		this.param = param;
		
		this.setBackground(UrmusicController.isFocused(param) ? EffectParamPane.PARAM_LINE_SELECTED_COLOR : EffectParamPane.PARAM_LINE_COLOR);
		this.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY),
			BorderFactory.createEmptyBorder(4, 4, 4, 4)
		));
		
		this.controlNameLabel = new JLabel();
		this.controlNameLabel.setText(UrmusicStrings.getString(UrmusicModel.getSourcePackage(fx.getEffectClass()), "effect." + fx.getEffectClass().getEffectClassID() + ".param." + param.getID() + ".name"));
		this.controlNameLabel.setFont(this.controlNameLabel.getFont().deriveFont(Font.PLAIN, 12));
		this.controlNameLabel.setBackground(EffectParamPane.PARAM_LINE_SELECTED_COLOR);
		this.controlNameLabel.setBorder(BorderFactory.createEmptyBorder(1, 3, 1, 3));
		this.controlNameLabel.setOpaque(false);
		
		this.keyframeIconLabel = new JLabel(UrmusicUIRes.KEY_FRAME_ICON);
		this.keyframeIconLabel.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				if(MathUtils.boxContains(e.getX(), e.getY(), 0, 0, e.getComponent().getWidth(), e.getComponent().getHeight()))
					UrmusicController.toggleKeyFrame(EffectParamPane.this.param);
			}
		});
		
		BoxLayout bl = new BoxLayout(this, BoxLayout.X_AXIS);
		this.setLayout(bl);
		
		this.add(this.keyframeIconLabel);
		this.add(Box.createHorizontalStrut(4));
		this.add(this.controlNameLabel);
		this.add(Box.createHorizontalStrut(64));
		this.add(Box.createHorizontalGlue());
		
		this.controlui = EffectParamUI.createParamUI(fx, this.param);
		if(this.controlui != null) this.add(this.controlui);
		
		this.addMouseListener(this);
		this.param.addEffectParamListener(this);
		UrmusicController.addFrameCursorListener(this);
		UrmusicController.addEffectParameterFocusListener(this);
		
		for(int j = 0; j < this.param.getKeyFrameCount(); j++) {
			this.param.getKeyFrame(j).addKeyFrameListener(this);
		}
		
		this.update();
	}
	
	public void focusChanged(EffectParam<?> oldFocus, EffectParam<?> newFocus) {
		if(this.param != newFocus && this.param != oldFocus) return;
		
		SwingUtilities.invokeLater(() -> {
			this.controlNameLabel.setOpaque(this.param == newFocus);
			this.controlNameLabel.repaint();
		});
	}
	
	public void focused(EffectParam<?> o) {
		if(this.param == o)
			SwingUtilities.invokeLater(() -> {
				this.controlNameLabel.setOpaque(true);
				this.controlNameLabel.repaint();
			});
	}

	public void unfocused(EffectParam<?> o) {
		if(this.param == o)
			SwingUtilities.invokeLater(() -> {
				this.controlNameLabel.setOpaque(false);
				this.controlNameLabel.repaint();
			});
	}
	
	private void update() {
		if(this.controlui != null) this.controlui.updateControl();
		
		if(this.param.getKeyFrameAt(UrmusicController.getTimePosition()) != null)
			this.keyframeIconLabel.setIcon(UrmusicUIRes.KEY_FRAME_ICON_RED);
		else if(this.param.getKeyFrameCount() != 0)
			this.keyframeIconLabel.setIcon(UrmusicUIRes.KEY_FRAME_ICON_BLUE);
		else
			this.keyframeIconLabel.setIcon(UrmusicUIRes.KEY_FRAME_ICON);
	}
	
	public void focusParam(boolean multiselect) {
		UrmusicController.focusTrackEffectInstance(null);
		UrmusicController.toggleFocusEffectParameter(this.param, multiselect);
	}
	
	public void dispose() {
		for(int i = 0; i < this.param.getKeyFrameCount(); i++) {
			this.param.getKeyFrame(i).removeKeyFrameListener(this);
		}
		
		this.param.removeEffectParamListener(this);
		UrmusicController.removeFrameCursorListener(this);
		UrmusicController.removeEffectParameterFocusListener(this);
	}
	
	public void frameChanged(int oldPosition, int newPosition) {
		SwingUtilities.invokeLater(() -> this.update());
	}

	public void valueChanged(EffectParam source, Object newVal) {
		SwingUtilities.invokeLater(() -> this.update());
	}

	public void keyFrameAdded(EffectParam source, KeyFrame kf) {
		kf.addKeyFrameListener(this);
		SwingUtilities.invokeLater(() -> this.update());
	}

	public void keyFrameRemoved(EffectParam source, KeyFrame kf) {
		kf.removeKeyFrameListener(this);
		SwingUtilities.invokeLater(() -> this.update());
	}
	
	public void valueChanged(KeyFrame source, Object newValue) {
		SwingUtilities.invokeLater(() -> this.update());
	}

	public void positionChanged(KeyFrame source, float newPos) {
		SwingUtilities.invokeLater(() -> this.update());
	}

	public void interpChanged(KeyFrame source, EasingFunction newInterp) {
		SwingUtilities.invokeLater(() -> this.update());
	}
	
	public void mouseClicked(MouseEvent e) {
	}
	
	public void mousePressed(MouseEvent e) {
		this.focusParam(e.isControlDown());
	}
	
	public void mouseReleased(MouseEvent e) {
	}
	
	public void mouseEntered(MouseEvent e) {
	}
	
	public void mouseExited(MouseEvent e) {
	}
}
