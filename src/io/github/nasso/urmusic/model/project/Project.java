package io.github.nasso.urmusic.model.project;

public class Project {
	private ProjectFileSystem files = new ProjectFileSystem();
	private Composition mainComp = new Composition();
	
	public Project() {
		
	}
	
	public Composition getMainComposition() {
		return this.mainComp;
	}
	
	public void setMainComposition(Composition mainComp) {
		this.mainComp = mainComp;
	}

	public ProjectFileSystem getFileSystem() {
		return this.files;
	}
}
