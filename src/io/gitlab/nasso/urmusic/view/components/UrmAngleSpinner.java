package io.gitlab.nasso.urmusic.view.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.function.Consumer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class UrmAngleSpinner extends JComponent {
	private class Spinner extends JComponent {
		public Spinner() {
			this.setPreferredSize(new Dimension(24, 24));
		}
		
		protected void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g.create();
			
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			int w = this.getWidth();
			int h = this.getHeight();
			int wh = w / 2;
			int hh = h / 2;
			double angleRad = Math.toRadians(UrmAngleSpinner.this.angle - 90);
			
			g2d.setColor(Color.GRAY);
			g2d.drawOval(0, 0, w - 1, h - 1);
			g2d.setColor(Color.DARK_GRAY);
			g2d.drawLine(wh, hh, wh + (int) (Math.cos(angleRad) * wh), hh + (int) (Math.sin(angleRad) * hh));
			
			g2d.dispose();
		}
	}
	
	private UrmEditableIntegerField turnsField;
	private UrmEditableNumberField angleField;
	
	private float angle = 0.0f;
	private float step = 0.0f;
	
	public UrmAngleSpinner(float angle, float step, boolean blockKeyEvents, Consumer<UrmAngleSpinner> onValueChange) {
		this.angle = angle;
		this.step = step;

		final Color lblColor = new Color(0, 0, 0, 128);
		JLabel lbl;
		
		JPanel fieldsPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		fieldsPane.setOpaque(false);
		
		fieldsPane.add(this.turnsField = new UrmEditableIntegerField(blockKeyEvents, (f) -> {
			this.setAngleValue(this.getNormalizedAngle() + f.getValue().intValue() * 360);
			
			onValueChange.accept(this);
		}));
		
		lbl = new JLabel("x + ");
		lbl.setForeground(lblColor);
		fieldsPane.add(lbl);
		
		fieldsPane.add(this.angleField = new UrmEditableNumberField(blockKeyEvents, (f) -> {
			this.setAngleValue(f.getValue().floatValue() + this.getNumberOfTurns() * 360);
			onValueChange.accept(this);
		}));
		this.angleField.setStep(step);
		
		lbl = new JLabel("°");
		lbl.setForeground(lblColor);
		fieldsPane.add(lbl);
		
		JPanel fieldsPaneContainer = new JPanel();
		fieldsPaneContainer.setOpaque(false);
		fieldsPaneContainer.setLayout(new BoxLayout(fieldsPaneContainer, BoxLayout.Y_AXIS));
		
		fieldsPaneContainer.add(Box.createVerticalGlue());
		fieldsPaneContainer.add(fieldsPane);
		
		this.setLayout(new BorderLayout(4, 4));
		this.add(new Spinner(), BorderLayout.EAST);
		this.add(fieldsPaneContainer, BorderLayout.CENTER);
	}
	
	/**
	 * Value is in degrees
	 * @return
	 */
	public float getAngleValue() {
		return this.angle;
	}
	
	public void setAngleValue(float degrees) {
		this.angle = degrees;
		this.turnsField.setValue(this.getNumberOfTurns());
		this.angleField.setValue(this.getNormalizedAngle());
		this.repaint();
	}
	
	public float getNormalizedAngle() {
		return this.angle - this.getNumberOfTurns() * 360;
	}
	
	public int getNumberOfTurns() {
		return (int) Math.floor(this.angle / 360);
	}
	
	public float getStep() {
		return this.step;
	}
}
