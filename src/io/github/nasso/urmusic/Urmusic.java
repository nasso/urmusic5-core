package io.github.nasso.urmusic;

import io.github.nasso.urmusic.controller.UrmusicController;
import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.effect.CircleMaskVFX;
import io.github.nasso.urmusic.model.project.Composition;
import io.github.nasso.urmusic.model.project.Project;
import io.github.nasso.urmusic.model.project.Track;
import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.github.nasso.urmusic.model.project.control.FloatParam;
import io.github.nasso.urmusic.model.project.control.RGBA32Param;
import io.github.nasso.urmusic.utils.MutableRGBA32;
import io.github.nasso.urmusic.utils.easing.penner.easing.Elastic;
import io.github.nasso.urmusic.view.UrmusicView;

public class Urmusic {
	public Urmusic() {
		UrmusicModel.init();
		UrmusicView.init();
		UrmusicController.init();
		
		Project prj = UrmusicModel.getCurrentProject();
		
		Composition comp = prj.getMainComposition();
		comp.setClearColor(new MutableRGBA32(0x5c2aaff));
		
		Track visuals = new Track(comp.getLength());
		visuals.setName("Visuals");
		
		TrackEffectInstance vignette = CircleMaskVFX.FX.instance();
		FloatParam radius = (FloatParam) vignette.getParamByName("radius");
		radius.addKeyFrame(0, 0.0f);
		radius.addKeyFrame(comp.getFramerate() * 2, 400.0f, Elastic::easeOut);
		radius.addKeyFrame(comp.getFramerate() * 3, 200.0f);
		
		TrackEffectInstance vignette2 = CircleMaskVFX.FX.instance();
		((RGBA32Param) vignette2.getParamByName("innerColor")).addKeyFrame(0, new MutableRGBA32(0xff0000ff));
		((RGBA32Param) vignette2.getParamByName("innerColor")).addKeyFrame(100, new MutableRGBA32(0x0000ffff));
		FloatParam radius2 = (FloatParam) vignette2.getParamByName("radius");
		radius2.addKeyFrame(0, 0.0f);
		radius2.addKeyFrame(comp.getFramerate() * 1, 300.0f);
		radius2.addKeyFrame(comp.getFramerate() * 2, 300.0f);
		radius2.addKeyFrame(comp.getFramerate() * 3, 0.0f);
		
		visuals.addEffect(vignette);
		visuals.addEffect(vignette2);
		
		prj.getMainComposition().getTimeline().addTrack(visuals);
	}
	
	public static void main(String[] args) {
		new Urmusic();
	}
}
