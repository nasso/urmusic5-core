package io.github.nasso.urmusic.controller;

import io.github.nasso.urmusic.model.UrmusicModel;

public class UrmusicController {
	private UrmusicController() { }
	
	public static final void init() {
		
	}
	
	public static final void requestExit() {
		UrmusicModel.exit();
	}
}
