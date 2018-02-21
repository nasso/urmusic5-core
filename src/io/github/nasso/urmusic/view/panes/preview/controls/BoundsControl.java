package io.github.nasso.urmusic.view.panes.preview.controls;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.function.Consumer;

import javax.swing.JComponent;

import org.joml.Vector2f;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import io.github.nasso.urmusic.common.MathUtils;
import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.project.param.BoundsParam;
import io.github.nasso.urmusic.view.panes.preview.PreviewView;

public class BoundsControl extends PreviewParamControl<BoundsParam> {
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
	
	public BoundsControl(PreviewView view, BoundsParam param) {
		super(view, param);
		
		this.setOpaque(false);
		
		this.setLayout(null);
		this.add(this.topLeft = new DraggablePoint(true, p -> {
			int frame = UrmusicModel.getFrameCursor();
			
			this._vec4.set(this.getParam().getValue(frame));
			
			p.value.x = Math.min(p.value.x, this._vec4.x + this._vec4.z);
			p.value.y = Math.max(p.value.y, this._vec4.y);
			
			this._vec4.z = this._vec4.x + this._vec4.z - p.value.x;
			this._vec4.w = p.value.y - this._vec4.y;
			
			this._vec4.x = p.value.x;
			
			this.getParam().setValue(this._vec4, frame);
		}));
		
		this.add(this.topRight = new DraggablePoint(true, p -> {
			int frame = UrmusicModel.getFrameCursor();
			
			this._vec4.set(this.getParam().getValue(frame));

			this._vec4.z = p.value.x - this._vec4.x;
			this._vec4.w = p.value.y - this._vec4.y;
			
			this.getParam().setValue(this._vec4, frame);
		}));
		
		this.add(this.botLeft = new DraggablePoint(true, p -> {
			int frame = UrmusicModel.getFrameCursor();
			
			this._vec4.set(this.getParam().getValue(frame));
			
			p.value.x = Math.min(p.value.x, this._vec4.x + this._vec4.z);
			p.value.y = Math.min(p.value.y, this._vec4.y + this._vec4.w);
			
			this._vec4.z = this._vec4.x + this._vec4.z - p.value.x;
			this._vec4.w = this._vec4.y + this._vec4.w - p.value.y;
			
			this._vec4.x = p.value.x;
			this._vec4.y = p.value.y;
			
			this.getParam().setValue(this._vec4, frame);
		}));
		
		this.add(this.botRight = new DraggablePoint(true, p -> {
			int frame = UrmusicModel.getFrameCursor();
			
			this._vec4.set(this.getParam().getValue(frame));

			p.value.x = Math.max(p.value.x, this._vec4.x);
			p.value.y = Math.min(p.value.y, this._vec4.y + this._vec4.w);

			this._vec4.z = p.value.x - this._vec4.x;
			this._vec4.w = this._vec4.y + this._vec4.w - p.value.y;

			this._vec4.z = p.value.x - this._vec4.x;
			this._vec4.y = p.value.y;
			
			this.getParam().setValue(this._vec4, frame);
		}));
	}
	
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		int x = BORDER_SIZE;
		int y = BORDER_SIZE;
		int w = this.getWidth() - BORDER_SIZE * 2;
		int h = this.getHeight() - BORDER_SIZE * 2;
		
		g2d.setColor(LINE_BORDER_COLOR);
		g2d.setStroke(LINE_BORDER_STROKE);
		g2d.drawRect(x - 1, y - 1, w + 2, h + 2);
		
		g2d.setColor(LINE_INNER_COLOR);
		g2d.setStroke(LINE_INNER_STROKE);
		g2d.drawRect(x, y, w, h);
		
		g2d.dispose();
	}
	
	public void updateComponentLayout(int frame) {
		Vector4fc p = this.getParam().getValue(frame);
		
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
		ix -= BORDER_SIZE;
		iy -= BORDER_SIZE;
		iw += BORDER_SIZE * 2;
		ih += BORDER_SIZE * 2;
		
		this.setBounds(ix, iy, iw, ih);
		
		// Points
		this.topLeft.setSize(BORDER_SIZE * 2, BORDER_SIZE * 2);
		this.topLeft.setLocation(0, 0);
		
		this.topRight.setSize(BORDER_SIZE * 2, BORDER_SIZE * 2);
		this.topRight.setLocation(iw - BORDER_SIZE * 2, 0);
		
		this.botLeft.setSize(BORDER_SIZE * 2, BORDER_SIZE * 2);
		this.botLeft.setLocation(0, ih - BORDER_SIZE * 2);
		
		this.botRight.setSize(BORDER_SIZE * 2, BORDER_SIZE * 2);
		this.botRight.setLocation(iw - BORDER_SIZE * 2, ih - BORDER_SIZE * 2);
	}

	public void dispose() {
	}
	
	private class DraggablePoint extends JComponent implements MouseListener, MouseMotionListener {
		private Vector2f value = new Vector2f();
		
		private final boolean filled;
		private boolean hover = false;
		private boolean pressed = false;
		
		private Consumer<DraggablePoint> onChange;
		
		public DraggablePoint(boolean filled, Consumer<DraggablePoint> onChange) {
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
				g2d.setColor(POINT_BORDER_COLOR);
				if(!this.hover) g2d.fillRect(3, 3, w - 6, h - 6);
				else g2d.fillRect(1, 1, w - 2, h - 2);
				
				g2d.setColor(POINT_INNER_COLOR);
				if(!this.hover) g2d.fillRect(4, 4, w - 8, h - 8);
				else g2d.fillRect(2, 2, w - 4, h - 4);
			} else {
				g2d.setColor(POINT_BORDER_COLOR);
				g2d.setStroke(POINT_BORDER_STROKE);
				if(!this.hover) g2d.drawRect(3, 3, w - 6, h - 6);
				else g2d.drawRect(1, 1, w - 2, h - 2);
				
				g2d.setColor(POINT_INNER_COLOR);
				g2d.setStroke(POINT_INNER_STROKE);
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
				this.onChange.accept(this);
			}
		}

		public void mouseMoved(MouseEvent e) {
		}
	}
}
