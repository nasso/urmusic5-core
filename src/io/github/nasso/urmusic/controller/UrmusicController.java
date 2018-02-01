package io.github.nasso.urmusic.controller;

import io.github.nasso.urmusic.model.UrmusicModel;

public class UrmusicController {
	private UrmusicController() { }
	
	public static void init() {
		
	}
	
	public static void requestExit() {
		UrmusicModel.exit();
	}
	
	public static void frameAdvance() {
		UrmusicModel.setFrameCursor(UrmusicModel.getFrameCursor() + 1);
	}
	
	public static void frameBack() {
		UrmusicModel.setFrameCursor(UrmusicModel.getFrameCursor() - 1);
	}
	
	public static void playPause() {
		if(UrmusicModel.isPlayingBack()) UrmusicModel.stopPlayback();
		else UrmusicModel.startPlayback();
	}
}
