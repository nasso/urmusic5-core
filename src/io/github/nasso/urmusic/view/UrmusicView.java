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
package io.github.nasso.urmusic.view;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import io.github.nasso.urmusic.common.DataUtils;
import io.github.nasso.urmusic.common.ObservableValue;
import io.github.nasso.urmusic.common.event.ProjectListener;
import io.github.nasso.urmusic.common.event.ValueChangeListener;
import io.github.nasso.urmusic.controller.UrmusicController;
import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.project.codec.ProjectCodec;
import io.github.nasso.urmusic.view.components.SplittablePane;
import io.github.nasso.urmusic.view.data.UrmusicSplittedPaneState;
import io.github.nasso.urmusic.view.data.UrmusicStrings;
import io.github.nasso.urmusic.view.data.UrmusicUIRes;
import io.github.nasso.urmusic.view.data.UrmusicViewState;
import io.github.nasso.urmusic.view.data.UrmusicViewStateCodec;
import io.github.nasso.urmusic.view.dialog.UrmusicAboutDialog;
import io.github.nasso.urmusic.view.dialog.UrmusicExportingDialog;

/**
 * User interface to the controller.
 * 
 * @author nasso
 */
public class UrmusicView {
	private static List<JFrame> frames = new ArrayList<>();
	private static JDialog audioLoadingDialog = null;
	
	private static UrmusicViewState viewState = null;
	
	private static ObservableValue<String> frameTitle;
	
	private static Action
		// File
		menuNewAction,
		menuSaveAction,
		menuSaveAsAction,
		menuOpenAction,
		menuLoadSongAction,
		menuExportAction,
		menuExitAction,
		
		// Help
		menuAboutAction;
	
	private static boolean keyEventBlocked = false;
	
	public static void init() {
		// TODO: Load Locale from pref file
		UrmusicStrings.init(UrmusicView.getLocale());
		UrmusicUIRes.init();
		
		UrmusicView.setupActions();
		UrmusicView.buildMenu();
		
		UrmusicView.loadViewState();
		
		UrmusicView.frameTitle = new ObservableValue<>();
		UrmusicView.updateFrameTitle();
		
		frameTitle.addListener(new ValueChangeListener<String>() {
			public void valueChanged(String oldValue, String newValue) {
				for(JFrame frame : frames)
					frame.setTitle(newValue);
			}
		});
		
		UrmusicView.audioLoadingDialog = new JDialog();
		UrmusicView.audioLoadingDialog.setTitle(UrmusicStrings.getString("dialog.loadingSong.title"));
		UrmusicView.audioLoadingDialog.setLocationRelativeTo(null);
		UrmusicView.audioLoadingDialog.setContentPane(new JPanel() {
			{
				this.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
			}
		});
		UrmusicView.audioLoadingDialog.getContentPane().add(new JProgressBar() {
			{
				this.setIndeterminate(true);
			}
		});
		UrmusicView.audioLoadingDialog.pack();
		
		UrmusicModel.addExitHook(() -> {
			UrmusicView.saveViewState();
			UrmusicView.dispose();
		});
		
		UrmusicController.addProjectListener(new ProjectListener() {
			public void changed() {
				updateFrameTitle();
			}
			
			public void saved() {
				updateFrameTitle();
			}
			
			public void loaded() {
				updateFrameTitle();
			}
		});
		
		SwingUtilities.invokeLater(() -> {
			KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(UrmusicView::keyEvent);
			
			if(UrmusicView.viewState == null || UrmusicView.viewState.getPaneStates().length == 0) {
				SplittablePane.popupNew();
			} else {
				UrmusicSplittedPaneState[] frames = UrmusicView.viewState.getPaneStates();
				for(int i = 0; i < UrmusicView.viewState.getPaneStates().length; i++) {
					SplittablePane pane = SplittablePane.popupNew();
					pane.loadState(frames[i]);
				}
			}			
		});
	}
	
	private static StringBuilder titleBuilder = null;
	private static int titleBaseLength = 0;
	private static void updateFrameTitle() {
		if(titleBuilder == null) {
			titleBuilder = new StringBuilder(UrmusicStrings.getString("title")).append(" - ");
			titleBaseLength = titleBuilder.length();
		}
		
		Path projectPath = UrmusicController.getCurrentProjectPath();
		
		titleBuilder.replace(titleBaseLength, titleBuilder.length(), projectPath == null ? UrmusicStrings.getString("title.untitled") : projectPath.getFileName().toString());
		
		if(UrmusicController.projectHasUnsavedChanges()) titleBuilder.append("*");
		
		frameTitle.set(titleBuilder.toString());
	}
	
	public static void dispose() {
		for(JFrame frame : UrmusicView.frames) {
			frame.dispose();
		}
		
		UrmusicView.frames.clear();
	}
	
