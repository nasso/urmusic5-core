package io.github.nasso.urmusic.view.components.panels.effectlist.controls;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JColorChooser;
import javax.swing.JComponent;

import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.project.control.RGBA32Param;
import io.github.nasso.urmusic.utils.MathUtils;
import io.github.nasso.urmusic.utils.MutableRGBA32;
import io.github.nasso.urmusic.utils.RGBA32;

public class RGBA32ParamUI extends EffectParamUI<RGBA32Param> {
	private static final long serialVersionUID = 8290439490941369516L;
	
	private static class ColorButton extends JComponent implements MouseListener {
		private static final long serialVersionUID = -1557230386392972285L;
		
		private Color color = Color.RED;
		
		private Runnable onClick;
		
		public ColorButton(Runnable onClick) {
			this.setPreferredSize(new Dimension(30, 0));
			
			this.addMouseListener(this);
			this.onClick = onClick;
		}
		
		protected void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g.create();
			
			g2d.setColor(Color.WHITE);
			g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
			
			g2d.setColor(Color.LIGHT_GRAY);
			for(int x = 0; x < this.getWidth(); x += 5) {
				for(int y = x % 10; y < this.getHeight(); y += 10) {
					g2d.fillRect(x, y, 5, 5);
				}
			}
			
			g2d.setColor(this.color);
			g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
			
			g2d.setColor(Color.BLACK);
			g2d.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
			
			g2d.dispose();
		}
		
		public void setColor(RGBA32 rgba) {
			if(
				this.color.getRed() == rgba.getRed() &&
				this.color.getGreen() == rgba.getGreen() &&
				this.color.getBlue() == rgba.getBlue() &&
				this.color.getAlpha() == rgba.getAlpha()
			) return;
			
			this.color = new Color(rgba.getRed(), rgba.getGreen(), rgba.getBlue(), rgba.getAlpha());
			this.repaint();
		}

		public void mouseClicked(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
			if(
				e.getButton() == MouseEvent.BUTTON1 &&
				MathUtils.boxContains(e.getX(), e.getY(), 0, 0, this.getWidth(), this.getHeight()))
				this.onClick.run();
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}
	}
	
	private MutableRGBA32 _rgba32 = new MutableRGBA32();
	
	private ColorButton colorButton;
	
	public RGBA32ParamUI(RGBA32Param param) {
		super(param);
		
		this.colorButton = new ColorButton(() -> {
			Color c = JColorChooser.showDialog(this, "Pick a color", this.colorButton.color);
			
			if(c != null) {
				this._rgba32.setRGBA(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
				this.getParam().setValue(this._rgba32, UrmusicModel.getFrameCursor());
				this.updateControl(UrmusicModel.getFrameCursor());
			}
		});

		this.setLayout(new BorderLayout());
		this.add(this.colorButton, BorderLayout.EAST);
	}

	public void updateControl(int frame) {
		this.colorButton.setColor(this.getParam().getValue(frame));
	}
}

