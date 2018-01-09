package io.github.nasso.urmusic.model;

import io.github.nasso.urmusic.view.UrmusicView;

public class UrmusicModel {
	private UrmusicModel() { }
	
	public static final void init() {
		
	}
	
	public static final void exit() {
		UrmusicView.saveViewState();
		System.exit(0);
	}
}
