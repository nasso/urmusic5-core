package io.github.nasso.urmusic.view.panes.effectlist.controls;

import java.awt.Font;
import java.awt.event.ItemEvent;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import io.github.nasso.urmusic.controller.UrmusicController;
import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.github.nasso.urmusic.model.project.param.OptionParam;
import io.github.nasso.urmusic.view.data.UrmusicStrings;

public class OptionParamUI extends EffectParamUI<OptionParam> {
	private JComboBox<String> combo;
	
	public OptionParamUI(TrackEffectInstance fx, OptionParam param) {
		super(fx, param);
	}

	public void updateControl() {
		this.combo.setSelectedIndex(UrmusicController.getParamValueNow(this.getParam()));
	}

	public JComponent buildUI() {
		this.combo = new JComboBox<>();
		this.combo.setFont(this.combo.getFont().deriveFont(Font.PLAIN, 11f));
		
		OptionParam p = this.getParam();
		for(int i = 0; i < p.getOptionCount(); i++)
			this.combo.addItem(
					UrmusicStrings.getString(
							"effect." + this.getEffectInstance().getEffectClass().getEffectClassName() +
							".param." + p.getName() +
							".option." + p.getOptionName(i)));
		
		this.combo.addItemListener((e) -> {
			if(e.getStateChange() == ItemEvent.SELECTED)
				UrmusicController.setParamValueNow(this.getParam(), this.combo.getSelectedIndex());
		});
		
		return this.combo;
	}
}
