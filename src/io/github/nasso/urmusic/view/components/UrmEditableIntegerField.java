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
import java.awt.Cursor;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.NumberFormatter;

import io.github.nasso.urmusic.common.MathUtils;
import io.github.nasso.urmusic.view.UrmusicView;

public class UrmEditableIntegerField extends JPanel {
	private static final String CARD_LABEL = "label";
	private static final String CARD_FIELD = "field";
	
	private boolean blockKeyEvents;
	
	private CardLayout card;
	private JLabel valueLabel;
	private JFormattedTextField valueField;
	
	private Integer lastValue = null;
	private int step = 1;
	
	private String minDigitString = null;
	
	private boolean editing = false;
	
	private Consumer<UrmEditableIntegerField> onValueChange;

	private class ValueLabelMouseController implements MouseListener, MouseMotionListener {
		private boolean button1 = false;
		private int pressedX;
		
		public void mousePressed(MouseEvent e) {
			if(!UrmEditableIntegerField.this.valueLabel.isEnabled()) return;
			
			if(	!this.button1 && 
				e.getButton() == MouseEvent.BUTTON1 &&
				MathUtils.boxContains(e.getX(), e.getY(), 0, 0, UrmEditableIntegerField.this.valueLabel.getWidth(), UrmEditableIntegerField.this.valueLabel.getHeight())
					) {
				this.button1 = true;
				this.pressedX = e.getXOnScreen();
			}
		}
		
		public void mouseReleased(MouseEvent e) {
			if(!UrmEditableIntegerField.this.valueLabel.isEnabled()) return;
			
			this.button1 &= e.getButton() != MouseEvent.BUTTON1;
		}
		
		public void mouseDragged(MouseEvent e) {
			if(!UrmEditableIntegerField.this.valueLabel.isEnabled()) return;
			
			if(this.button1) {
				UrmEditableIntegerField.this.setValue(UrmEditableIntegerField.this.lastValue + (e.getXOnScreen() - this.pressedX) * UrmEditableIntegerField.this.getStep());
				if(UrmEditableIntegerField.this.onValueChange != null) UrmEditableIntegerField.this.onValueChange.accept(UrmEditableIntegerField.this);
				
				this.pressedX = e.getXOnScreen();
			}
		}
		
		public void mouseClicked(MouseEvent e) {
			if(!UrmEditableIntegerField.this.valueLabel.isEnabled()) return;
			
			if(e.getButton() == MouseEvent.BUTTON1)
				UrmEditableIntegerField.this.startTextEdit();
		}

		public void mouseMoved(MouseEvent e) {
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}
	}
	
	public UrmEditableIntegerField() {
		this(false);
	}
	
	public UrmEditableIntegerField(boolean blockKeyEvents) {
		this(blockKeyEvents, null);
	}

	public UrmEditableIntegerField(boolean blockKeyEvents, Consumer<UrmEditableIntegerField> onValueChange) {
		this(blockKeyEvents, onValueChange, 0);
	}
	
