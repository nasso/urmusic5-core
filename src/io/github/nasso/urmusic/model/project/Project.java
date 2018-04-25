package io.github.nasso.urmusic.model.project;

import java.nio.file.Path;

public class Project {
	private Path projectFilePath = null;
	private Composition mainComp = null;
	
	public Project() {
		this(new Composition());
	}
	
	public Project(Composition mainComp) {
		this.setMainComposition(mainComp);
	}
	
	public Composition getMainComposition() {
		return this.mainComp;
	}
	
	public void setMainComposition(Composition mainComp) {
		this.mainComp = mainComp;
	}

	public Path getProjectFilePath() {
		return projectFilePath;
	}

	public void setProjectFilePath(Path projectFilePath) {
		this.projectFilePath = projectFilePath;
	}
}
