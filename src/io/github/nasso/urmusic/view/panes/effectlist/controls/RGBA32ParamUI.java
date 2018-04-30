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
package io.github.nasso.urmusic.view.panes.effectlist.controls;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JColorChooser;
import javax.swing.JComponent;

import io.github.nasso.urmusic.common.MathUtils;
import io.github.nasso.urmusic.common.MutableRGBA32;
import io.github.nasso.urmusic.common.RGBA32;
import io.github.nasso.urmusic.controller.UrmusicController;
import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.github.nasso.urmusic.model.project.param.RGBA32Param;

public class RGBA32ParamUI extends EffectParamUI<RGBA32Param> {
	private static class ColorButton extends JComponent implements MouseListener {
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
	
	public RGBA32ParamUI(TrackEffectInstance fx, RGBA32Param param) {
		super(fx, param);
	}

	public void updateControl() {
		this.colorButton.setColor(UrmusicController.getParamValueNow(this.getParam()));
	}

	public JComponent buildUI() {
		this.colorButton = new ColorButton(() -> {
			Color c = JColorChooser.showDialog(this, "Pick a color", this.colorButton.color);
			
			if(c != null) {
				this._rgba32.setRGBA(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
				UrmusicController.setParamValueNow(this.getParam(), this._rgba32);
			}
		});
		
		return this.colorButton;
	}
}

