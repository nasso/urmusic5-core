/*******************************************************************************
 * urmusic - The Free and Open Source Music Visualizer Tool
 * Copyright (C) 2018  nasso (https://github.com/nasso)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * Contact "nasso": nassomails -at- gmail dot com
 ******************************************************************************/
package io.github.nasso.urmusic.view.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import io.github.nasso.urmusic.view.data.UrmusicStrings;

public class UrmPathField extends JComponent {
	private Path p;
	private JTextField field;
	private JButton browseBtn;
	
	public UrmPathField(boolean directory, final boolean openMode) {
		this.setLayout(new BorderLayout(2, 2));
		this.add(this.field = new JTextField(), BorderLayout.CENTER);
		this.add(this.browseBtn = new JButton(new AbstractAction(UrmusicStrings.getString("global.browseFileButton")) {
			private JFileChooser fileChooser;
			
			public void actionPerformed(ActionEvent e) {
				LookAndFeel laf = UIManager.getLookAndFeel();
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					
					if(this.fileChooser == null) this.fileChooser = new JFileChooser();
					SwingUtilities.updateComponentTreeUI(this.fileChooser);
					this.fileChooser.setFileSelectionMode(directory ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_ONLY);
					int action = openMode ? this.fileChooser.showOpenDialog(UrmPathField.this) : this.fileChooser.showSaveDialog(UrmPathField.this);
					
					if(action == JFileChooser.APPROVE_OPTION) {
						File f = this.fileChooser.getSelectedFile();
						
						if(f != null)
							UrmPathField.this.setPath(f.toPath());
					}
					
					UIManager.setLookAndFeel(laf);
				} catch(ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e1) {
					e1.printStackTrace();
				}
			}
		}), BorderLayout.EAST);

		this.field.getDocument().addDocumentListener(new DocumentListener() {
			public void removeUpdate(DocumentEvent e) {
				UrmPathField.this.setPathNoField(Paths.get(UrmPathField.this.field.getText()));
			}
			
			public void insertUpdate(DocumentEvent e) {
				UrmPathField.this.setPathNoField(Paths.get(UrmPathField.this.field.getText()));
			}
			
			public void changedUpdate(DocumentEvent e) {
				UrmPathField.this.setPathNoField(Paths.get(UrmPathField.this.field.getText()));
			}
		});
		
		this.field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY), BorderFactory.createEmptyBorder(0, 4, 0, 4)));
		this.browseBtn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY), BorderFactory.createEmptyBorder(0, 8, 0, 8)));
	}
	
	private boolean setPathNoField(Path p) {
		if(this.p != null && this.p.equals(p)) return false;
		
		p = p.toAbsolutePath();
		this.p = p;
		
		return true;
	}
	
	public void setPath(Path p) {
		if(!this.setPathNoField(p)) return;
		
		this.field.setText(this.p.toString());
	}
	
	public Path getPath() {
		return this.p;
	}
	
	public void setEditable(boolean editable) {
		this.field.setEditable(editable);
	}
	
	public boolean isEditable() {
		return this.field.isEditable();
	}
}
