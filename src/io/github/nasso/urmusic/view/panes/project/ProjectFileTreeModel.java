package io.github.nasso.urmusic.view.panes.project;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import io.github.nasso.urmusic.common.event.ProjectFileSystemListener;
import io.github.nasso.urmusic.common.event.ProjectLoadingListener;
import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.project.Project;
import io.github.nasso.urmusic.model.project.ProjectFileSystem;
import io.github.nasso.urmusic.model.project.ProjectFileSystem.ProjectDirectory;
import io.github.nasso.urmusic.model.project.ProjectFileSystem.ProjectElement;
import io.github.nasso.urmusic.model.project.ProjectFileSystem.ProjectFile;

public class ProjectFileTreeModel implements TreeModel, ProjectFileSystemListener, ProjectLoadingListener {
	class ProjectElementNode implements TreeNode, Cloneable {
		private ProjectElement elem;
		
		private ProjectElementNode parent = null;
		private List<ProjectElementNode> children = new ArrayList<>();
		
		public ProjectElementNode(ProjectElementNode e) {
			this.reset(e.elem);
		}
		
		public ProjectElementNode(ProjectElement e) {
			this.reset(e);
		}
		
		public ProjectElementNode clone() {
			return new ProjectElementNode(this);
		}
		
		public void reset(ProjectElement e) {
			if(!this.children.isEmpty()) {
				for(ProjectElementNode c : this.children) {
					c.parent = null;
				}
				
				this.children.clear();
			}
			
			this.elem = e;
			if(e instanceof ProjectDirectory) {
				for(ProjectElement childElem : ((ProjectDirectory) e).getContent()) {
					this.addChild(new ProjectElementNode(childElem));
				}
			}
		}
		
		public int getLevel() {
			TreeNode ancestor;
			int levels = 0;
			
			ancestor = this;
			while((ancestor = ancestor.getParent()) != null) levels++;
			
			return levels;
		}
		
		private void buildPathHelper(List<Object> path) {
			if(this.parent != null) this.parent.buildPathHelper(path);
			path.add(this);
		}
		
		public Object[] buildPath() {
			List<Object> pathList = new ArrayList<>();
			
			this.buildPathHelper(pathList);
			
			Object[] path = new Object[pathList.size()];
			for(int i = 0; i < path.length; i++) {
				path[i] = pathList.get(i);
			}
			
			return path;
		}
		
		public int insertChild(ProjectElementNode child, int i) {
			if(child.parent != null) child.parent.children.remove(child);
			
			this.children.add(i, child);
			child.parent = this;
			
			return i;
		}
		
		public int addChild(ProjectElementNode child) {
			return this.insertChild(child, this.children.size());
		}
		
		public int removeFromParent() {
			if(this.parent == null) return -1;
			
			int i = this.parent.children.indexOf(this);
			this.parent.children.remove(this);
			this.parent = null;
			
			return i;
		}
		
		public TreeNode getChildAt(int childIndex) {
			return this.children.get(childIndex);
		}

		public int getChildCount() {
			return this.children.size();
		}

		public TreeNode getParent() {
			return this.parent;
		}

		public int getIndex(TreeNode node) {
			return this.children.indexOf(node);
		}

		public boolean getAllowsChildren() {
			return this.elem instanceof ProjectDirectory;
		}

		public boolean isLeaf() {
			return this.elem instanceof ProjectFile;
		}
		
		public String toString() {
			return this.elem.getName();
		}

		public Enumeration<ProjectElementNode> children() {
			return new Enumeration<ProjectFileTreeModel.ProjectElementNode>() {
				private int i;
				
				public boolean hasMoreElements() {
					return this.i < ProjectElementNode.this.children.size();
				}

				public ProjectElementNode nextElement() {
					return ProjectElementNode.this.children.get(this.i++);
				}
			};
		}
		
		public ProjectElementNode findNodeByElement(ProjectElement e) {
			if(this.elem == e) return this;

			for(ProjectElementNode node : this.children) {
				ProjectElementNode found = node.findNodeByElement(e);
				if(found != null) return found;
			}

			return null;
		}
		
		public ProjectElementNode findNode(Object n) {
			if(n == this) return this;

			for(ProjectElementNode node : this.children) {
				ProjectElementNode found = node.findNode(n);
				if(found != null) return found;
			}
			
			return null;
		}
	}
	
