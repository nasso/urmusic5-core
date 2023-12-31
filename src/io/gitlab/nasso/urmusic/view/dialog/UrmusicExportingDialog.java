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
package io.gitlab.nasso.urmusic.view.dialog;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.text.DefaultCaret;

import io.gitlab.nasso.urmusic.common.event.ExportJobCallback;
import io.gitlab.nasso.urmusic.controller.UrmusicController;
import io.gitlab.nasso.urmusic.model.exporter.ExportSettings;
import io.gitlab.nasso.urmusic.model.exporter.Exporter.ExportJob;
import io.gitlab.nasso.urmusic.model.ffmpeg.Encoder;
import io.gitlab.nasso.urmusic.model.ffmpeg.FFmpeg;
import io.gitlab.nasso.urmusic.model.ffmpeg.Muxer;
import io.gitlab.nasso.urmusic.view.UrmusicView;
import io.gitlab.nasso.urmusic.view.components.UrmEditableIntegerField;
import io.gitlab.nasso.urmusic.view.components.UrmPathField;
import io.gitlab.nasso.urmusic.view.data.UrmusicStrings;
import io.gitlab.nasso.urmusic.view.layout.VListLayout;

public class UrmusicExportingDialog extends JDialog implements ExportJobCallback {
	private static final String CARD_BITRATE_VARIABLE = "variable";
	private static final String CARD_BITRATE_CONSTANT = "constant";
	
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
	private ExportJob job;
	
	private JTabbedPane tabbedPane;
	
	// Settings
	private UrmPathField destField;
	private JComboBox<Muxer> muxerCBox;
	
	private JComboBox<Encoder> videoEncoderCBox;
	private JComboBox<Encoder> audioEncoderCBox;
	
	private JSlider videoQualitySlider;
	private JSlider audioQualitySlider;
	
	private JCheckBox videoUseVariableBitrate;
	private JCheckBox audioUseVariableBitrate;
	
	private CardLayout videoBitrateCards;
	private CardLayout audioBitrateCards;
	
	private JPanel videoBitrateCardsPane;
	private JPanel audioBitrateCardsPane;
	
	private UrmEditableIntegerField videoBitrate;
	private UrmEditableIntegerField audioBitrate;
	
	private JButton cancelBtn, startBtn;
	private JProgressBar pBar;
	
	// Console
	private JTextArea consoleTextArea;
	
