package io.github.nasso.urmusic.view.panes.effectlist.controls;

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
import javax.swing.JPanel;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import io.github.nasso.urmusic.controller.UrmusicController;
import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.github.nasso.urmusic.model.project.param.FileParam;
import io.github.nasso.urmusic.view.components.UrmEditableLabel;
import io.github.nasso.urmusic.view.data.UrmusicStrings;

public class FileParamUI extends EffectParamUI<FileParam> {
	private JPanel container;
	private UrmEditableLabel urlField;
	private JButton browseButton;
	
	public FileParamUI(TrackEffectInstance fx, FileParam param) {
		super(fx, param);
	}
	
	public JComponent buildUI() {
		this.container = new JPanel(new BorderLayout(2, 2));
		this.urlField = new UrmEditableLabel((f) -> UrmusicController.setParamValueNow(this.getParam(), Paths.get(f.getValue())));
		this.browseButton = new JButton(new AbstractAction("...") {
			private JFileChooser fileChooser;
			
			public void actionPerformed(ActionEvent e) {
				LookAndFeel laf = UIManager.getLookAndFeel();
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					
					if(this.fileChooser == null) this.fileChooser = new JFileChooser();
					SwingUtilities.updateComponentTreeUI(this.fileChooser);
					this.fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					int action = this.fileChooser.showOpenDialog(FileParamUI.this);
					
					if(action == JFileChooser.APPROVE_OPTION) {
						File f = this.fileChooser.getSelectedFile();
						
						if(f != null)
							UrmusicController.setParamValueNow(FileParamUI.this.getParam(), f.toPath());
					}
					
					UIManager.setLookAndFeel(laf);
				} catch(ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		this.browseButton.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(Color.BLACK, 1),
			BorderFactory.createEmptyBorder(0, 6, 0, 6)
		));
		
		this.container.setOpaque(false);
		this.container.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		this.container.add(this.urlField, BorderLayout.CENTER);
		this.container.add(this.browseButton, BorderLayout.EAST);
		
		return this.container;
	}
	
	public void updateControl() {
		Path p = UrmusicController.getParamValueNow(this.getParam());
		
		this.urlField.setValue(
			p == null ?
				UrmusicStrings.getString(
					"effect." + this.getEffectInstance().getEffectClass().getEffectClassID() +
					".param." + this.getParam().getID() +
					".empty") :
				
				p.getFileName().toString()
		);
	}
}
