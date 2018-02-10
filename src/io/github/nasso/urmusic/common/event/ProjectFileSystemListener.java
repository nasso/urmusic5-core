package io.github.nasso.urmusic.common.event;

import io.github.nasso.urmusic.model.project.ProjectFileSystem;
import io.github.nasso.urmusic.model.project.ProjectFileSystem.ProjectElement;

public interface ProjectFileSystemListener {
	public void elementRenamed(ProjectFileSystem source, ProjectElement file);
	public void elementAdded(ProjectFileSystem source, ProjectElement file);
	public void elementRemoved(ProjectFileSystem source, ProjectElement file);
}
