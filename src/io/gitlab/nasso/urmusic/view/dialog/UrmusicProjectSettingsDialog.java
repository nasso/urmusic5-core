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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import io.gitlab.nasso.urmusic.common.MutableRGBA32;
import io.gitlab.nasso.urmusic.controller.UrmusicController;
import io.gitlab.nasso.urmusic.model.UrmusicModel;
import io.gitlab.nasso.urmusic.view.UrmusicView;
import io.gitlab.nasso.urmusic.view.components.UrmColorButton;
import io.gitlab.nasso.urmusic.view.components.UrmEditableDurationPane;
import io.gitlab.nasso.urmusic.view.components.UrmEditableIntegerField;
import io.gitlab.nasso.urmusic.view.components.UrmEditableNumberField;
import io.gitlab.nasso.urmusic.view.data.UrmusicStrings;
import io.gitlab.nasso.urmusic.view.layout.VListLayout;

public class UrmusicProjectSettingsDialog extends JDialog {
	private UrmEditableIntegerField frameWidthField;
	private UrmEditableIntegerField frameHeightField;
	private UrmEditableNumberField framerateField;
	private UrmEditableDurationPane durationPane;
	private UrmColorButton bgColorBtn;
	
	public UrmusicProjectSettingsDialog() {
		JPanel labelsPane = new JPanel(new VListLayout(8));
		JPanel fieldsPane = new JPanel(new VListLayout(8));
		
		JLabel lbl;
		
		lbl = new JLabel(UrmusicStrings.getString("dialog.projectSettings.frameWidth") + ":", SwingConstants.RIGHT);
		lbl.setVerticalAlignment(SwingConstants.CENTER);
		lbl.setLabelFor(this.frameWidthField = new UrmEditableIntegerField(false, (o) -> { if(o.getValue().intValue() < 0) o.setValue(0); }));
		lbl.setPreferredSize(new Dimension(lbl.getPreferredSize().width, this.frameWidthField.getPreferredSize().height));
		fieldsPane.add(new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0)) {
			{
				this.add(UrmusicProjectSettingsDialog.this.frameWidthField);
				
				JLabel unitLbl = new JLabel("px");
				unitLbl.setLabelFor(UrmusicProjectSettingsDialog.this.frameWidthField);
				unitLbl.setForeground(new Color(0, 0, 0, 128));
				this.add(unitLbl);
			}
		});
		labelsPane.add(lbl);
		
		lbl = new JLabel(UrmusicStrings.getString("dialog.projectSettings.frameHeight") + ":", SwingConstants.RIGHT);
		lbl.setVerticalAlignment(SwingConstants.CENTER);
		lbl.setLabelFor(this.frameHeightField = new UrmEditableIntegerField(false, (o) -> { if(o.getValue().intValue() < 0) o.setValue(0); }));
		lbl.setPreferredSize(new Dimension(lbl.getPreferredSize().width, this.frameHeightField.getPreferredSize().height));
		fieldsPane.add(new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0)) {
			{
				this.add(UrmusicProjectSettingsDialog.this.frameHeightField);
				
				JLabel unitLbl = new JLabel("px");
				unitLbl.setLabelFor(UrmusicProjectSettingsDialog.this.frameHeightField);
				unitLbl.setForeground(new Color(0, 0, 0, 128));
				this.add(unitLbl);
			}
		});
		labelsPane.add(lbl);
		
		lbl = new JLabel(UrmusicStrings.getString("dialog.projectSettings.framerate") + ":", SwingConstants.RIGHT);
		lbl.setVerticalAlignment(SwingConstants.CENTER);
		lbl.setLabelFor(this.framerateField = new UrmEditableNumberField(false, (o) -> { if(o.getValue().floatValue() < 1.0f) o.setValue(1.0f); }));
		lbl.setPreferredSize(new Dimension(lbl.getPreferredSize().width, this.framerateField.getPreferredSize().height));
		fieldsPane.add(new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0)) {
			{
				this.add(UrmusicProjectSettingsDialog.this.framerateField);
				
				JLabel unitLbl = new JLabel("FPS");
				unitLbl.setLabelFor(UrmusicProjectSettingsDialog.this.framerateField);
				unitLbl.setForeground(new Color(0, 0, 0, 128));
				this.add(unitLbl);
			}
		});
		labelsPane.add(lbl);
		
		lbl = new JLabel(UrmusicStrings.getString("dialog.projectSettings.duration") + ":", SwingConstants.RIGHT);
		lbl.setVerticalAlignment(SwingConstants.CENTER);
		lbl.setLabelFor(this.durationPane = new UrmEditableDurationPane());
		lbl.setPreferredSize(new Dimension(lbl.getPreferredSize().width, this.durationPane.getPreferredSize().height));
		fieldsPane.add(UrmusicProjectSettingsDialog.this.durationPane);
		labelsPane.add(lbl);
		
		lbl = new JLabel(UrmusicStrings.getString("dialog.projectSettings.background") + ":", SwingConstants.RIGHT);
		lbl.setVerticalAlignment(SwingConstants.CENTER);
		lbl.setLabelFor(this.bgColorBtn = new UrmColorButton());
		lbl.setPreferredSize(new Dimension(lbl.getPreferredSize().width, this.bgColorBtn.getPreferredSize().height));
		fieldsPane.add(new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0)) {
			{
				this.add(UrmusicProjectSettingsDialog.this.bgColorBtn);
			}
		});
		labelsPane.add(lbl);
		
		JPanel optionPane = new JPanel(new GridLayout(0, 2, 12, 16));
		optionPane.setBorder(BorderFactory.createEmptyBorder(4, 16, 4, 16));
		optionPane.add(labelsPane);
		optionPane.add(fieldsPane);
		
		JPanel actionPane = new JPanel();
		actionPane.setLayout(new BoxLayout(actionPane, BoxLayout.X_AXIS));
		actionPane.add(new JButton(new AbstractAction(UrmusicStrings.getString("dialog.global.cancel")) {
			public void actionPerformed(ActionEvent e) {
				UrmusicProjectSettingsDialog.this.close();
			}
		}));
		actionPane.add(Box.createHorizontalGlue());
		actionPane.add(new JButton(new AbstractAction(UrmusicStrings.getString("dialog.global.ok")) {
			public void actionPerformed(ActionEvent e) {
				UrmusicProjectSettingsDialog.this.applyAndClose();
			}
		}));
		actionPane.add(Box.createHorizontalStrut(4));
		actionPane.add(new JButton(new AbstractAction(UrmusicStrings.getString("dialog.global.apply")) {
			public void actionPerformed(ActionEvent e) {
				UrmusicProjectSettingsDialog.this.apply();
			}
		}));
		
		JPanel container = new JPanel(new BorderLayout(4, 4));
		container.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		container.add(optionPane, BorderLayout.CENTER);
		container.add(actionPane, BorderLayout.SOUTH);
		
		this.setTitle(UrmusicStrings.getString("dialog.projectSettings.title"));
		this.setContentPane(container);
		this.addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent e) {
				UrmusicView.blockKeyEvent();
			}
			
			public void windowClosing(WindowEvent e) {
				UrmusicView.freeKeyEvent();
			}
		});
		this.setSize(340, 240);
		this.setLocationRelativeTo(null);
	}
	
	public void open() {
		this.frameWidthField.setValue(UrmusicModel.getCurrentProject().getMainComposition().getWidth());
		this.frameHeightField.setValue(UrmusicModel.getCurrentProject().getMainComposition().getHeight());
		this.framerateField.setValue(UrmusicModel.getCurrentProject().getMainComposition().getTimeline().getFramerate());
		this.durationPane.setSeconds(UrmusicModel.getCurrentProject().getMainComposition().getTimeline().getDuration());
		this.bgColorBtn.setColor(UrmusicModel.getCurrentProject().getMainComposition().getClearColor());
		
		this.setVisible(true);
		UrmusicView.blockKeyEvent();
	}
	
	private void close() {
		this.setVisible(false);
		UrmusicView.freeKeyEvent();
	}
	
	private MutableRGBA32 _rgba = new MutableRGBA32();
	private void apply() {
		this._rgba.setRGBA(
				this.bgColorBtn.getColor().getRed(),
				this.bgColorBtn.getColor().getGreen(),
				this.bgColorBtn.getColor().getBlue(),
				this.bgColorBtn.getColor().getAlpha());
		
		UrmusicController.updateProjectSettings(
				this.frameWidthField.getValue().intValue(),
				this.frameHeightField.getValue().intValue(),
				this.framerateField.getValue().floatValue(),
				this.durationPane.getSeconds(),
				this._rgba);
	}
	
	private void applyAndClose() {
		this.apply();
		this.close();
	}
}
