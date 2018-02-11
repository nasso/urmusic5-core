package io.github.nasso.urmusic.model.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.nasso.urmusic.common.DataUtils;
import io.github.nasso.urmusic.common.event.ProjectFileSystemListener;

public class ProjectFileSystem {
	public abstract class ProjectElement {
		private ProjectDirectory parent = null;
		private String name;
		
		private ProjectElement() {
		}
		
		private ProjectElement(String name) {
			this.name = this.ensureNameValid(name);
		}
		
		private String ensureNameValid(String name) {
			if(name.equals(this.name)) return name;
			
			String validName = name;
			if(this.parent != null) {
				for(int i = 1; this.parent.hasChildren(validName); i++) {
					validName = name + " (" + i + ")";
				}
			}
			
			return validName;
		}

		public void setName(String name) {
			name = this.ensureNameValid(name);
			if(name.equals(this.name)) return;
			
			this.name = name;
			ProjectFileSystem.this.notifyElementRenamed(this);
		}
		
		public String getName() {
			return this.name;
		}

		public ProjectDirectory getParent() {
			return this.parent;
		}
		
		public String getProjectPath() {
			return (this.getParent() != null ? this.getParent().getProjectPath() + this.getName() : this.getName());
		}
		
		/**
		 * Returns true if this file/directory is present in the project file system structure.
		 */
		public boolean exists() {
			return this == ProjectFileSystem.this.root || (this.parent != null && this.parent.exists());
		}
	}
	
	public class ProjectFile extends ProjectElement {
		private String externalLink;
		
		private ProjectFile(String externalLink) {
			super(DataUtils.getPathName(externalLink));
			this.setExternalLink(externalLink);
		}
		
		public String getExternalLink() {
			return this.externalLink;
		}

		public void setExternalLink(String externalLink) {
			this.externalLink = externalLink;
		}
	}
	
	public class ProjectDirectory extends ProjectElement {
		private List<ProjectElement> content = new ArrayList<>();
		private List<ProjectElement> unmodifiableContent = Collections.unmodifiableList(this.content);
		
		private ProjectDirectory(String name) {
			super(name);
		}
		
		public List<ProjectElement> getContent() {
			return this.unmodifiableContent;
		}
		
		public void add(ProjectElement elem) {
			if(elem.parent == this) return;
			if(elem.parent != null) elem.parent.content.remove(elem);
			
			this.content.add(elem);
			elem.parent = this;
			
			if(this.exists()) ProjectFileSystem.this.notifyElementAdded(elem);
		}
		
		public void remove(ProjectElement elem) {
			if(elem.parent != this) return;
			
			this.content.remove(elem);
			elem.parent = null;
			
			if(this.exists()) ProjectFileSystem.this.notifyElementRemoved(elem);
		}
		
		public ProjectElement find(String path) {
			// Normalize the path
			path = DataUtils.normalizePath(path);
			
			if(path.isEmpty()) return this;
			
			String[] elems = path.split("/");
			
			boolean nextIsLast = elems.length == 1;
			String nextElem = elems[0];
			
			if(nextIsLast) {
				// -- Next is the last one
				for(ProjectElement e : this.content) {
					if(e.getName().equals(nextElem)) return e;
				}
			} else {
				// -- Next is a directory
				String subpath = path.substring(nextElem.length() + 1);
				
				for(ProjectElement e : this.content) {
					if(e.getName().equals(nextElem) && e instanceof ProjectDirectory) return ((ProjectDirectory) e).find(subpath);
				}
			}
			
			return null;
		}
		
		public boolean hasChildren(String childName) {
			for(ProjectElement e : this.content) {
				if(e.getName().equals(childName)) return true;
			}
			
			return false;
		}
		
		private void generateDescription(StringBuilder builder, int tabs) {
			builder.append("+ " + this.getName() + "\n");
			
			String prefix = new String(new char[tabs + 1]).replace("\0", "|\t"); // prefix = rep("|\t", tabs + 1)
			
			for(int i = 0, l = this.content.size(); i < l; i++) {
				builder.append(prefix + "|\n" + prefix);
				
				ProjectElement elem = this.content.get(i);
				
				if(elem instanceof ProjectDirectory) {
					((ProjectDirectory) elem).generateDescription(builder, tabs + 1);
				} else {
					builder.append("+ " + elem.getName() + "\n");
				}
			}
		}
		
		public String toString() {
			StringBuilder builder = new StringBuilder();
			
			this.generateDescription(builder, 0);
			builder.append("o"); // Final "o" (makes it prettier :D)
			
			return builder.toString();
		}
	}
	
	private List<ProjectFileSystemListener> listeners = new ArrayList<>();
	private ProjectDirectory root = new ProjectDirectory("");
	
	public ProjectFileSystem() {
	}
	
	public ProjectFile createFile(String link) {
		return new ProjectFile(link);
	}
	
	public ProjectDirectory createDirectory(String name) {
		return new ProjectDirectory(name);
	}
	
	public List<ProjectElement> getFilesIn(String dir) {
		ProjectElement e = this.root.find(dir);
		
		if(e == null || !(e instanceof ProjectDirectory))
			return null;
		
		return ((ProjectDirectory) e).unmodifiableContent;
	}
	
	public ProjectDirectory root() {
		return this.root;
	}
	
	public String toString() {
		return this.root.toString();
	}
	
	public void addListener(ProjectFileSystemListener l) {
		this.listeners.add(l);
	}
	
	public void removeListener(ProjectFileSystemListener l) {
		this.listeners.remove(l);
	}

	private void notifyElementRenamed(ProjectElement f) {
		for(ProjectFileSystemListener l : this.listeners) {
			l.elementRenamed(this, f);
		}
	}
	
	private void notifyElementAdded(ProjectElement f) {
		for(ProjectFileSystemListener l : this.listeners) {
			l.elementAdded(this, f);
		}
	}
	
	private void notifyElementRemoved(ProjectElement f) {
		for(ProjectFileSystemListener l : this.listeners) {
			l.elementRemoved(this, f);
		}
	}
}
