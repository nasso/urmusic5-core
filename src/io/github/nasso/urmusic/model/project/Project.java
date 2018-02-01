package io.github.nasso.urmusic.model.project;

public class Project {
	private Composition mainComp = new Composition();
	
	public Project() {
	}

	public Composition getMainComposition() {
		return this.mainComp;
	}

	public void setMainComposition(Composition mainComp) {
		this.mainComp = mainComp;
	}
}
