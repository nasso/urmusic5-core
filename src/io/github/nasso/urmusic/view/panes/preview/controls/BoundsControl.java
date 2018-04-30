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
package io.github.nasso.urmusic.view.panes.preview.controls;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.function.BiConsumer;

import javax.swing.JComponent;

import org.joml.Vector2f;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import io.github.nasso.urmusic.common.BoolValue;
import io.github.nasso.urmusic.common.MathUtils;
import io.github.nasso.urmusic.controller.UrmusicController;
import io.github.nasso.urmusic.model.project.param.BoundsParam;
import io.github.nasso.urmusic.view.panes.preview.PreviewView;

public class BoundsControl extends PreviewParamControl<BoundsParam> implements MouseListener, MouseMotionListener {
	private static final int BORDER_SIZE = 10;
	
	private static final Stroke LINE_BORDER_STROKE = new BasicStroke(3);
	private static final Stroke LINE_INNER_STROKE = new BasicStroke(2);
	private static final Color LINE_BORDER_COLOR = new Color(0x990000);
	private static final Color LINE_INNER_COLOR = new Color(0xFF0000);
	
	private static final Stroke POINT_BORDER_STROKE = new BasicStroke(3);
	private static final Stroke POINT_INNER_STROKE = new BasicStroke(1);
	private static final Color POINT_BORDER_COLOR = new Color(0x990000);
	private static final Color POINT_INNER_COLOR = new Color(0xFF0000);
	
	private Vector4f _vec4 = new Vector4f();
	private DraggablePoint topLeft, topRight, botLeft, botRight;
	
	private float lastValidRatio = 1;
	
	public BoundsControl(PreviewView view, BoundsParam param) {
		super(view, param);
		
		this.setOpaque(false);
		
		this.setLayout(null);
		this.add(this.topLeft = new DraggablePoint(true, (p, shift) -> {
			this._vec4.set(UrmusicController.getParamValueNow(this.getParam()));
			
			p.value.x = Math.min(p.value.x, this._vec4.x + this._vec4.z);
			p.value.y = Math.max(p.value.y, this._vec4.y);
			
			float width = this._vec4.x + this._vec4.z - p.value.x;
			float height = p.value.y - this._vec4.y;
			
			if(shift == BoolValue.FALSE == this.getParam().isKeepRatio())
				width = height * this.lastValidRatio;
			
			this._vec4.x = this._vec4.x + this._vec4.z - width;
			this._vec4.z = width;
			this._vec4.w = height;
			
			UrmusicController.setParamValueNow(this.getParam(), this._vec4);
		}));
		
		this.add(this.topRight = new DraggablePoint(true, (p, shift) -> {
			this._vec4.set(UrmusicController.getParamValueNow(this.getParam()));
			
			float width = p.value.x - this._vec4.x;
			float height = p.value.y - this._vec4.y;
			
			if(shift == BoolValue.FALSE == this.getParam().isKeepRatio())
				width = height * this.lastValidRatio;
			
			this._vec4.z = width;
			this._vec4.w = height;
			
			UrmusicController.setParamValueNow(this.getParam(), this._vec4);
		}));
		
		this.add(this.botLeft = new DraggablePoint(true, (p, shift) -> {
			this._vec4.set(UrmusicController.getParamValueNow(this.getParam()));
			
			p.value.x = Math.min(p.value.x, this._vec4.x + this._vec4.z);
			p.value.y = Math.min(p.value.y, this._vec4.y + this._vec4.w);
			
			float width = this._vec4.x + this._vec4.z - p.value.x;
			float height = this._vec4.y + this._vec4.w - p.value.y;
			
			if(shift == BoolValue.FALSE == this.getParam().isKeepRatio())
				width = height * this.lastValidRatio;
			
			this._vec4.x = this._vec4.x + this._vec4.z - width;
			this._vec4.y = p.value.y;
			this._vec4.z = width;
			this._vec4.w = height;
			
			UrmusicController.setParamValueNow(this.getParam(), this._vec4);
		}));
		
		this.add(this.botRight = new DraggablePoint(true, (p, shift) -> {
			this._vec4.set(UrmusicController.getParamValueNow(this.getParam()));
			
			p.value.x = Math.max(p.value.x, this._vec4.x);
			p.value.y = Math.min(p.value.y, this._vec4.y + this._vec4.w);
			
			float width = p.value.x - this._vec4.x;
			float height = this._vec4.y + this._vec4.w - p.value.y;
			
			if(shift == BoolValue.FALSE == this.getParam().isKeepRatio())
				width = height * this.lastValidRatio;

			this._vec4.y = p.value.y;
			this._vec4.z = width;
			this._vec4.w = height;
			
			UrmusicController.setParamValueNow(this.getParam(), this._vec4);
		}));
		
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}
	
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		int x = BoundsControl.BORDER_SIZE;
		int y = BoundsControl.BORDER_SIZE;
		int w = this.getWidth() - BoundsControl.BORDER_SIZE * 2;
		int h = this.getHeight() - BoundsControl.BORDER_SIZE * 2;
		
