package io.github.nasso.urmusic.common.event;

import io.github.nasso.urmusic.model.project.Project;

public interface ProjectLoadingListener {
	/**
	 * Fired before {@link projectLoaded} when the user opens a new project (it closes this one).
	 * @param p
	 */
	public void projectUnloaded(Project p);

	/**
	 * Fired after {@link projectUnloaded} when the user opens a new project (the previous one is closed before).
	 * @param p
	 */
	public void projectLoaded(Project p);
}
