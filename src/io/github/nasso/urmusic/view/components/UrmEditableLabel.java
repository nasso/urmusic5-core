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

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import io.github.nasso.urmusic.view.UrmusicView;

public class UrmEditableLabel extends JPanel  {
	private static final String CARD_LABEL = "label";
	private static final String CARD_FIELD = "field";
	
	private CardLayout card;
	private JLabel valueLabel;
	private JTextField valueField;
	
	private String lastValue;
	
	private boolean editing = false;
	
	private Consumer<UrmEditableLabel> onValueChange;

	public UrmEditableLabel(Consumer<UrmEditableLabel> onValueChange) {
		this.onValueChange = onValueChange;
		
		this.valueLabel = new JLabel();
		this.valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
		this.valueLabel.setOpaque(false);
		this.valueLabel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK),
			BorderFactory.createEmptyBorder(0, 2, 0, 2)
		));
		this.valueLabel.addMouseListener(new MouseAdapter() {
		    public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2)
					UrmEditableLabel.this.startTextEdit();
		     }
		});
		
		this.valueField = new JTextField();
		this.valueField.setFont(this.valueLabel.getFont());
		this.valueField.setBorder(this.valueLabel.getBorder());
		this.valueField.setOpaque(false);
		this.valueField.setHorizontalAlignment(SwingConstants.CENTER);
		this.valueField.addFocusListener(new FocusListener() {
			private boolean waitingToGetItBack = false;
			
			public void focusLost(FocusEvent e) {
				if(!e.isTemporary())
					SwingUtilities.invokeLater(UrmEditableLabel.this::validateTextEdit);
				else
					this.waitingToGetItBack = true;
			}
			
			public void focusGained(FocusEvent e) {
				if(this.waitingToGetItBack) {
					this.waitingToGetItBack = false;
					return;
				}
				
				SwingUtilities.invokeLater(() -> {
					UrmEditableLabel.this.valueField.setText(UrmEditableLabel.this.valueLabel.getText());
					UrmEditableLabel.this.valueField.selectAll();
					
					// Set text to empty string to let the field reduce the size
					UrmEditableLabel.this.valueLabel.setText(null);
				});
			}
		});
		this.valueField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode()) {
					case KeyEvent.VK_ESCAPE:
						UrmEditableLabel.this.cancelTextEdit();
						break;
					case KeyEvent.VK_ENTER:
						UrmEditableLabel.this.validateTextEdit();
						break;
				}
			}
		});
		this.valueField.getDocument().addDocumentListener(new DocumentListener() {
			public void removeUpdate(DocumentEvent e) {
				UrmEditableLabel.this.revalidate();
			}
			
			public void insertUpdate(DocumentEvent e) {
				UrmEditableLabel.this.revalidate();
			}
			
			public void changedUpdate(DocumentEvent e) {
				UrmEditableLabel.this.revalidate();
			}
		});
		
		this.setLayout(this.card = new CardLayout());
		this.setOpaque(false);
		this.add(this.valueLabel, CARD_LABEL);
		this.add(this.valueField, CARD_FIELD);
		this.card.show(this, CARD_LABEL);
	}
	
	private void startTextEdit() {
		if(this.editing) return;
		this.editing = true;
		
		UrmusicView.blockKeyEvent();

		this.card.show(this, CARD_FIELD);

		this.valueField.requestFocusInWindow();
	}
	
	private void validateTextEdit() {
		if(!this.editing) return;
		this.editing = false;
		
		UrmusicView.freeKeyEvent();

		this.setValue(this.valueField.getText());
		if(this.onValueChange != null) this.onValueChange.accept(this);

		this.card.show(this, CARD_LABEL);
	}
	
	private void cancelTextEdit() {
		if(!this.editing) return;
		this.editing = false;
		
		UrmusicView.freeKeyEvent();
		
		this.valueLabel.setText(this.lastValue);
		
		this.card.show(this, CARD_LABEL);
	}
	
	public Consumer<UrmEditableLabel> getOnValueChange() {
		return this.onValueChange;
	}

	public void setOnValueChange(Consumer<UrmEditableLabel> onValueChange) {
		this.onValueChange = onValueChange;
	}

	public void setValue(String val) {
		this.lastValue = val;
		
		this.valueLabel.setText(val);
		this.valueField.setText(this.valueLabel.getText());
	}
	
	public String getValue() {
		return this.lastValue;
	}
}
