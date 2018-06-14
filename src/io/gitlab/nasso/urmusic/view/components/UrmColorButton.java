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
package io.gitlab.nasso.urmusic.view.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import io.gitlab.nasso.urmusic.common.MathUtils;
import io.gitlab.nasso.urmusic.common.RGBA32;
import io.gitlab.nasso.urmusic.view.data.UrmusicStrings;

public class UrmColorButton extends JComponent implements MouseListener {
	private static final Color DARK_COLOR = new Color(0x444444);
	private static final Color LIGHT_COLOR = new Color(0xe4e4e4);
	
	private Color color = Color.RED;
	
	private JLabel label;
	
	private Consumer<UrmColorButton> onChange;

	public UrmColorButton() {
		this(null);
	}
	
	public UrmColorButton(Consumer<UrmColorButton> onChange) {
		this.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
		
		this.setLayout(new BorderLayout());
		this.add(this.label = new JLabel(), BorderLayout.CENTER);
		this.label.setHorizontalAlignment(SwingConstants.CENTER);
		this.updateLabel();
		
		this.addMouseListener(this);
		this.onChange = onChange;
	}
	
	private void updateLabel() {
		this.label.setText(RGBA32.toHexString(this.color.getRGB()));
		
		float grayscale = (this.color.getRed() + this.color.getGreen() + this.color.getBlue()) / 765.0f;
		
		if(this.color.getAlpha() < 0.5f || grayscale > 0.5f) this.label.setForeground(DARK_COLOR);
		else this.label.setForeground(LIGHT_COLOR);
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
		
		super.paintComponent(g);
	}
	
	public Color getColor() {
		return this.color;
	}
	
	public void setColor(RGBA32 rgba) {
		if(
			this.color.getRed() == rgba.getRed() &&
			this.color.getGreen() == rgba.getGreen() &&
			this.color.getBlue() == rgba.getBlue() &&
			this.color.getAlpha() == rgba.getAlpha()
		) return;
		
		this.color = new Color(rgba.getRed(), rgba.getGreen(), rgba.getBlue(), rgba.getAlpha());
		this.updateLabel();
		this.repaint();
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
		if(
			e.getButton() == MouseEvent.BUTTON1 &&
			MathUtils.boxContains(e.getX(), e.getY(), 0, 0, this.getWidth(), this.getHeight())) {
			Color c = JColorChooser.showDialog(this, UrmusicStrings.getString("dialog.colorPicker.title"), this.color);
			
			if(c != null) {
				this.color = c;
				this.updateLabel();
				this.repaint();
				
				if(this.onChange != null) this.onChange.accept(this);
			}
		}
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}
}