	public UrmusicExportingDialog() {
		JLabel lbl;
		Dictionary<Integer, JLabel> qualityLabelTable = new Hashtable<Integer, JLabel>() {
			{
				JLabel lbl;
				Color foreground = new Color(0, 0, 0, 128);
				
				this.put(ExportSettings.QSCALE_MIN, lbl = new JLabel(UrmusicStrings.getString("dialog.export.bitrate.variable.quality"), SwingConstants.LEFT));
				lbl.setForeground(foreground);
				lbl.setFont(lbl.getFont().deriveFont(10f));
				
				this.put((ExportSettings.QSCALE_MIN + ExportSettings.QSCALE_MAX) / 2, lbl = new JLabel(UrmusicStrings.getString("dialog.export.bitrate.variable.balanced"), SwingConstants.CENTER));
				lbl.setForeground(foreground);
				lbl.setFont(lbl.getFont().deriveFont(10f));
				
				this.put(ExportSettings.QSCALE_MAX, lbl = new JLabel(UrmusicStrings.getString("dialog.export.bitrate.variable.smallSize"), SwingConstants.RIGHT));
				lbl.setForeground(foreground);
				lbl.setFont(lbl.getFont().deriveFont(10f));
			}
		};
		
		JPanel topBar = new JPanel(new GridLayout(1, 2, 8, 8));
		topBar.add(this.destField = new UrmPathField(false, false));
		topBar.add(this.muxerCBox = new JComboBox<>(MUXERS));

		JPanel videoFieldsPane = new JPanel(new VListLayout(8));
		JPanel audioFieldsPane = new JPanel(new VListLayout(8));
		JPanel videoLabelsPane = new JPanel(new VListLayout(8));
		JPanel audioLabelsPane = new JPanel(new VListLayout(8));
		
		// Encoder
		lbl = new JLabel(UrmusicStrings.getString("dialog.export.encoder") + ":", SwingConstants.RIGHT);
		lbl.setVerticalAlignment(SwingConstants.CENTER);
		lbl.setLabelFor(this.videoEncoderCBox = new JComboBox<>(VIDEO_ENCODERS));
		lbl.setPreferredSize(new Dimension(lbl.getPreferredSize().width, this.videoEncoderCBox.getPreferredSize().height));
		videoFieldsPane.add(this.videoEncoderCBox);
		videoLabelsPane.add(lbl);
		
		lbl = new JLabel(UrmusicStrings.getString("dialog.export.encoder") + ":", SwingConstants.RIGHT);
		lbl.setLabelFor(this.audioEncoderCBox = new JComboBox<>(AUDIO_ENCODERS));
		lbl.setPreferredSize(new Dimension(lbl.getPreferredSize().width, this.audioEncoderCBox.getPreferredSize().height));
		audioFieldsPane.add(this.audioEncoderCBox);
		audioLabelsPane.add(lbl);

		this.videoBitrateCards = new CardLayout();
		this.videoBitrateCardsPane = new JPanel(this.videoBitrateCards);
		
		this.audioBitrateCards = new CardLayout();
		this.audioBitrateCardsPane = new JPanel(this.audioBitrateCards);
		
		// Bitrate mode (constant/variable)
		lbl = new JLabel("", SwingConstants.RIGHT);
		lbl.setVerticalAlignment(SwingConstants.CENTER);
		lbl.setLabelFor(this.videoUseVariableBitrate = new JCheckBox(new AbstractAction(UrmusicStrings.getString("dialog.export.variableBitrate")) {
			public void actionPerformed(ActionEvent e) {
				boolean enabled = UrmusicExportingDialog.this.videoUseVariableBitrate.isSelected();
				
				UrmusicExportingDialog.this.videoBitrateCards.show(UrmusicExportingDialog.this.videoBitrateCardsPane, enabled ? CARD_BITRATE_VARIABLE : CARD_BITRATE_CONSTANT);
			}
		}));
		lbl.setPreferredSize(new Dimension(lbl.getPreferredSize().width, this.videoUseVariableBitrate.getPreferredSize().height));
		videoFieldsPane.add(this.videoUseVariableBitrate);
		videoLabelsPane.add(lbl);
		
		lbl = new JLabel("", SwingConstants.RIGHT);
		lbl.setVerticalAlignment(SwingConstants.CENTER);
		lbl.setLabelFor(this.audioUseVariableBitrate = new JCheckBox(new AbstractAction(UrmusicStrings.getString("dialog.export.variableBitrate")) {
			public void actionPerformed(ActionEvent e) {
				boolean enabled = UrmusicExportingDialog.this.audioUseVariableBitrate.isSelected();
				
				UrmusicExportingDialog.this.audioBitrateCards.show(UrmusicExportingDialog.this.audioBitrateCardsPane, enabled ? CARD_BITRATE_VARIABLE : CARD_BITRATE_CONSTANT);
			}
		}));
		lbl.setPreferredSize(new Dimension(lbl.getPreferredSize().width, this.audioUseVariableBitrate.getPreferredSize().height));
		audioFieldsPane.add(this.audioUseVariableBitrate);
		audioLabelsPane.add(lbl);
		
		// Bitrate
		this.videoBitrateCardsPane.add(new JPanel() {
			{
				// Using Box Layout to center vertically
				BoxLayout bl = new BoxLayout(this, BoxLayout.X_AXIS);
				this.setLayout(bl);
				
				JPanel container = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
				
				JLabel unitLbl = new JLabel("kb/s");
				unitLbl.setForeground(new Color(0, 0, 0, 128));
				
				container.add(UrmusicExportingDialog.this.videoBitrate = new UrmEditableIntegerField());
				container.add(unitLbl);
				
				this.add(container);
			}
		}, CARD_BITRATE_CONSTANT);
		this.videoBitrateCardsPane.add(this.videoQualitySlider = new JSlider(ExportSettings.QSCALE_MIN, ExportSettings.QSCALE_MAX, 23), CARD_BITRATE_VARIABLE);
		this.videoQualitySlider.setLabelTable(qualityLabelTable);
		this.videoQualitySlider.setMajorTickSpacing((ExportSettings.QSCALE_MIN + ExportSettings.QSCALE_MAX) / 2);
		this.videoQualitySlider.setMinorTickSpacing(1);
		this.videoQualitySlider.setPaintLabels(true);
		this.videoQualitySlider.setSnapToTicks(true);
		
		this.videoBitrateCards.show(this.videoBitrateCardsPane, CARD_BITRATE_CONSTANT);

		lbl = new JLabel(UrmusicStrings.getString("dialog.export.bitrate") + ":", SwingConstants.RIGHT);
		lbl.setVerticalAlignment(SwingConstants.CENTER);
		lbl.setPreferredSize(new Dimension(lbl.getPreferredSize().width, this.videoBitrateCardsPane.getPreferredSize().height));
		videoFieldsPane.add(this.videoBitrateCardsPane);
		videoLabelsPane.add(lbl);
		
		this.audioBitrateCardsPane.add(new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0)) {
			{
				BoxLayout bl = new BoxLayout(this, BoxLayout.X_AXIS);
				this.setLayout(bl);
				
				JPanel container = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
				
				JLabel unitLbl = new JLabel("kb/s");
				unitLbl.setForeground(new Color(0, 0, 0, 128));
				
				container.add(UrmusicExportingDialog.this.audioBitrate = new UrmEditableIntegerField());
				container.add(unitLbl);
				
				this.add(container);
			}
		}, CARD_BITRATE_CONSTANT);
		this.audioBitrateCardsPane.add(this.audioQualitySlider = new JSlider(ExportSettings.QSCALE_MIN, ExportSettings.QSCALE_MAX, 23), CARD_BITRATE_VARIABLE);
		this.audioQualitySlider.setLabelTable(qualityLabelTable);
		this.audioQualitySlider.setMajorTickSpacing((ExportSettings.QSCALE_MIN + ExportSettings.QSCALE_MAX) / 2);
		this.audioQualitySlider.setMinorTickSpacing(1);
		this.audioQualitySlider.setPaintLabels(true);
		this.audioQualitySlider.setSnapToTicks(true);
		
		this.audioBitrateCards.show(this.audioBitrateCardsPane, CARD_BITRATE_CONSTANT);

		lbl = new JLabel(UrmusicStrings.getString("dialog.export.bitrate") + ":", SwingConstants.RIGHT);
		lbl.setVerticalAlignment(SwingConstants.CENTER);
		lbl.setPreferredSize(new Dimension(lbl.getPreferredSize().width, this.audioBitrateCardsPane.getPreferredSize().height));
		audioFieldsPane.add(this.audioBitrateCardsPane);
		audioLabelsPane.add(lbl);
		
		// Panels
		JPanel videoFormatPane = new JPanel(new BorderLayout(12, 0));
		videoFormatPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(UrmusicStrings.getString("dialog.export.video.title")), BorderFactory.createEmptyBorder(4, 16, 4, 16)));
		videoFormatPane.add(videoLabelsPane, BorderLayout.WEST);
		videoFormatPane.add(videoFieldsPane, BorderLayout.CENTER);
		
		JPanel audioFormatPane = new JPanel(new BorderLayout(12, 0));
		audioFormatPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(UrmusicStrings.getString("dialog.export.audio.title")), BorderFactory.createEmptyBorder(4, 16, 4, 16)));
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
		
		JPanel settingsPane = new JPanel(new BorderLayout(8, 8));
		settingsPane.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		settingsPane.add(topBar, BorderLayout.NORTH);
		settingsPane.add(settingsContainer, BorderLayout.CENTER);
		settingsPane.add(bottomBar, BorderLayout.SOUTH);
		
		JPanel consolePane = new JPanel(new BorderLayout(8, 8));
		consolePane.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		consolePane.add(new JScrollPane(this.consoleTextArea = new JTextArea(), ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
		((DefaultCaret) this.consoleTextArea.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		this.consoleTextArea.setLineWrap(true);
		this.consoleTextArea.setWrapStyleWord(true);
		this.consoleTextArea.setEditable(false);
		this.consoleTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		
		this.tabbedPane = new JTabbedPane();
		this.tabbedPane.add(UrmusicStrings.getString("dialog.export.tab.settings"), settingsPane);
		this.tabbedPane.add(UrmusicStrings.getString("dialog.export.tab.console"), consolePane);
		
		this.setTitle(UrmusicStrings.getString("dialog.export.title"));
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.setModalityType(ModalityType.APPLICATION_MODAL);
		this.setContentPane(this.tabbedPane);
		this.setSize(800, 360);
		this.setLocationRelativeTo(null);
		
		this.addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent e) {
				UrmusicView.blockKeyEvent();
			}
			
			public void windowClosing(WindowEvent e) {
				if(UrmusicExportingDialog.this.job != null && !UrmusicExportingDialog.this.job.isDone() && !UrmusicExportingDialog.this.job.isCancelled()) {
					if(!UrmusicExportingDialog.this.promptCancel()) return;
					UrmusicController.cancelExport(UrmusicExportingDialog.this.job);
				}
				
				UrmusicView.freeKeyEvent();
				UrmusicExportingDialog.this.setVisible(false);
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
		
		this.videoQualitySlider.setValue(this.settings.vqscale);
		this.audioQualitySlider.setValue(this.settings.aqscale);
		
		this.videoUseVariableBitrate.setSelected(!this.settings.useConstantBitrateVideo);
		this.audioUseVariableBitrate.setSelected(!this.settings.useConstantBitrateAudio);
		
		this.videoBitrateCards.show(this.videoBitrateCardsPane, this.settings.useConstantBitrateVideo ? CARD_BITRATE_CONSTANT : CARD_BITRATE_VARIABLE);
		this.audioBitrateCards.show(this.audioBitrateCardsPane, this.settings.useConstantBitrateAudio ? CARD_BITRATE_CONSTANT : CARD_BITRATE_VARIABLE);
		
		this.videoBitrate.setValue(this.settings.bv);
		this.audioBitrate.setValue(this.settings.ba);
		
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
		
		this.settings.useConstantBitrateVideo = !this.videoUseVariableBitrate.isSelected();
		this.settings.useConstantBitrateAudio = !this.audioUseVariableBitrate.isSelected();
		
		this.settings.bv = this.videoBitrate.getValue().intValue();
		this.settings.ba = this.audioBitrate.getValue().intValue();
		
		this.settings.vqscale = this.videoQualitySlider.getValue();
		this.settings.aqscale = this.audioQualitySlider.getValue();
		
		this.job = UrmusicController.export(this.settings, this);
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
	

	public void exportBegin() {
		SwingUtilities.invokeLater(() -> {
			this.cancelBtn.setEnabled(true);
			this.startBtn.setEnabled(false);
			this.pBar.setIndeterminate(true);
		});
	}
	
	public void renderBegin() {
		SwingUtilities.invokeLater(() -> {
			this.pBar.setIndeterminate(false);
			this.pBar.setValue(0);
		});
		
	}
	
	public void renderProgress(float progress) {
		SwingUtilities.invokeLater(() -> {
			this.pBar.setValue((int) (progress * UrmusicExportingDialog.this.pBar.getMaximum()));
		});
	}
	
	public void renderDone() {
		SwingUtilities.invokeLater(() -> {
			UrmusicExportingDialog.this.pBar.setIndeterminate(true);
		});
	}
	
	public void exportDone() {
		SwingUtilities.invokeLater(() -> {
			this.pBar.setIndeterminate(false);
			this.pBar.setValue(0);
			
			this.cancelBtn.setEnabled(false);
			this.startBtn.setEnabled(true);
			
			JOptionPane.showMessageDialog(this, UrmusicStrings.getString("dialog.export.finishedMessage"), UrmusicStrings.getString("dialog.export.title"), JOptionPane.INFORMATION_MESSAGE);
		});
	}

	public void exportException(Exception e) {
		SwingUtilities.invokeLater(() -> {
			this.cancelBtn.setEnabled(false);
			this.startBtn.setEnabled(true);
			
			JOptionPane.showMessageDialog(this, e.getMessage(), UrmusicStrings.getString("dialog.export.title"), JOptionPane.ERROR_MESSAGE);
		});
	}

	public void exportStdout(String str) {
		SwingUtilities.invokeLater(() -> {
			this.consoleTextArea.append(str);
		});
	}

	public void exportStderr(String str) {
		this.exportStdout(str);
	}
}
