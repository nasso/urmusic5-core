package io.github.nasso.urmusic.view.components.panels.timeline;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.plaf.LayerUI;

import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.project.Composition;
import io.github.nasso.urmusic.model.renderer.CachedFrame;

public class TimelineCaretLayer extends LayerUI<JPanel> {
	private static final long serialVersionUID = 8643950564372365882L;

	private TimelineView view;
	
	public TimelineCaretLayer(TimelineView view) {
		this.view = view;
	}
	
	public void paint(Graphics g, JComponent c) {
		super.paint(g, c);

		Composition comp = UrmusicModel.getFocusedComposition();

		int hscroll = (int) this.view.getHorizontalScroll();
		int hscalei = (int) this.view.getHorizontalScale();
		
		int frameIndex = UrmusicModel.getFrameCursor();
		int frameXOffset = (int) (frameIndex * this.view.getHorizontalScale() + this.view.getHorizontalScroll());
		
		Graphics2D g2d = (Graphics2D) g.create();
		
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		float secondScreenSize = comp.getFramerate() * this.view.getHorizontalScale();
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
		CachedFrame[] renderedFrames = UrmusicModel.getRenderer().getCachedFrames();
		for(int i = 0; i < renderedFrames.length; i++) {
			CachedFrame f = renderedFrames[i];
			
			int x = (int) (f.frame_id * this.view.getHorizontalScale() + hscroll);
			
			if(x < -hscalei || x > c.getWidth()) continue;
			
			if(f.dirty) continue;
			
			g2d.fillRect(x, 0, hscalei + 1, TimelineView.FRAME_CARET_HEADER_HEIGHT);
		}
		
		if(this.view.getHorizontalScale() >= 6) {
			int visibleFrames = (int) (c.getWidth() / this.view.getHorizontalScale()) + 1;
			
			g2d.setColor(Color.GRAY);
			for(int i = 0; i <= visibleFrames; i++) {
				float x = (i - (int) (hscroll / this.view.getHorizontalScale())) * this.view.getHorizontalScale() + hscroll;
				
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
