package io.github.nasso.urmusic.model;

import io.github.nasso.urmusic.model.renderer.Renderer;
import io.github.nasso.urmusic.model.timeline.Timeline;
import io.github.nasso.urmusic.view.UrmusicView;

public class UrmusicModel {
	private UrmusicModel() { }

	private static Timeline timeline;
	private static Renderer renderer;
	
	public static void init() {
		timeline = new Timeline();
		
		// TODO: User prefs
		renderer = new Renderer(1280, 720, 500, Renderer.Backend.GL3);
		renderer.renderFrame(0);
	}
	
	public static void exit() {
		UrmusicView.saveViewState();
		
		renderer.dispose();
		
		System.exit(0);
	}
	
	public static Renderer getRenderer() {
		return renderer;
	}
	
	public static Timeline getTimeline() {
		return timeline;
	}
}
