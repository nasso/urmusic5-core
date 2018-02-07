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
		comp.setClearColor(new MutableRGBA32(0xE621ffff));
		
		int fps = comp.getTimeline().getFramerate();
		
		Track visuals = new Track(comp.getTimeline().getLength());
		visuals.setName("Visuals");
		
		TrackEffectInstance vignette = CircleMaskVFX.FX.instance();
		RGBA32Param color1 = (RGBA32Param) vignette.getParamByName("color");
		color1.setValue(new MutableRGBA32(0xffffffff), 0);
		FloatParam radius = (FloatParam) vignette.getParamByName("outerRadius");
		FloatParam outerFade = (FloatParam) vignette.getParamByName("outerFade");
		radius.addKeyFrame(0, 0.0f);
		outerFade.addKeyFrame(0, 0.0f);
		radius.addKeyFrame(fps * 2, 400.0f, Elastic::easeOut);
		outerFade.addKeyFrame(fps * 2, 5.0f);
		radius.addKeyFrame(fps * 3, 200.0f);
		
		TrackEffectInstance vignette2 = CircleMaskVFX.FX.instance();
		RGBA32Param color2 = (RGBA32Param) vignette2.getParamByName("color");
		color2.addKeyFrame(0, new MutableRGBA32(0xff0000ff));
		color2.addKeyFrame(100, new MutableRGBA32(0x0000ffff));
		FloatParam radius2 = (FloatParam) vignette2.getParamByName("outerRadius");
		FloatParam outerFade2 = (FloatParam) vignette2.getParamByName("outerFade");
		radius2.addKeyFrame(0, 0.0f);
		radius2.addKeyFrame(fps * 1, 300.0f);
		outerFade2.addKeyFrame(fps * 1, 0.0f);
		radius2.addKeyFrame(fps * 2, 300.0f);
		outerFade2.addKeyFrame(fps * 2, 20.0f);
		outerFade2.addKeyFrame(fps * 3, 0.0f);
		radius2.addKeyFrame(fps * 3, 0.0f);
		
		visuals.addEffect(vignette);
		visuals.addEffect(vignette2);
		
		prj.getMainComposition().getTimeline().addTrack(visuals);
	}
	
	public static void main(String[] args) {
		new Urmusic();
	}
}
