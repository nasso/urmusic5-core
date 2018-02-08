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
import java.text.NumberFormat;
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

public class UrmEditableNumberField extends JPanel {
	private static final long serialVersionUID = -63508914364230123L;
	private static final String CARD_LABEL = "label";
	private static final String CARD_FIELD = "field";
	
	private CardLayout card;
	private JLabel valueLabel;
	private JFormattedTextField valueField;
	
	private float lastValue = Float.NaN;
	private float step = 1.0f;
	
	private boolean editing = false;
	
	private Consumer<UrmEditableNumberField> onValueChange;

	private class ValueLabelMouseController implements MouseListener, MouseMotionListener {
		private boolean button1 = false;
		private int pressedX;
		
		public void mousePressed(MouseEvent e) {
			if(	!this.button1 && 
				e.getButton() == MouseEvent.BUTTON1 &&
				MathUtils.boxContains(e.getX(), e.getY(), 0, 0, UrmEditableNumberField.this.valueLabel.getWidth(), UrmEditableNumberField.this.valueLabel.getHeight())
					) {
				this.button1 = true;
				this.pressedX = e.getXOnScreen();
			}
		}
		
		public void mouseReleased(MouseEvent e) {
			this.button1 &= e.getButton() != MouseEvent.BUTTON1;
		}
		
		public void mouseDragged(MouseEvent e) {
			if(this.button1) {
				UrmEditableNumberField.this.setValue(UrmEditableNumberField.this.lastValue + (e.getXOnScreen() - this.pressedX) * UrmEditableNumberField.this.getStep());
				if(UrmEditableNumberField.this.onValueChange != null) UrmEditableNumberField.this.onValueChange.accept(UrmEditableNumberField.this);
				
				this.pressedX = e.getXOnScreen();
			}
		}
		
		public void mouseClicked(MouseEvent e) {
			if(e.getButton() == MouseEvent.BUTTON1)
				UrmEditableNumberField.this.startTextEdit();
		}

		public void mouseMoved(MouseEvent e) {
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}
	}
	
	public UrmEditableNumberField(Consumer<UrmEditableNumberField> onValueChange) {
		this.onValueChange = onValueChange;
		
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
		
		NumberFormat format = NumberFormat.getNumberInstance(UrmusicView.getLocale());
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
					SwingUtilities.invokeLater(UrmEditableNumberField.this::validateTextEdit);
				else
					this.waitingToGetItBack = true;
			}
			
			public void focusGained(FocusEvent e) {
				if(this.waitingToGetItBack) {
					this.waitingToGetItBack = false;
					return;
				}
				
				SwingUtilities.invokeLater(() -> {
					UrmEditableNumberField.this.valueField.setText(UrmEditableNumberField.this.valueLabel.getText());
					UrmEditableNumberField.this.valueField.selectAll();
					
					// Set text to empty string to let the field reduce the size
					UrmEditableNumberField.this.valueLabel.setText(null);
				});
			}
		});
		this.valueField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode()) {
					case KeyEvent.VK_ESCAPE:
						UrmEditableNumberField.this.cancelTextEdit();
						break;
					case KeyEvent.VK_ENTER:
						UrmEditableNumberField.this.validateTextEdit();
						break;
				}
			}
		});
		this.valueField.getDocument().addDocumentListener(new DocumentListener() {
			public void removeUpdate(DocumentEvent e) {
				UrmEditableNumberField.this.revalidate();
			}
			
			public void insertUpdate(DocumentEvent e) {
				UrmEditableNumberField.this.revalidate();
			}
			
			public void changedUpdate(DocumentEvent e) {
				UrmEditableNumberField.this.revalidate();
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

		this.setValue((Number) this.valueField.getValue());
		if(this.onValueChange != null) this.onValueChange.accept(this);

		this.card.show(this, CARD_LABEL);
	}
	
	private void cancelTextEdit() {
		if(!this.editing) return;
		this.editing = false;
		
		UrmusicView.freeKeyEvent();
		
		this.valueLabel.setText(String.valueOf(this.lastValue));

		this.card.show(this, CARD_LABEL);
	}
	
	public Consumer<UrmEditableNumberField> getOnValueChange() {
		return this.onValueChange;
	}

	public void setOnValueChange(Consumer<UrmEditableNumberField> onValueChange) {
		this.onValueChange = onValueChange;
	}

	public void setValue(Number val) {
		if(val != null && val.floatValue() == this.lastValue) return;
		
		this.lastValue = Math.round(val.floatValue() * 100.0f) / 100.0f;
		
		this.valueLabel.setText(String.valueOf(this.lastValue));
		this.valueField.setText(this.valueLabel.getText());
	}
	
	public Number getValue() {
		return this.lastValue;
	}

	public float getStep() {
		return this.step;
	}

	public void setStep(float step) {
		this.step = step;
	}
}
