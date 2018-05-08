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
package io.github.nasso.urmusic.view.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import io.github.nasso.urmusic.common.ExportProgressCallback;
import io.github.nasso.urmusic.controller.UrmusicController;
import io.github.nasso.urmusic.model.exporter.ExportSettings;
import io.github.nasso.urmusic.model.exporter.Exporter.ExportJob;
import io.github.nasso.urmusic.model.ffmpeg.Encoder;
import io.github.nasso.urmusic.model.ffmpeg.FFmpeg;
import io.github.nasso.urmusic.model.ffmpeg.Muxer;
import io.github.nasso.urmusic.view.UrmusicView;
import io.github.nasso.urmusic.view.components.UrmPathField;
import io.github.nasso.urmusic.view.data.UrmusicStrings;
import io.github.nasso.urmusic.view.layout.VListLayout;

public class UrmusicExportingDialog extends JDialog {
	private static final Muxer[] MUXERS;
	private static final Encoder[] VIDEO_ENCODERS;
	private static final Encoder[] AUDIO_ENCODERS;
	
	static {
		MUXERS = FFmpeg.getMuxers().toArray(new Muxer[FFmpeg.getMuxers().size()]);
		
		int vidEncoderCount = 0;
		int audEncoderCount = 0;
		
		for(Encoder e : FFmpeg.getEncoders()) {
			switch(e.getType()) {
				case AUDIO:
					audEncoderCount++;
					break;
				case VIDEO:
					vidEncoderCount++;
					break;
			}
		}
		
		VIDEO_ENCODERS = new Encoder[vidEncoderCount];
		AUDIO_ENCODERS = new Encoder[audEncoderCount];
		
		int vi = 0;
		int ai = 0;
		for(Encoder e : FFmpeg.getEncoders()) {
			switch(e.getType()) {
				case AUDIO:
					AUDIO_ENCODERS[ai++] = e;
					break;
				case VIDEO:
					VIDEO_ENCODERS[vi++] = e;
					break;
			}
		}
	}
	
	private ExportSettings settings = new ExportSettings();
	
	private UrmPathField destField;
	private JComboBox<Muxer> muxerCBox;
	
	private JComboBox<Encoder> videoEncoderCBox;
	private JComboBox<Encoder> audioEncoderCBox;
	
	private JButton cancelBtn, startBtn;
	private JProgressBar pBar;
	
	private ExportJob job;
	
