package io.github.nasso.urmusic.view.data;

public class UrmusicViewState {
	private UrmusicSplittedPaneState[] paneStates;
	
	public UrmusicViewState() {
		this(null);
	}
	
	public UrmusicViewState(UrmusicSplittedPaneState[] paneStates) {
		this.setPaneStates(paneStates);
	}
	
	public UrmusicSplittedPaneState[] getPaneStates() {
		return this.paneStates;
	}

	public void setPaneStates(UrmusicSplittedPaneState[] paneStates) {
		this.paneStates = paneStates;
	}
}
