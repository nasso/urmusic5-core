package io.github.nasso.urmusic.view;

import static io.github.nasso.urmusic.view.data.UrmusicStrings.*;

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

public class UrmusicView {
	private static List<JFrame> frames = new ArrayList<JFrame>();
	
	private static UrmusicViewState viewState = null;
	
	private static Action menuExitAction, menuAboutAction;
	
	private static boolean keyEventBlocked = false;
	
	public static void init() {
		// TODO: Load Locale from pref file
		UrmusicStrings.init(getLocale());
		UrmusicUIRes.init();
		
		setupActions();
		buildMenu();
		
		loadViewState();

		SwingUtilities.invokeLater(() -> {
			KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher((e) -> {
							return UrmusicView.keyEvent(e);
			});
			
			if(viewState == null || viewState.getPaneStates().length == 0) {
				SplittablePane.popupNew();
			} else {
				UrmusicSplittedPaneState[] frames = viewState.getPaneStates();
				for(int i = 0; i < viewState.getPaneStates().length; i++) {
					SplittablePane pane = SplittablePane.popupNew();
					pane.loadState(frames[i]);
				}
			}			
		});
	}
	
	public static void dispose() {
		for(JFrame frame : frames) {
			frame.dispose();
		}
		
		frames.clear();
	}
	
	public static Locale getLocale() {
		return Locale.ENGLISH;
	}
	
	public static void registerFrame(JFrame frame) {
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if(frames.size() > 1) {
					int chosen = JOptionPane.showOptionDialog(
						frame,
						getString("frame.multiCloseWarning.message"),
						getString("frame.multiCloseWarning.title"),
						JOptionPane.CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						new String[]{
							getString("frame.multiCloseWarning.closeOne"),
							getString("frame.multiCloseWarning.closeAll"),
							getString("frame.multiCloseWarning.cancel")
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
				if(frames.size() == 1) UrmusicController.requestExit();
				else frames.remove(frame);
				
				// Close the window
				e.getWindow().dispose();
			}
		});
		
		frame.setJMenuBar(buildMenu());
		frames.add(frame);
	}
	
	private static void setupActions() {
		menuExitAction = new AbstractAction(getString("menu.file.quit")) {
			public void actionPerformed(ActionEvent e) {
				UrmusicController.requestExit();
			}
		};
		
		menuAboutAction = new AbstractAction(getString("menu.help.about")) {
			private final UrmusicAboutDialog aboutDialog = new UrmusicAboutDialog();
			
			public void actionPerformed(ActionEvent e) {
				this.aboutDialog.setVisible(true);
			}
		};
	}
	
	private static JMenuBar buildMenu() {
		JMenuBar mb = new JMenuBar();
		
		JMenu fileMenu = new JMenu(getString("menu.file"));
		fileMenu.add(new JMenuItem(menuExitAction));
		
		JMenu helpMenu = new JMenu(getString("menu.help"));
		helpMenu.add(new JMenuItem(menuAboutAction));
		
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
			viewState = UrmusicViewStateCodec.readState(in);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveViewState() {
		File viewStateFile = DataUtils.localFile("view-state.dat");
		if(!viewStateFile.getParentFile().exists()) viewStateFile.getParentFile().mkdirs();
		
		UrmusicSplittedPaneState[] paneStates = new UrmusicSplittedPaneState[frames.size()];
		for(int i = 0; i < paneStates.length; i++) paneStates[i] = ((SplittablePane) frames.get(i).getContentPane()).saveState();

		viewState = new UrmusicViewState();
		viewState.setPaneStates(paneStates);
		
		try(BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(viewStateFile))) {
			UrmusicViewStateCodec.writeState(viewState, out);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void blockKeyEvent() {
		keyEventBlocked = true;
	}
	
	public static void freeKeyEvent() {
		keyEventBlocked = false;
	}
	
	public static boolean keyEvent(KeyEvent e) {
		if(keyEventBlocked) return false;
		
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
