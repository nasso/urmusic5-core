package io.github.nasso.urmusic.view;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import io.github.nasso.urmusic.common.DataUtils;
import io.github.nasso.urmusic.controller.UrmusicController;
import io.github.nasso.urmusic.view.components.SplittablePane;
import io.github.nasso.urmusic.view.data.UrmusicSplittedPaneState;
import io.github.nasso.urmusic.view.data.UrmusicStrings;
import io.github.nasso.urmusic.view.data.UrmusicUIRes;
import io.github.nasso.urmusic.view.data.UrmusicViewState;
import io.github.nasso.urmusic.view.data.UrmusicViewStateCodec;
import io.github.nasso.urmusic.view.dialog.UrmusicAboutDialog;

/**
 * User interface to the controller.
 * 
 * @author nasso
 */
public class UrmusicView {
	private static List<JFrame> frames = new ArrayList<>();
	
	private static UrmusicViewState viewState = null;
	
	private static Action menuExitAction, menuAboutAction;
	
	private static boolean keyEventBlocked = false;
	
	public static void init() {
		// TODO: Load Locale from pref file
		UrmusicStrings.init(UrmusicView.getLocale());
		UrmusicUIRes.init();
		
		UrmusicView.setupActions();
		UrmusicView.buildMenu();
		
		UrmusicView.loadViewState();

		SwingUtilities.invokeLater(() -> {
			KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher((e) -> {
							return UrmusicView.keyEvent(e);
			});
			
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
		fileMenu.add(new JMenuItem(UrmusicView.menuExitAction));
		
		JMenu helpMenu = new JMenu(UrmusicStrings.getString("menu.help"));
		helpMenu.add(new JMenuItem(UrmusicView.menuAboutAction));
		
		mb.add(fileMenu);
		mb.add(helpMenu);
		
		return mb;
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
