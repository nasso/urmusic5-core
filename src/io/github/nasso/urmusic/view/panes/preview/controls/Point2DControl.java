package io.github.nasso.urmusic.view.panes.preview.controls;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import org.joml.Vector2f;
import org.joml.Vector2fc;

import io.github.nasso.urmusic.common.MathUtils;
import io.github.nasso.urmusic.controller.UrmusicController;
import io.github.nasso.urmusic.model.project.param.Point2DParam;
import io.github.nasso.urmusic.view.panes.preview.PreviewView;

public class Point2DControl extends PreviewParamControl<Point2DParam> implements MouseListener, MouseMotionListener {
	private static final Stroke POINT_BORDER_STROKE = new BasicStroke(3);
	private static final Stroke POINT_INNER_STROKE = new BasicStroke(1);
	private static final Color POINT_BORDER_COLOR = new Color(0x990000);
	private static final Color POINT_INNER_COLOR = new Color(0xFF0000);

	private Vector2f _vec2 = new Vector2f();
	
	private boolean hover = false;
	private boolean pressed = false;
	
	public Point2DControl(PreviewView view, Point2DParam param) {
		super(view, param);
		
		this.setPreferredSize(new Dimension(17, 17));
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}

	public void updateComponentLayout() {
		Vector2fc p = UrmusicController.getParamValueNow(this.getParam());
		
		int x = this.xPosToUI(p.x());
		int y = this.yPosToUI(p.y());
		
		Dimension size = this.getPreferredSize();
		this.setLocation(x - size.width / 2, y - size.height / 2);
		this.setSize(size);
	}
	
	public void dispose() {
		
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		int w = this.getWidth() - 1;
		int h = this.getHeight() - 1;
		
		g2d.setColor(Point2DControl.POINT_BORDER_COLOR);
		g2d.setStroke(Point2DControl.POINT_BORDER_STROKE);
		if(!this.hover) g2d.drawOval(3, 3, w - 6, h - 6);
		else g2d.drawOval(1, 1, w - 2, h - 2);
		g2d.drawLine(0, h / 2, w, h / 2);
		g2d.drawLine(w / 2, 0, w / 2, h);
		
		g2d.setColor(Point2DControl.POINT_INNER_COLOR);
		g2d.setStroke(Point2DControl.POINT_INNER_STROKE);
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
			float x = this.xUIToPos(e.getX() + this.getX());
			float y = this.yUIToPos(e.getY() + this.getY());

			UrmusicController.setParamValueNow(this.getParam(), this._vec2.set(x, y));
		}
	}

	public void mouseMoved(MouseEvent e) {
	}
}
