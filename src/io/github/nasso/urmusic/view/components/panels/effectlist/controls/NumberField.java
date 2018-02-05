package io.github.nasso.urmusic.view.components.panels.effectlist.controls;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.NumberFormatter;

import io.github.nasso.urmusic.utils.MathUtils;
import io.github.nasso.urmusic.view.UrmusicView;

public class NumberField extends JPanel {
	private static final long serialVersionUID = -63508914364230123L;
	
	private JLabel valueLabel;
	private JFormattedTextField valueField;
	
	private Number lastValue;
	
	private boolean editing = false;
	
	private Consumer<NumberField> onValueChange;

	public NumberField(Consumer<NumberField> onValueChange) {
		this.onValueChange = onValueChange;
		
		this.valueLabel = new JLabel();
		this.valueLabel.setOpaque(false);
		this.valueLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
		this.valueLabel.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				if(MathUtils.boxContains(e.getX(), e.getY(), 0, 0, e.getComponent().getWidth(), e.getComponent().getHeight()))
					NumberField.this.startTextEdit();
			}
		});
		
		NumberFormat format = NumberFormat.getNumberInstance(UrmusicView.getLocale());
		NumberFormatter formatter = new NumberFormatter(format);
		formatter.setValueClass(Float.class);
		formatter.setAllowsInvalid(true);
		formatter.setCommitsOnValidEdit(true);
		
		this.valueField = new JFormattedTextField(formatter);
		this.valueField.setOpaque(false);
		this.valueField.setHorizontalAlignment(SwingConstants.RIGHT);
		this.valueField.setPreferredSize(new Dimension(30, 0));
		this.valueField.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				SwingUtilities.invokeLater(() -> NumberField.this.validateTextEdit());
			}
			
			public void focusGained(FocusEvent e) {
				SwingUtilities.invokeLater(() -> {
					NumberField.this.valueField.setText(NumberField.this.valueLabel.getText());
					NumberField.this.valueField.selectAll();
				});
			}
		});
		this.valueField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode()) {
					case KeyEvent.VK_ESCAPE:
						NumberField.this.cancelTextEdit();
						break;
					case KeyEvent.VK_ENTER:
						NumberField.this.validateTextEdit();
						break;
				}
			}
		});
		
		this.setLayout(new BorderLayout());
		this.setOpaque(false);
		this.add(this.valueLabel, BorderLayout.CENTER);
	}
	
	private void startTextEdit() {
		if(this.editing) return;
		this.editing = true;
		
		UrmusicView.blockKeyEvent();
		
		this.remove(this.valueLabel);
		this.add(this.valueField, BorderLayout.CENTER);
		
		this.valueField.requestFocusInWindow();
		
		this.revalidate();
		this.repaint();
	}
	
	private void validateTextEdit() {
		if(!this.editing) return;
		this.editing = false;
		
		UrmusicView.freeKeyEvent();
		
		this.setValue((Number) this.valueField.getValue());
		if(this.onValueChange != null) this.onValueChange.accept(this);
		
		this.remove(this.valueField);
		this.add(this.valueLabel, BorderLayout.CENTER);
		
		this.revalidate();
		this.repaint();
	}
	
	private void cancelTextEdit() {
		if(!this.editing) return;
		this.editing = false;
		
		UrmusicView.freeKeyEvent();
		
		this.remove(this.valueField);
		this.add(this.valueLabel, BorderLayout.CENTER);
		
		this.revalidate();
		this.repaint();
	}
	
	public Consumer<NumberField> getOnValueChange() {
		return this.onValueChange;
	}

	public void setOnValueChange(Consumer<NumberField> onValueChange) {
		this.onValueChange = onValueChange;
	}

	public void setValue(Number val) {
		this.lastValue = val;
		this.valueLabel.setText(String.valueOf(val));
	}
	
	public Number getValue() {
		return this.lastValue;
	}
}
