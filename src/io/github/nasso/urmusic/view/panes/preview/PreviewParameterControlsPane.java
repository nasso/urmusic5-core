package io.github.nasso.urmusic.view.panes.preview;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import org.joml.Vector2f;
import org.joml.Vector2fc;

import io.github.nasso.urmusic.common.MathUtils;
import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.project.control.EffectParam;
import io.github.nasso.urmusic.model.project.control.Point2DParam;

public class PreviewParameterControlsPane extends JComponent {
	private static final long serialVersionUID = -6814038302072686626L;
	
	private static final Stroke POINT_BORDER_STROKE = new BasicStroke(3);
	private static final Stroke POINT_INNER_STROKE = new BasicStroke(1);
	private static final Color POINT_BORDER_COLOR = new Color(0x990000);
	private static final Color POINT_INNER_COLOR = new Color(0xFF0000);
	
	private class ControlsLayout implements LayoutManager {
		public void addLayoutComponent(String name, Component comp) {
		}

		public void removeLayoutComponent(Component comp) {
		}

		public Dimension preferredLayoutSize(Container parent) {
			return new Dimension(0, 0);
		}

		public Dimension minimumLayoutSize(Container parent) {
			return new Dimension(0, 0);
		}

		public void layoutContainer(Container parent) {
			Component[] comps = parent.getComponents();
			
			int frame = UrmusicModel.getFrameCursor();
			
			for(int i = 0; i < comps.length; i++) {
				Component c = comps[i];
				
				if(c instanceof Point2DControl) {
					Vector2fc p = ((Point2DControl) c).param.getValue(frame);
					
					int x = PreviewParameterControlsPane.this.view.xPosToUI(p.x());
					int y = PreviewParameterControlsPane.this.view.yPosToUI(p.y());
					
					Dimension size = c.getPreferredSize();
					c.setLocation(x - size.width / 2, y - size.height / 2);
					c.setSize(size);
				}
			}
		}
	}
	
	private class Point2DControl extends JComponent implements MouseListener, MouseMotionListener {
		private static final long serialVersionUID = -7243820735887468939L;
		
		private Vector2f _vec2 = new Vector2f();
		
		private Point2DParam param;
		private boolean hover = false;
		private boolean pressed = false;
		
		public Point2DControl(Point2DParam param) {
			this.param = param;
			
			this.setPreferredSize(new Dimension(17, 17));
			this.addMouseListener(this);
			this.addMouseMotionListener(this);
		}
		
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			Graphics2D g2d = (Graphics2D) g.create();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			int w = this.getWidth() - 1;
			int h = this.getHeight() - 1;
			
			g2d.setColor(POINT_BORDER_COLOR);
			g2d.setStroke(POINT_BORDER_STROKE);
			if(!this.hover) g2d.drawOval(3, 3, w - 6, h - 6);
			else g2d.drawOval(1, 1, w - 2, h - 2);
			g2d.drawLine(0, h / 2, w, h / 2);
			g2d.drawLine(w / 2, 0, w / 2, h);
			
			g2d.setColor(POINT_INNER_COLOR);
			g2d.setStroke(POINT_INNER_STROKE);
			if(!this.hover) g2d.drawOval(3, 3, w - 6, h - 6);
			else g2d.drawOval(1, 1, w - 2, h - 2);
			g2d.drawLine(1, h / 2, w - 1, h / 2);
			g2d.drawLine(w / 2, 1, w / 2, h - 1);
			
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
				float x = PreviewParameterControlsPane.this.view.xUIToPos(e.getX() + this.getX());
				float y = PreviewParameterControlsPane.this.view.yUIToPos(e.getY() + this.getY());
				
				this.param.setValue(this._vec2.set(x, y), UrmusicModel.getFrameCursor());
			}
		}

		public void mouseMoved(MouseEvent e) {
		}
	}
	
	private final PreviewView view;
	private List<Point2DControl> points = new ArrayList<>();
	
	public PreviewParameterControlsPane(PreviewView view) {
		this.view = view;
		
		this.setOpaque(false);
		
		this.setLayout(new ControlsLayout());
	}
	
	public void update() {
		this.revalidate();
		this.repaint();
	}
	
	public void addParameter(EffectParam<?> param) {
		if(param instanceof Point2DParam) {
			Point2DControl ctrl = new Point2DControl((Point2DParam) param);
			
			this.add(ctrl);
			this.points.add(ctrl); 
			
			this.revalidate();
			this.repaint();
		}
	}
	
	public void removeParameter(EffectParam<?> param) {
		if(param instanceof Point2DParam) {
			Point2DControl ctrl = this.getControlFor((Point2DParam) param);
			
			if(ctrl != null) {
				this.remove(ctrl);
				this.points.remove(ctrl);
				
				this.revalidate();
				this.repaint();
			}
		}
	}
	
	private Point2DControl getControlFor(Point2DParam param) {
		for(Point2DControl ctrl : this.points) {
			if(ctrl.param == param) return ctrl;
		}
		
		return null;
	}
	
	public void dispose() {
	}
}
