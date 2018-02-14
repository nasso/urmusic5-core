package io.github.nasso.urmusic.view.panes.effectlist.controls;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JComponent;

import io.github.nasso.urmusic.common.BoolValue;
import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.github.nasso.urmusic.model.project.control.BooleanParam;

public class BooleanParamUI extends EffectParamUI<BooleanParam> {
	private JCheckBox box;
	
	public BooleanParamUI(TrackEffectInstance fx, BooleanParam param) {
		super(fx, param);
		
		this.setLayout(new BorderLayout());
		this.add(this.box, BorderLayout.EAST);
	}

	public void updateControl(int frame) {
		this.box.setSelected(this.getParam().getValue(frame) == BoolValue.TRUE);
	}

	public JComponent buildUI() {
		this.box = new JCheckBox(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				BooleanParamUI.this.getParam().setValue(BooleanParamUI.this.box.isSelected() ? BoolValue.TRUE : BoolValue.FALSE, UrmusicModel.getFrameCursor());
			}
		});
		this.box.setOpaque(false);
		
		return this.box;
	}
}
