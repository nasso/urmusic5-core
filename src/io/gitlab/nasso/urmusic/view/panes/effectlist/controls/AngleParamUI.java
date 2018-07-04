package io.gitlab.nasso.urmusic.view.panes.effectlist.controls;

import javax.swing.JComponent;

import io.gitlab.nasso.urmusic.controller.UrmusicController;
import io.gitlab.nasso.urmusic.model.project.VideoEffect.VideoEffectInstance;
import io.gitlab.nasso.urmusic.model.project.param.AngleParam;
import io.gitlab.nasso.urmusic.view.components.UrmAngleSpinner;

public class AngleParamUI extends EffectParamUI<AngleParam> {
	private UrmAngleSpinner spinner;
	
	public AngleParamUI(VideoEffectInstance fx, AngleParam param) {
		super(fx, param);
	}

	public JComponent buildUI() {
		return this.spinner = new UrmAngleSpinner(UrmusicController.getParamValueNow(this.getParam()), this.getParam().getStep(), true, (s) -> {
			UrmusicController.setParamValueNow(this.getParam(), s.getAngleValue());
		});
	}

	public void updateControl() {
		this.spinner.setAngleValue(UrmusicController.getParamValueNow(this.getParam()));
	}
}
