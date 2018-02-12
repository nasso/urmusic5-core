package io.github.nasso.urmusic.model.project.control;

import io.github.nasso.urmusic.model.project.ProjectFileSystem.ProjectFile;

public class FileParam extends EffectParam<ProjectFile> {
	private ProjectFile val;
	
	public FileParam(String name) {
		super(name, false);
	}
	

	protected void setStaticValue(ProjectFile val) {
		this.val = val;
	}
	
	protected ProjectFile getStaticValue() {
		return this.val;
	}
	
	protected ProjectFile cloneValue(ProjectFile val) {
		return null;
	}
	
	public ProjectFile ramp(ProjectFile s, ProjectFile e, float t) {
		return null;
	}
}
