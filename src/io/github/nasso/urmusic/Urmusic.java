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
		
		int fps = comp.getFramerate();
		
		Track visuals = new Track(comp.getLength());
		visuals.setName("Visuals");
		
		TrackEffectInstance vignette = CircleMaskVFX.FX.instance();
		((RGBA32Param) vignette.getParamByName("innerColor")).setValue(new MutableRGBA32(0xffffffff), 0);
		((RGBA32Param) vignette.getParamByName("outerColor")).setValue(new MutableRGBA32(0xffffff00), 0);
		FloatParam radius = (FloatParam) vignette.getParamByName("radius");
		FloatParam penumbra = (FloatParam) vignette.getParamByName("penumbra");
		radius.addKeyFrame(0, 0.0f);
		penumbra.addKeyFrame(0, 0.0f);
		radius.addKeyFrame(fps * 2, 400.0f, Elastic::easeOut);
		penumbra.addKeyFrame(fps * 2, 5.0f);
		radius.addKeyFrame(fps * 3, 200.0f);
		
		TrackEffectInstance vignette2 = CircleMaskVFX.FX.instance();
		((RGBA32Param) vignette2.getParamByName("innerColor")).addKeyFrame(0, new MutableRGBA32(0xff0000ff));
		((RGBA32Param) vignette2.getParamByName("outerColor")).addKeyFrame(0, new MutableRGBA32(0xff000000));
		((RGBA32Param) vignette2.getParamByName("innerColor")).addKeyFrame(100, new MutableRGBA32(0x0000ffff));
		((RGBA32Param) vignette2.getParamByName("outerColor")).addKeyFrame(100, new MutableRGBA32(0x0000ff00));
		FloatParam radius2 = (FloatParam) vignette2.getParamByName("radius");
		FloatParam penumbra2 = (FloatParam) vignette2.getParamByName("penumbra");
		radius2.addKeyFrame(0, 0.0f);
		radius2.addKeyFrame(fps * 1, 300.0f);
		penumbra2.addKeyFrame(fps * 1, 0.0f);
		radius2.addKeyFrame(fps * 2, 300.0f);
		penumbra2.addKeyFrame(fps * 2, 20.0f);
		penumbra2.addKeyFrame(fps * 3, 0.0f);
		radius2.addKeyFrame(fps * 3, 0.0f);
		
		visuals.addEffect(vignette);
		visuals.addEffect(vignette2);
		
		prj.getMainComposition().getTimeline().addTrack(visuals);
	}
	
	public static void main(String[] args) {
		new Urmusic();
	}
}
