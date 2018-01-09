package io.github.nasso.urmusic.view.data;

public class UrmusicSplittedPaneState {
	private int viewID = 0;
	private boolean splitted = false;
	private float splitLocation = 0.5f;
	private UrmusicSplittedPaneState stateA = null;
	private UrmusicSplittedPaneState stateB = null;

	public int getViewID() {
		return this.viewID;
	}

	public void setViewID(int viewID) {
		this.viewID = viewID;
	}
	
	public boolean isSplitted() {
		return this.splitted;
	}

	public void setSplitted(boolean splitted) {
		this.splitted = splitted;
	}

	public float getSplitLocation() {
		return splitLocation;
	}

	public void setSplitLocation(float splitLocation) {
		this.splitLocation = splitLocation;
	}

	public UrmusicSplittedPaneState getStateA() {
		return stateA;
	}

	public void setStateA(UrmusicSplittedPaneState stateA) {
		this.stateA = stateA;
	}

	public UrmusicSplittedPaneState getStateB() {
		return stateB;
	}

	public void setStateB(UrmusicSplittedPaneState stateB) {
		this.stateB = stateB;
	}
}
