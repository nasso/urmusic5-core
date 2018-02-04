package io.github.nasso.urmusic;

import io.github.nasso.urmusic.controller.UrmusicController;
import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.effect.VignetteVFX;
import io.github.nasso.urmusic.model.project.Composition;
import io.github.nasso.urmusic.model.project.Project;
import io.github.nasso.urmusic.model.project.Track;
import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.github.nasso.urmusic.model.project.control.FloatParam;
import io.github.nasso.urmusic.model.project.control.RGBA32Param;
import io.github.nasso.urmusic.utils.MutableRGBA32;
import io.github.nasso.urmusic.utils.easing.EasingFunction;
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
		
		Track song = new Track(comp.getLength());
		song.setName("Song");
		
		Track visuals = new Track(comp.getLength());
		visuals.setName("Visuals");
		
		TrackEffectInstance vignette = VignetteVFX.FX.instance();
		FloatParam dist = (FloatParam) vignette.getParamByName("distance");
		dist.addKeyFrame(0, 0.0f);
		dist.addKeyFrame(120, 1.0f, Elastic::easeOut);
		
		TrackEffectInstance vignette2 = VignetteVFX.FX.instance();
		((RGBA32Param) vignette2.getParamByName("outerColor")).addKeyFrame(0, new MutableRGBA32(0xff0000ff));
		((RGBA32Param) vignette2.getParamByName("outerColor")).addKeyFrame(100, new MutableRGBA32(0x0000ffff));
		FloatParam dist2 = (FloatParam) vignette2.getParamByName("distance");
		dist2.addKeyFrame(0, 2.0f);
		dist2.addKeyFrame(150, 0.2f, EasingFunction.EASE_OUT);
		dist2.addKeyFrame(220, 4.0f, EasingFunction.EASE_OUT);
		
		visuals.addEffect(vignette);
		visuals.addEffect(vignette2);
		
		prj.getMainComposition().getTimeline().addTrack(song);
		prj.getMainComposition().getTimeline().addTrack(visuals);
	}
	
	public static void main(String[] args) {
		new Urmusic();
	}
}