		g2d.setColor(BoundsControl.LINE_BORDER_COLOR);
		g2d.setStroke(BoundsControl.LINE_BORDER_STROKE);
		g2d.drawRect(x - 1, y - 1, w + 2, h + 2);
		
		g2d.setColor(BoundsControl.LINE_INNER_COLOR);
		g2d.setStroke(BoundsControl.LINE_INNER_STROKE);
		g2d.drawRect(x, y, w, h);
		
		g2d.dispose();
	}
	
	public void updateComponentLayout() {
		Vector4fc p = UrmusicController.getParamValueNow(this.getParam());
		
		if(this._vec4.z != 0 && this._vec4.w != 0) this.lastValidRatio = this._vec4.z / this._vec4.w;
		
		// Calc rect coords
		float x = p.x();
		float y = p.y();
		float x2 = p.x() + p.z();
		float y2 = p.y() + p.w();
		
		if(x > x2) {
			float temp = x;
			x = x2;
			x2 = temp;
		}
		
		if(y < y2) {
			float temp = y;
			y = y2;
			y2 = temp;
		}
		
		// Calc component coords
		int ix = this.xPosToUI(x);
		int iy = this.yPosToUI(y);
		int iw = this.xPosToUI(x2) - ix;
		int ih = this.yPosToUI(y2) - iy;
		
		// Border
		ix -= BoundsControl.BORDER_SIZE;
		iy -= BoundsControl.BORDER_SIZE;
		iw += BoundsControl.BORDER_SIZE * 2;
		ih += BoundsControl.BORDER_SIZE * 2;
		
		this.setBounds(ix, iy, iw, ih);
		
		// Points
		this.topLeft.setSize(BoundsControl.BORDER_SIZE * 2, BoundsControl.BORDER_SIZE * 2);
		this.topLeft.setLocation(0, 0);
		
		this.topRight.setSize(BoundsControl.BORDER_SIZE * 2, BoundsControl.BORDER_SIZE * 2);
		this.topRight.setLocation(iw - BoundsControl.BORDER_SIZE * 2, 0);
		
		this.botLeft.setSize(BoundsControl.BORDER_SIZE * 2, BoundsControl.BORDER_SIZE * 2);
		this.botLeft.setLocation(0, ih - BoundsControl.BORDER_SIZE * 2);
		
		this.botRight.setSize(BoundsControl.BORDER_SIZE * 2, BoundsControl.BORDER_SIZE * 2);
		this.botRight.setLocation(iw - BoundsControl.BORDER_SIZE * 2, ih - BoundsControl.BORDER_SIZE * 2);
	}

	public void dispose() {
	}
	
	private class DraggablePoint extends JComponent implements MouseListener, MouseMotionListener {
		private Vector2f value = new Vector2f();
		
		private final boolean filled;
		private boolean hover = false;
		private boolean pressed = false;
		
		private BiConsumer<DraggablePoint, BoolValue> onChange;
		
		public DraggablePoint(boolean filled, BiConsumer<DraggablePoint, BoolValue> onChange) {
			this.filled = filled;
			this.onChange = onChange;
			
			this.addMouseListener(this);
			this.addMouseMotionListener(this);
		}
		
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			Graphics2D g2d = (Graphics2D) g.create();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			int w = this.getWidth();
			int h = this.getHeight();
			
			if(this.filled) {
				g2d.setColor(BoundsControl.POINT_BORDER_COLOR);
				if(!this.hover) g2d.fillRect(3, 3, w - 6, h - 6);
				else g2d.fillRect(1, 1, w - 2, h - 2);
				
				g2d.setColor(BoundsControl.POINT_INNER_COLOR);
				if(!this.hover) g2d.fillRect(4, 4, w - 8, h - 8);
				else g2d.fillRect(2, 2, w - 4, h - 4);
			} else {
				g2d.setColor(BoundsControl.POINT_BORDER_COLOR);
				g2d.setStroke(BoundsControl.POINT_BORDER_STROKE);
				if(!this.hover) g2d.drawRect(3, 3, w - 6, h - 6);
				else g2d.drawRect(1, 1, w - 2, h - 2);
				
				g2d.setColor(BoundsControl.POINT_INNER_COLOR);
				g2d.setStroke(BoundsControl.POINT_INNER_STROKE);
				if(!this.hover) g2d.drawRect(3, 3, w - 6, h - 6);
				else g2d.drawRect(1, 1, w - 2, h - 2);
			}
			
			g2d.dispose();
		}

		public void mouseClicked(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
			this.pressed |= (MathUtils.boxContains(e.getX(), e.getY(), 0, 0, this.getWidth(), this.getHeight()) && e.getButton() == MouseEvent.BUTTON1);
		}

		public void mouseReleased(MouseEvent e) {
			this.pressed &= e.getButton() != MouseEvent.BUTTON1;
		}

		public void mouseEntered(MouseEvent e) {
			this.hover = true;
			this.repaint();
		}

		public void mouseExited(MouseEvent e) {
			this.hover = false;
			this.repaint();
		}

		public void mouseDragged(MouseEvent e) {
			if(this.pressed) {
				float x = BoundsControl.this.xUIToPos(e.getX() + this.getX() + BoundsControl.this.getX());
				float y = BoundsControl.this.yUIToPos(e.getY() + this.getY() + BoundsControl.this.getY());
				
				this.value.set(x, y);
				this.onChange.accept(this, e.isShiftDown() ? BoolValue.TRUE : BoolValue.FALSE);
			}
		}

		public void mouseMoved(MouseEvent e) {
		}
	}
	
	// Global drag
	private Point pressPoint = new Point();
	private boolean pressed = false;

	public void mouseDragged(MouseEvent e) {
		if(this.pressed) {
			int relx = e.getXOnScreen() - this.pressPoint.x;
			int rely = e.getYOnScreen() - this.pressPoint.y;
			
			this._vec4.set(UrmusicController.getParamValueNow(this.getParam()));
			this._vec4.x += relx;
			this._vec4.y -= rely;
			
			UrmusicController.setParamValueNow(this.getParam(), this._vec4);
			
			this.pressPoint.setLocation(e.getLocationOnScreen());
		}
	}

	public void mouseMoved(MouseEvent e) {
		
	}

	public void mouseClicked(MouseEvent e) {
		
	}

	public void mouseEntered(MouseEvent e) {
		
	}

	public void mouseExited(MouseEvent e) {
		
	}

	public void mousePressed(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1) {
			this.pressed = true;
			this.pressPoint.setLocation(e.getLocationOnScreen());
		}
	}

	public void mouseReleased(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1) {
			this.pressed = false;
		}
	}
}