	public UrmusicExportingDialog() {
		JLabel lbl;
		
		JPanel topBar = new JPanel(new GridLayout(1, 2, 8, 8));
		topBar.add(this.destField = new UrmPathField(false));
		topBar.add(this.muxerCBox = new JComboBox<>(MUXERS));

		JPanel videoFieldsPane = new JPanel(new VListLayout(4));
		JPanel videoLabelsPane = new JPanel(new VListLayout(4));
		
		lbl = new JLabel(UrmusicStrings.getString("dialog.export.video.encoder") + ":", SwingConstants.RIGHT);
		lbl.setLabelFor(UrmusicExportingDialog.this.videoEncoderCBox = new JComboBox<>(VIDEO_ENCODERS));
		lbl.setPreferredSize(new Dimension(lbl.getPreferredSize().width, this.videoEncoderCBox.getPreferredSize().height));
		videoFieldsPane.add(this.videoEncoderCBox);
		videoLabelsPane.add(lbl);

		JPanel audioFieldsPane = new JPanel(new VListLayout(4));
		JPanel audioLabelsPane = new JPanel(new VListLayout(4));
		
		lbl = new JLabel(UrmusicStrings.getString("dialog.export.audio.encoder") + ":", SwingConstants.RIGHT);
		lbl.setLabelFor(UrmusicExportingDialog.this.audioEncoderCBox = new JComboBox<>(AUDIO_ENCODERS));
		lbl.setPreferredSize(new Dimension(lbl.getPreferredSize().width, this.audioEncoderCBox.getPreferredSize().height));
		audioFieldsPane.add(this.audioEncoderCBox);
		audioLabelsPane.add(lbl);
		
		JPanel videoFormatPane = new JPanel(new BorderLayout(4, 4));
		videoFormatPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(UrmusicStrings.getString("dialog.export.video.title")), BorderFactory.createEmptyBorder(4, 8, 4, 8)));
		videoFormatPane.add(videoLabelsPane, BorderLayout.WEST);
		videoFormatPane.add(videoFieldsPane, BorderLayout.CENTER);
		
		JPanel audioFormatPane = new JPanel(new BorderLayout(4, 4));
		audioFormatPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(UrmusicStrings.getString("dialog.export.audio.title")), BorderFactory.createEmptyBorder(4, 8, 4, 8)));
		audioFormatPane.add(audioLabelsPane, BorderLayout.WEST);
		audioFormatPane.add(audioFieldsPane, BorderLayout.CENTER);
		
		JPanel settingsContainer = new JPanel(new GridLayout(1, 2, 4, 4));
		settingsContainer.add(videoFormatPane);
		settingsContainer.add(audioFormatPane);
		
		JPanel bottomBar = new JPanel();
		bottomBar.setLayout(new BorderLayout(8, 8));
		bottomBar.add(this.cancelBtn = new JButton(new AbstractAction(UrmusicStrings.getString("dialog.global.cancel")) {
			public void actionPerformed(ActionEvent e) {
				UrmusicExportingDialog.this.cancel();
			}
		}), BorderLayout.WEST);
		bottomBar.add(this.pBar = new JProgressBar(0, 10000), BorderLayout.CENTER);
		bottomBar.add(this.startBtn = new JButton(new AbstractAction(UrmusicStrings.getString("dialog.export.start")) {
			public void actionPerformed(ActionEvent e) {
				UrmusicExportingDialog.this.start();
			}
		}), BorderLayout.EAST);
		
		this.pBar.setStringPainted(true);
		
		JPanel container = new JPanel(new BorderLayout(8, 8));
		container.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		container.add(topBar, BorderLayout.NORTH);
		container.add(settingsContainer, BorderLayout.CENTER);
		container.add(bottomBar, BorderLayout.SOUTH);
		
		this.setTitle(UrmusicStrings.getString("dialog.export.title"));
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.setModalityType(ModalityType.APPLICATION_MODAL);
		this.setContentPane(container);
		this.setSize(800, 480);
		this.setLocationRelativeTo(null);
		
		this.addWindowListener(new WindowListener() {
			public void windowOpened(WindowEvent e) {
				UrmusicView.blockKeyEvent();
			}
			
			public void windowIconified(WindowEvent e) {
			}
			
			public void windowDeiconified(WindowEvent e) {
			}
			
			public void windowDeactivated(WindowEvent e) {
			}
			
			public void windowClosing(WindowEvent e) {
				if(UrmusicExportingDialog.this.job != null && !UrmusicExportingDialog.this.job.isDone() && !UrmusicExportingDialog.this.job.isCancelled()) {
					if(!UrmusicExportingDialog.this.promptCancel()) return;
					UrmusicController.cancelExport(UrmusicExportingDialog.this.job);
				}
				
				UrmusicView.freeKeyEvent();
				UrmusicExportingDialog.this.setVisible(false);
			}
			
			public void windowClosed(WindowEvent e) {
			}
			
			public void windowActivated(WindowEvent e) {
			}
		});
	}
	
	public void open() {
		if(UrmusicController.getCurrentSong() == null) {
			JOptionPane.showMessageDialog(this, UrmusicStrings.getString("dialog.export.noAudioMessage"), UrmusicStrings.getString("dialog.export.title"), JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		this.settings.reset();
		this.cancelBtn.setEnabled(false);
		this.startBtn.setEnabled(true);
		
		this.videoEncoderCBox.setSelectedItem(this.settings.videoEncoder);
		this.audioEncoderCBox.setSelectedItem(this.settings.audioEncoder);
		this.muxerCBox.setSelectedItem(this.settings.muxer);
		
		this.setVisible(true);
	}
	
	private boolean checkDestination() {
		Path p = this.destField.getPath();
		
		if(p == null || Files.isDirectory(p)) {
			JOptionPane.showMessageDialog(this,
				UrmusicStrings.getString("dialog.export.invalidDestFileMessage"),
				UrmusicStrings.getString("dialog.export.title"),
				JOptionPane.ERROR_MESSAGE
			);
			return false;
		}
		
		if(Files.exists(p)) {
			int choice = JOptionPane.showConfirmDialog(this,
				UrmusicStrings.getString("dialog.export.overwriteDestFileMessage"),
				UrmusicStrings.getString("dialog.export.title"),
				JOptionPane.YES_NO_OPTION
			);
			
			if(choice != JOptionPane.YES_OPTION) return false;
		}
		
		return true;
	}
	
	private boolean checkEncoders() {
		if(this.videoEncoderCBox.getSelectedIndex() < 0) {
			JOptionPane.showMessageDialog(this,
				UrmusicStrings.getString("dialog.export.invalidVideoEncoder"),
				UrmusicStrings.getString("dialog.export.title"),
				JOptionPane.ERROR_MESSAGE
			);
			return false;
		}
		
		if(this.audioEncoderCBox.getSelectedIndex() < 0) {
			JOptionPane.showMessageDialog(this,
				UrmusicStrings.getString("dialog.export.invalidAudioEncoder"),
				UrmusicStrings.getString("dialog.export.title"),
				JOptionPane.ERROR_MESSAGE
			);
			return false;
		}
		
		return true;
	}
	
	private void start() {
		// Check settings
		if(!this.checkDestination()) return;
		if(!this.checkEncoders()) return;
		
		this.settings.destination = this.destField.getPath();
		this.settings.videoEncoder = VIDEO_ENCODERS[this.videoEncoderCBox.getSelectedIndex()];
		
		this.job = UrmusicController.export(this.settings, new ExportProgressCallback() {
			public void exportBegin() {
				SwingUtilities.invokeLater(() -> {
					UrmusicExportingDialog.this.cancelBtn.setEnabled(true);
					UrmusicExportingDialog.this.startBtn.setEnabled(false);
					UrmusicExportingDialog.this.pBar.setIndeterminate(true);
				});
			}
			
			public void renderBegin() {
				SwingUtilities.invokeLater(() -> {
					UrmusicExportingDialog.this.pBar.setIndeterminate(false);
					UrmusicExportingDialog.this.pBar.setValue(0);
				});
				
			}
			
			public void renderProgress(float progress) {
				SwingUtilities.invokeLater(() -> {
					UrmusicExportingDialog.this.pBar.setValue((int) (progress * UrmusicExportingDialog.this.pBar.getMaximum()));
				});
			}
			
			public void renderDone() {
				SwingUtilities.invokeLater(() -> {
					UrmusicExportingDialog.this.pBar.setIndeterminate(true);
				});
			}
			
			public void exportDone() {
				SwingUtilities.invokeLater(() -> {
					UrmusicExportingDialog.this.pBar.setIndeterminate(false);
					UrmusicExportingDialog.this.pBar.setValue(0);
					
					UrmusicExportingDialog.this.cancelBtn.setEnabled(false);
					UrmusicExportingDialog.this.startBtn.setEnabled(true);
					
					JOptionPane.showMessageDialog(UrmusicExportingDialog.this, UrmusicStrings.getString("dialog.export.finishedMessage"), UrmusicStrings.getString("dialog.export.title"), JOptionPane.INFORMATION_MESSAGE);
				});
			}

			public void exportException(Exception e) {
				SwingUtilities.invokeLater(() -> {
					UrmusicExportingDialog.this.cancelBtn.setEnabled(false);
					UrmusicExportingDialog.this.startBtn.setEnabled(true);
					
					JOptionPane.showMessageDialog(UrmusicExportingDialog.this, e.getMessage(), UrmusicStrings.getString("dialog.export.title"), JOptionPane.ERROR_MESSAGE);
				});
			}
		});
	}
	
	private boolean promptCancel() {
		return JOptionPane.showConfirmDialog(this,
			UrmusicStrings.getString("dialog.export.promptCancel"),
			UrmusicStrings.getString("dialog.export.title"),
			JOptionPane.YES_NO_OPTION
		) == JOptionPane.YES_OPTION;
	}
	
	public void cancel() {
		if(this.job != null && !this.job.isCancelled() && this.promptCancel())
			UrmusicController.cancelExport(this.job);
	}
}