	public UrmEditableIntegerField(boolean blockKeyEvents, Consumer<UrmEditableIntegerField> onValueChange, int minDigitCount) {
		this.blockKeyEvents = blockKeyEvents;
		this.onValueChange = onValueChange;
		
		char[] digits = new char[minDigitCount < 1 ? 1 : minDigitCount];
		Arrays.fill(digits, '0');
		this.minDigitString = new String(digits);
		
		this.valueLabel = new JLabel();
		this.valueLabel.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		this.valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
		this.valueLabel.setOpaque(false);
		this.valueLabel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK),
			BorderFactory.createEmptyBorder(0, 2, 0, 2)
		));
		
		ValueLabelMouseController mouseControl = new ValueLabelMouseController();
		this.valueLabel.addMouseMotionListener(mouseControl);
		this.valueLabel.addMouseListener(mouseControl);
		
		NumberFormat format = NumberFormat.getIntegerInstance(UrmusicView.getLocale());
		NumberFormatter formatter = new NumberFormatter(format);
		formatter.setValueClass(Float.class);
		formatter.setAllowsInvalid(true);
		formatter.setCommitsOnValidEdit(true);
		
		this.valueField = new JFormattedTextField(formatter);
		this.valueField.setFont(this.valueLabel.getFont());
		this.valueField.setBorder(this.valueLabel.getBorder());
		this.valueField.setOpaque(false);
		this.valueField.setHorizontalAlignment(SwingConstants.CENTER);
		this.valueField.addFocusListener(new FocusListener() {
			private boolean waitingToGetItBack = false;
			
			public void focusLost(FocusEvent e) {
				if(!e.isTemporary())
					SwingUtilities.invokeLater(UrmEditableIntegerField.this::validateTextEdit);
				else
					this.waitingToGetItBack = true;
			}
			
			public void focusGained(FocusEvent e) {
				if(this.waitingToGetItBack) {
					this.waitingToGetItBack = false;
					return;
				}
				
				SwingUtilities.invokeLater(() -> {
					UrmEditableIntegerField.this.valueField.setText(UrmEditableIntegerField.this.valueLabel.getText());
					UrmEditableIntegerField.this.valueField.selectAll();
					
					// Set text to empty string to let the field reduce the size
					UrmEditableIntegerField.this.valueLabel.setText(null);
				});
			}
		});
		this.valueField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode()) {
					case KeyEvent.VK_ESCAPE:
						UrmEditableIntegerField.this.cancelTextEdit();
						break;
					case KeyEvent.VK_ENTER:
						UrmEditableIntegerField.this.validateTextEdit();
						break;
				}
			}
		});
		this.valueField.getDocument().addDocumentListener(new DocumentListener() {
			public void removeUpdate(DocumentEvent e) {
				UrmEditableIntegerField.this.revalidate();
			}
			
			public void insertUpdate(DocumentEvent e) {
				UrmEditableIntegerField.this.revalidate();
			}
			
			public void changedUpdate(DocumentEvent e) {
				UrmEditableIntegerField.this.revalidate();
			}
		});
		
		this.addPropertyChangeListener("enabled", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				UrmEditableIntegerField that = UrmEditableIntegerField.this;
				that.valueLabel.setEnabled(that.isEnabled());
				that.valueField.setEnabled(that.isEnabled());
				
				if(!that.isEnabled())
					that.cancelTextEdit();
			}
		});
		
		this.setLayout(this.card = new CardLayout());
		this.setOpaque(false);
		this.add(this.valueLabel, UrmEditableIntegerField.CARD_LABEL);
		this.add(this.valueField, UrmEditableIntegerField.CARD_FIELD);
		this.card.show(this, UrmEditableIntegerField.CARD_LABEL);
	}
	
	private void startTextEdit() {
		if(this.editing) return;
		this.editing = true;
		
		if(this.blockKeyEvents) UrmusicView.blockKeyEvent();

		this.card.show(this, UrmEditableIntegerField.CARD_FIELD);
		
		this.valueField.requestFocusInWindow();
	}
	
	private void validateTextEdit() {
		if(!this.editing) return;
		this.editing = false;
		
		if(this.blockKeyEvents) UrmusicView.freeKeyEvent();

		this.setValue((Number) this.valueField.getValue());
		if(this.onValueChange != null) this.onValueChange.accept(this);

		this.card.show(this, UrmEditableIntegerField.CARD_LABEL);
	}
	
	private void cancelTextEdit() {
		if(!this.editing) return;
		this.editing = false;
		
		if(this.blockKeyEvents) UrmusicView.freeKeyEvent();
		
		this.valueLabel.setText(this.stringifyDigits(this.lastValue));

		this.card.show(this, UrmEditableIntegerField.CARD_LABEL);
	}
	
	public Consumer<UrmEditableIntegerField> getOnValueChange() {
		return this.onValueChange;
	}

	public void setOnValueChange(Consumer<UrmEditableIntegerField> onValueChange) {
		this.onValueChange = onValueChange;
	}

	public void setValue(Number val) {
		this.lastValue = val.intValue();
		
		this.valueLabel.setText(this.stringifyDigits(this.lastValue));
		this.valueField.setText(this.valueLabel.getText());
	}
	
	public Number getValue() {
		return this.lastValue;
	}

	public int getStep() {
		return this.step;
	}

	public void setStep(int step) {
		this.step = step;
	}
	
	private String stringifyDigits(Integer i) {
		String str = String.valueOf(i);
		return str.length() < this.minDigitString.length() ? this.minDigitString.substring(0, this.minDigitString.length() - str.length()) + str : str;
	}
}
