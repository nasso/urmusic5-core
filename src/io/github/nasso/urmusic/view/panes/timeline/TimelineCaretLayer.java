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
package io.github.nasso.urmusic.view.panes.timeline;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.plaf.LayerUI;

import io.github.nasso.urmusic.controller.UrmusicController;
import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.project.Composition;
import io.github.nasso.urmusic.model.project.param.EffectParam;
import io.github.nasso.urmusic.model.project.param.KeyFrame;
import io.github.nasso.urmusic.model.renderer.video.CachedFrame;

public class TimelineCaretLayer extends LayerUI<JPanel> {
	private static final Color KEY_FRAME_COLOR = new Color(0x0099FF);

	private TimelineView view;
	
	public TimelineCaretLayer(TimelineView view) {
		this.view = view;
	}
	
	public void paint(Graphics g, JComponent c) {
		super.paint(g, c);

		Composition comp = UrmusicController.getFocusedComposition();

		int hscroll = Math.round(this.view.getHorizontalScroll());
		int hscalei = Math.round(this.view.getHorizontalScale());
		
		int frameIndex = UrmusicController.getFrameCursor();
		int frameXOffset = (int) (frameIndex * this.view.getHorizontalScale() + this.view.getHorizontalScroll());
		
		Graphics2D g2d = (Graphics2D) g.create();
		
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		float secondScreenSize = comp.getTimeline().getFramerate() * this.view.getHorizontalScale();
		int visibleSeconds = (int) (c.getWidth() / secondScreenSize / 2);
		
		g2d.setColor(Color.WHITE);
		for(int i = 0; i <= visibleSeconds + 1; i++) {
			float x = (i - (int) (hscroll / (secondScreenSize * 2))) * secondScreenSize * 2 + hscroll;
			
			g2d.fillRect(
				(int) x,
				0,
				(int) secondScreenSize,
				TimelineView.FRAME_CARET_HEADER_HEIGHT
			);
		}
		
		g2d.setColor(Color.GREEN);
		CachedFrame[] renderedFrames = UrmusicModel.getVideoRenderer().getCachedFrames();
		for(int i = 0; i < renderedFrames.length; i++) {
			CachedFrame f = renderedFrames[i];
			
			int x = Math.round(f.frame_pos * this.view.getHorizontalScale() + hscroll);
			int w = Math.round((f.frame_pos + 1) * this.view.getHorizontalScale() + hscroll) - x;
			
			if(x < -hscalei || x > c.getWidth()) continue;
			
			if(f.dirty) continue;
			
			g2d.fillRect(x, 0, w, TimelineView.FRAME_CARET_HEADER_HEIGHT);
		}
		
		for(EffectParam<?> param : UrmusicController.getFocusedEffectParameters()) {
			int keyframesCount = param.getKeyFrameCount();
			
			g2d.setColor(TimelineCaretLayer.KEY_FRAME_COLOR);
			for(int i = 0; i < keyframesCount; i++) {
				KeyFrame<?> kf = param.getKeyFrame(i);

				int x = Math.round((kf.getPosition() * comp.getTimeline().getFramerate()) * this.view.getHorizontalScale() + hscroll);
				if(x < -hscalei || x > c.getWidth()) continue;
				
				g2d.fillRect(x, 0, hscalei + 1, TimelineView.FRAME_CARET_HEADER_HEIGHT);
			}
		}
		
		if(this.view.getHorizontalScale() >= 6) {
			int visibleFrames = (int) (c.getWidth() / this.view.getHorizontalScale()) + 1;
			
			g2d.setColor(Color.GRAY);
			for(int i = 0; i <= visibleFrames; i++) {
				float x = (i - Math.round(hscroll / this.view.getHorizontalScale())) * this.view.getHorizontalScale() + hscroll;
				
				g2d.drawLine(
						(int) x, 0,
						(int) x, TimelineView.FRAME_CARET_HEADER_HEIGHT
				);
			}
		}
		
		g2d.setColor(Color.MAGENTA);
		g2d.fillRect(
			frameXOffset - 3,
			0,
			7,
			TimelineView.FRAME_CARET_HEADER_HEIGHT
		);
		g2d.drawLine(
			frameXOffset,
			0,
			frameXOffset,
			c.getHeight()
		);
		
		g2d.setColor(Color.WHITE);
		g2d.drawLine(
			frameXOffset,
			2,
			frameXOffset,
			TimelineView.FRAME_CARET_HEADER_HEIGHT - 3
		);
		
		g2d.dispose();
	}
}
