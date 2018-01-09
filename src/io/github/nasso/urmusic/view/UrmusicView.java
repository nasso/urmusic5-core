package io.github.nasso.urmusic.view;

import static io.github.nasso.urmusic.view.data.UrmusicStrings.*;

import java.awt.event.ActionEvent;
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

import io.github.nasso.urmusic.controller.UrmusicController;
import io.github.nasso.urmusic.view.components.UrmusicSplittablePane;
import io.github.nasso.urmusic.view.data.UrmusicIcons;
import io.github.nasso.urmusic.view.data.UrmusicStrings;
import io.github.nasso.urmusic.view.data.UrmusicViewState;
import io.github.nasso.urmusic.view.data.UrmusicViewStateCodec;

public class UrmusicView {
	private static List<JFrame> frames = new ArrayList<JFrame>();
	
	private static UrmusicViewState viewState = new UrmusicViewState();
	
	private static Action menuAboutAction;
	
	public static final void init() {
		// TODO: Load Locale from pref file
		UrmusicStrings.init(Locale.ENGLISH);
		UrmusicIcons.init();
		
		setupActions();
		buildMenu();
		
		loadViewState();
		
		UrmusicSplittablePane.popupNew();
	}
	
	public static final void registerFrame(JFrame frame) {
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
				frames.remove(frame);
				checkFramesLeft();
			}
		});
		
		frame.setJMenuBar(buildMenu());
		frames.add(frame);
	}
	
	private static void checkFramesLeft() {
		if(frames.isEmpty()) UrmusicController.requestExit();
	}
	
	private static void setupActions() {
		menuAboutAction = new AbstractAction(getString("menu.help.about")) {
			private static final long serialVersionUID = -48007517019376751L;

			public void actionPerformed(ActionEvent e) {
				// TODO: Open About Dialog (with a link to https://icons8.com/)
			}
		};
	}
	
	private static JMenuBar buildMenu() {
		JMenuBar mb = new JMenuBar();
		
		JMenu helpMenu = new JMenu(getString("menu.help"));
		JMenuItem aboutItem = new JMenuItem();
		aboutItem.setAction(menuAboutAction);
		helpMenu.add(aboutItem);
		mb.add(helpMenu);
		
		return mb;
	}
	
	public static boolean loadViewState() {
		File viewStateFile = new File("appdata/view-state.dat");
		
		if(!viewStateFile.exists()) return false;
		
		try(BufferedInputStream in = new BufferedInputStream(new FileInputStream(viewStateFile))) {
			UrmusicViewStateCodec.readState(viewState, in);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public static void saveViewState() {
		File viewStateFile = new File("appdata/view-state.dat");
		
		try(BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(viewStateFile))) {
			UrmusicViewStateCodec.writeState(viewState, out);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
