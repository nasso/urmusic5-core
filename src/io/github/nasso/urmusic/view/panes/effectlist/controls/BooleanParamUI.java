package io.github.nasso.urmusic.view.panes.effectlist.controls;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;

import io.github.nasso.urmusic.common.BoolValue;
import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.project.control.BooleanParam;

public class BooleanParamUI extends EffectParamUI<BooleanParam> {
	private static final long serialVersionUID = -8300159978938206819L;
	
	private JCheckBox box;
	
	public BooleanParamUI(BooleanParam param) {
		super(param);
		
		this.box = new JCheckBox(new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				int frame = UrmusicModel.getFrameCursor();
				
				BooleanParamUI.this.getParam().setValue(BooleanParamUI.this.box.isSelected() ? BoolValue.TRUE : BoolValue.FALSE, frame);
			}
		});
		this.box.setOpaque(false);
		
		this.setLayout(new BorderLayout());
		this.add(this.box, BorderLayout.EAST);
	}
	

	public void updateControl(int frame) {
		this.box.setSelected(this.getParam().getValue(frame) == BoolValue.TRUE);
	}
}