	private ProjectElementNode rootNode;
	private List<TreeModelListener> listeners = new ArrayList<>();
	
	public ProjectFileTreeModel() {
		this.rootNode = new ProjectElementNode(UrmusicModel.getCurrentProject().getFileSystem().root());
		
		UrmusicModel.addProjectLoadingListener(this);
		UrmusicModel.getCurrentProject().getFileSystem().addListener(this);
	}
	
	public void dispose() {
		UrmusicModel.removeProjectLoadingListener(this);
		UrmusicModel.getCurrentProject().getFileSystem().removeListener(this);
	}

	public Object getRoot() {
		return this.rootNode;
	}

	public Object getChild(Object parent, int index) {
		ProjectElementNode n = this.rootNode.findNode(parent);
		if(n == null) return null;
		
		return n.getChildAt(index);
	}

	public int getChildCount(Object parent) {
		ProjectElementNode n = this.rootNode.findNode(parent);
		if(n == null) return 0;
		
		return n.getChildCount();
	}

	public boolean isLeaf(Object node) {
		ProjectElementNode n = this.rootNode.findNode(node);
		if(n == null) return false;
		
		return n.isLeaf();
	}

	public void valueForPathChanged(TreePath path, Object newValue) {
		ProjectElementNode node = (ProjectElementNode) path.getLastPathComponent();
		
		node.elem.setName(newValue.toString());
	}

	public int getIndexOfChild(Object parent, Object child) {
		ProjectElementNode n = this.rootNode.findNode(parent);
		if(n == null) return -1;
		
		return n.getIndex((ProjectElementNode) child);
	}
	
	public void addTreeModelListener(TreeModelListener l) {
		this.listeners.add(l);
	}

	public void removeTreeModelListener(TreeModelListener l) {
		this.listeners.remove(l);
	}
	
	private void notifyTreeNodesChanged(TreeModelEvent e) {
		for(TreeModelListener l : this.listeners) {
			l.treeNodesChanged(e);
		}
	}
	
	private void notifyTreeNodesInserted(TreeModelEvent e) {
		for(TreeModelListener l : this.listeners) {
			l.treeNodesInserted(e);
		}
	}
	
	private void notifyTreeNodesRemoved(TreeModelEvent e) {
		for(TreeModelListener l : this.listeners) {
			l.treeNodesRemoved(e);
		}
	}
	
	private void notifyTreeStructureChanged(TreeModelEvent e) {
		for(TreeModelListener l : this.listeners) {
			l.treeStructureChanged(e);
		}	
	}
	
	private void forceUpdateProjectStructure(Project p) {
		this.rootNode.reset(p.getFileSystem().root());
		this.notifyTreeStructureChanged(new TreeModelEvent(this, new TreePath(this.rootNode)));
	}
	
	public void projectUnloaded(Project p) {
		p.getFileSystem().removeListener(this);
	}

	public void projectLoaded(Project p) {
		p.getFileSystem().addListener(this);
		this.forceUpdateProjectStructure(p);
	}

	public void elementRenamed(ProjectFileSystem source, ProjectElement e) {
		ProjectElementNode child = this.rootNode.findNodeByElement(e);
		ProjectElementNode node = child.parent;
		int i = node.getIndex(child);
		
		this.notifyTreeNodesChanged(new TreeModelEvent(this, node.buildPath(), new int[] { i }, new Object[] { child }));
	}

	public void elementAdded(ProjectFileSystem source, ProjectElement e) {
		ProjectDirectory dir = e.getParent();
		ProjectElementNode node = this.rootNode.findNodeByElement(dir);
		ProjectElementNode child = new ProjectElementNode(e);
		int i = node.addChild(child);
		
		this.notifyTreeNodesInserted(new TreeModelEvent(this, node.buildPath(), new int[] { i }, new Object[] { child }));
	}

	public void elementRemoved(ProjectFileSystem source, ProjectElement e) {
		ProjectElementNode child = this.rootNode.findNodeByElement(e);
		ProjectElementNode node = child.parent;
		int i = child.removeFromParent();
		
		this.notifyTreeNodesRemoved(new TreeModelEvent(this, node.buildPath(), new int[] { i }, new Object[] { child }));
	}
}
