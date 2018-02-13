package io.github.nasso.urmusic.view.panes.project;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;

import io.github.nasso.urmusic.controller.UrmusicController;
import io.github.nasso.urmusic.view.UrmusicView;
import io.github.nasso.urmusic.view.components.UrmMenu;
import io.github.nasso.urmusic.view.components.UrmViewPane;
import io.github.nasso.urmusic.view.data.UrmusicStrings;

public class ProjectView extends UrmViewPane {
	public static final String VIEW_NAME = "project";

	private ProjectFileTreeModel treeModel;
	private JTree tree;
	
	public ProjectView() {
		this.addMenu(new UrmMenu(UrmusicStrings.getString("view." + VIEW_NAME + ".menu.add"),
			new JMenuItem(new AbstractAction(UrmusicStrings.getString("view." + VIEW_NAME + ".menu.add.importFile")) {
				private JFileChooser chooser;
				
				public void actionPerformed(ActionEvent e) {
					LookAndFeel laf = UIManager.getLookAndFeel();
					try {
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
						
						if(this.chooser == null) this.chooser = new JFileChooser(UrmusicStrings.getString("view." + VIEW_NAME + ".dialog.importFile.title"));
						this.chooser.setMultiSelectionEnabled(true);
						this.chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
						int action = this.chooser.showOpenDialog(ProjectView.this);
						
						if(action == JFileChooser.APPROVE_OPTION) {
							File[] files = this.chooser.getSelectedFiles();
							
							for(File f : files) {
								UrmusicController.importFile(f.getAbsolutePath());
							}
						}
						
						UIManager.setLookAndFeel(laf);
					} catch(ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e1) {
						e1.printStackTrace();
					}
				}
			}),
			new JMenuItem(new AbstractAction(UrmusicStrings.getString("view." + VIEW_NAME + ".menu.add.newDirectory")) {
				public void actionPerformed(ActionEvent e) {
					UrmusicController.newDirectory("New Directory");
				}
			})
		));

		this.treeModel = new ProjectFileTreeModel();
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		DefaultTreeCellEditor editor = new DefaultTreeCellEditor(this.tree, renderer) {
		    protected void prepareForEditing() {
		    	super.prepareForEditing();
		    	UrmusicView.blockKeyEvent();
		    }
		};
		editor.addCellEditorListener(new CellEditorListener() {
			public void editingStopped(ChangeEvent e) {
		    	UrmusicView.freeKeyEvent();
			}
			
			public void editingCanceled(ChangeEvent e) {
		    	UrmusicView.freeKeyEvent();
			}
		});
		
		this.tree = new JTree(this.treeModel);
		this.tree.setCellRenderer(renderer);
		this.tree.setCellEditor(editor);
		this.tree.setEditable(true);
		
		this.tree.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		this.tree.setRootVisible(false);
		
		this.setLayout(new BorderLayout());
		this.add(new JScrollPane(this.tree), BorderLayout.CENTER);
	}

	public void dispose() {
		this.treeModel.dispose();
	}
}
