package io.github.nasso.urmusic.controller;

import io.github.nasso.urmusic.model.UrmusicModel;

public class UrmusicController {
	private UrmusicController() { }
	
	public static void init() {
		
	}
	
	public static void requestExit() {
		UrmusicModel.exit();
	}
}