	public static Locale getLocale() {
		return Locale.ENGLISH;
	}
	
	public static void registerFrame(JFrame frame) {
		frame.setTitle(frameTitle.get());
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if(UrmusicView.frames.size() > 1) {
					int chosen = JOptionPane.showOptionDialog(
						frame,
						UrmusicStrings.getString("frame.multiCloseWarning.message"),
						UrmusicStrings.getString("frame.multiCloseWarning.title"),
						JOptionPane.CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						new String[]{
							UrmusicStrings.getString("frame.multiCloseWarning.closeOne"),
							UrmusicStrings.getString("frame.multiCloseWarning.closeAll"),
							UrmusicStrings.getString("frame.multiCloseWarning.cancel")
						},
						null
					);
					
					switch(chosen) {
						case 1: // close all
							UrmusicController.requestExit();
						case 0: // close one
							break;
						default: // cancel
							return;
					}
				}
				
				// If this is the last window left, exit
				if(UrmusicView.frames.size() == 1) UrmusicController.requestExit();
				else UrmusicView.frames.remove(frame);
				
				// Close the window
				e.getWindow().dispose();
			}
		});
		
		frame.setJMenuBar(UrmusicView.buildMenu());
		UrmusicView.frames.add(frame);
	}
	
	private static void setupActions() {
		JFileChooser fileChooser = new JFileChooser();
		FileFilter projectFilter = new FileNameExtensionFilter(UrmusicStrings.getString("files.project.desc") + " (*." + ProjectCodec.FILE_EXT + ")", ProjectCodec.FILE_EXT);

		UrmusicView.menuNewAction = new AbstractAction(UrmusicStrings.getString("menu.file.new")) {
			public void actionPerformed(ActionEvent e) {
				UrmusicController.newProject();
			}
		};
		
		UrmusicView.menuSaveAction = new AbstractAction(UrmusicStrings.getString("menu.file.save")) {
			public void actionPerformed(ActionEvent e) {
				if(UrmusicController.getCurrentProjectPath() == null) UrmusicView.menuSaveAsAction.actionPerformed(e);
				else {
					UrmusicController.saveCurrentProject();
				}
			}
		};
		
		UrmusicView.menuSaveAsAction = new AbstractAction(UrmusicStrings.getString("menu.file.saveAs")) {
			public void actionPerformed(ActionEvent e) {
				LookAndFeel laf = UIManager.getLookAndFeel();
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					SwingUtilities.updateComponentTreeUI(fileChooser);
					
					fileChooser.setFileFilter(projectFilter);
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					int action = fileChooser.showSaveDialog(e.getSource() instanceof Component ? (Component) e.getSource() : null);
					
					if(action == JFileChooser.APPROVE_OPTION) {
						File f = fileChooser.getSelectedFile();
						
						if(f != null) {
							UrmusicController.saveCurrentProject(f.toPath());
						}
					}
					
					UIManager.setLookAndFeel(laf);
				} catch(ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e1) {
					e1.printStackTrace();
				}
			}
		};
		
		UrmusicView.menuOpenAction = new AbstractAction(UrmusicStrings.getString("menu.file.open")) {
			public void actionPerformed(ActionEvent e) {
				LookAndFeel laf = UIManager.getLookAndFeel();
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					SwingUtilities.updateComponentTreeUI(fileChooser);

					fileChooser.setFileFilter(projectFilter);
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					int action = fileChooser.showOpenDialog(e.getSource() instanceof Component ? (Component) e.getSource() : null);
					
					if(action == JFileChooser.APPROVE_OPTION) {
						File f = fileChooser.getSelectedFile();
						
						if(f != null) {
							UrmusicController.openProject(f.toPath());
						}
					}
					
					UIManager.setLookAndFeel(laf);
				} catch(ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e1) {
					e1.printStackTrace();
				}
			}
		};
		
		UrmusicView.menuLoadSongAction = new AbstractAction(UrmusicStrings.getString("menu.file.loadSong")) {
			public void actionPerformed(ActionEvent e) {
				LookAndFeel laf = UIManager.getLookAndFeel();
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					SwingUtilities.updateComponentTreeUI(fileChooser);

					// TODO: File filter for songs
					fileChooser.setFileFilter(null);
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					int action = fileChooser.showOpenDialog(e.getSource() instanceof Component ? (Component) e.getSource() : null);
					
					if(action == JFileChooser.APPROVE_OPTION) {
						File f = fileChooser.getSelectedFile();
						
						if(f != null) {
							UrmusicView.showAudioLoadingDialog();
							UrmusicController.setCurrentSong(f.toPath(), UrmusicView::closeAudioLoadingDialog);
						}
					}
					
					UIManager.setLookAndFeel(laf);
				} catch(ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e1) {
					e1.printStackTrace();
				}
			}
		};
		
		UrmusicView.menuExportAction = new AbstractAction(UrmusicStrings.getString("menu.file.export")) {
			private UrmusicExportingDialog dialog = new UrmusicExportingDialog();
			
			public void actionPerformed(ActionEvent e) {
				this.dialog.open();
			}
		};
		
		UrmusicView.menuExitAction = new AbstractAction(UrmusicStrings.getString("menu.file.quit")) {
			public void actionPerformed(ActionEvent e) {
				UrmusicController.requestExit();
			}
		};
		
		UrmusicView.menuAboutAction = new AbstractAction(UrmusicStrings.getString("menu.help.about")) {
			private final UrmusicAboutDialog aboutDialog = new UrmusicAboutDialog();
			
			public void actionPerformed(ActionEvent e) {
				this.aboutDialog.setVisible(true);
			}
		};
	}
	
	private static JMenuBar buildMenu() {
		JMenuBar mb = new JMenuBar();
		
		JMenu fileMenu = new JMenu(UrmusicStrings.getString("menu.file"));
		fileMenu.add(new JMenuItem(UrmusicView.menuNewAction));
		fileMenu.add(new JMenuItem(UrmusicView.menuSaveAction));
		fileMenu.add(new JMenuItem(UrmusicView.menuSaveAsAction));
		fileMenu.add(new JMenuItem(UrmusicView.menuOpenAction));
		fileMenu.addSeparator();
		fileMenu.add(new JMenuItem(UrmusicView.menuLoadSongAction));
		fileMenu.addSeparator();
		fileMenu.add(new JMenuItem(UrmusicView.menuExportAction));
		fileMenu.addSeparator();
		fileMenu.add(new JMenuItem(UrmusicView.menuExitAction));
		
		JMenu helpMenu = new JMenu(UrmusicStrings.getString("menu.help"));
		helpMenu.add(new JMenuItem(UrmusicView.menuAboutAction));
		
		mb.add(fileMenu);
		mb.add(helpMenu);
		
		return mb;
	}
	
	private static void showAudioLoadingDialog() {
		SwingUtilities.invokeLater(() -> {
			UrmusicView.audioLoadingDialog.setVisible(true);
		});
	}
	
	private static void closeAudioLoadingDialog() {
		SwingUtilities.invokeLater(() -> {
			UrmusicView.audioLoadingDialog.setVisible(false);
		});
	}
	
	public static void loadViewState() {
		File viewStateFile = DataUtils.localFile("view-state.dat");
		
		if(!viewStateFile.exists()) {
			viewStateFile = DataUtils.localFile("default-view-state.dat");
			if(!viewStateFile.exists()) return;
		}
		
		try(BufferedInputStream in = new BufferedInputStream(new FileInputStream(viewStateFile))) {
			UrmusicView.viewState = UrmusicViewStateCodec.readState(in);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveViewState() {
		File viewStateFile = DataUtils.localFile("view-state.dat");
		if(!viewStateFile.getParentFile().exists()) viewStateFile.getParentFile().mkdirs();
		
		UrmusicSplittedPaneState[] paneStates = new UrmusicSplittedPaneState[UrmusicView.frames.size()];
		for(int i = 0; i < paneStates.length; i++) paneStates[i] = ((SplittablePane) UrmusicView.frames.get(i).getContentPane()).saveState();

		UrmusicView.viewState = new UrmusicViewState();
		UrmusicView.viewState.setPaneStates(paneStates);
		
		try(BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(viewStateFile))) {
			UrmusicViewStateCodec.writeState(UrmusicView.viewState, out);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void blockKeyEvent() {
		UrmusicView.keyEventBlocked = true;
	}
	
	public static void freeKeyEvent() {
		UrmusicView.keyEventBlocked = false;
	}
	
	public static boolean keyEvent(KeyEvent e) {
		if(UrmusicView.keyEventBlocked) return false;
		
		boolean nofocus = true;
		for(JFrame frame : UrmusicView.frames) {
			if(frame.isFocused()) {
				nofocus = false;
				break;
			}
		}
		if(nofocus) return false;
		
		if(e.getID() == KeyEvent.KEY_PRESSED) {
			switch(e.getKeyCode()) {
				case KeyEvent.VK_RIGHT:
					UrmusicController.frameAdvance();
					return true;
				case KeyEvent.VK_LEFT:
					UrmusicController.frameBack();
					return true;
				case KeyEvent.VK_UP:
					UrmusicController.goToNextKeyFrame();
					return true;
				case KeyEvent.VK_DOWN:
					UrmusicController.goToPreviousKeyFrame();
					return true;
				case KeyEvent.VK_SPACE:
					UrmusicController.playPause();
					return true;
				case KeyEvent.VK_S:
					UrmusicController.splitTrack();
					return true;
			}
		}
		
		return false;
	}
}
