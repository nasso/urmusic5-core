package io.github.nasso.urmusic.view.components.panels.properties.controls;

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

import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.NumberFormatter;

import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.project.control.FloatParam;
import io.github.nasso.urmusic.utils.MathUtils;
import io.github.nasso.urmusic.view.UrmusicView;

public class FloatParamUI extends EffectParamUI<FloatParam> {
	private static final long serialVersionUID = 8290439490941369516L;
	
	private JLabel valueLabel;
	private JFormattedTextField valueField;
	
	public FloatParamUI(FloatParam param) {
		super(param);
		
		this.valueLabel = new JLabel();
		this.valueLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
		this.valueLabel.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				if(MathUtils.boxContains(e.getX(), e.getY(), 0, 0, e.getComponent().getWidth(), e.getComponent().getHeight()))
					FloatParamUI.this.startTextEdit();
			}
		});
		
		NumberFormat format = NumberFormat.getNumberInstance(UrmusicView.getLocale());
		NumberFormatter formatter = new NumberFormatter(format);
		formatter.setValueClass(Float.class);
		formatter.setAllowsInvalid(true);
		formatter.setCommitsOnValidEdit(true);
		
		this.valueField = new JFormattedTextField(formatter);
		this.valueField.setHorizontalAlignment(SwingConstants.RIGHT);
		this.valueField.setPreferredSize(new Dimension(30, 0));
		this.valueField.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				SwingUtilities.invokeLater(() -> {
					FloatParamUI.this.validateTextEdit();
				});
			}
			
			public void focusGained(FocusEvent e) {
				SwingUtilities.invokeLater(() -> {
					FloatParamUI.this.valueField.setText(FloatParamUI.this.valueLabel.getText());
					FloatParamUI.this.valueField.selectAll();
				});
			}
		});
		this.valueField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode()) {
					case KeyEvent.VK_ENTER:
						FloatParamUI.this.validateTextEdit();
						break;
				}
			}
		});
		
		this.setLayout(new BorderLayout());
		this.add(this.valueLabel, BorderLayout.EAST);
	}
	
	private void startTextEdit() {
		UrmusicView.blockKeyEvent();
		
		this.remove(this.valueLabel);
		this.add(this.valueField, BorderLayout.EAST);
		
		this.valueField.requestFocusInWindow();
		
		this.revalidate();
		this.repaint();
	}
	
	private void validateTextEdit() {
		UrmusicView.freeKeyEvent();
		
		int frame = UrmusicModel.getFrameCursor();

		this.getParam().setValue(((Number) this.valueField.getValue()).floatValue(), frame);
		this.updateControl(frame);
		
		this.remove(this.valueField);
		this.add(this.valueLabel, BorderLayout.EAST);
		
		this.revalidate();
		this.repaint();
	}

	public void updateControl(int frame) {
		this.valueLabel.setText(String.valueOf(this.getParam().getValue(frame)));
	}
}
