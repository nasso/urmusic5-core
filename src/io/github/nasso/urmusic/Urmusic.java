package io.github.nasso.urmusic;

import io.github.nasso.urmusic.controller.UrmusicController;
import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.effect.VignetteVFX;
import io.github.nasso.urmusic.model.project.Project;
import io.github.nasso.urmusic.model.project.Track;
import io.github.nasso.urmusic.model.project.TrackEffect.TrackEffectInstance;
import io.github.nasso.urmusic.model.project.control.FloatParam;
import io.github.nasso.urmusic.utils.MutableRGBA32;
import io.github.nasso.urmusic.utils.easing.penner.easing.Elastic;
import io.github.nasso.urmusic.view.UrmusicView;

public class Urmusic {
	public Urmusic() {
		UrmusicModel.init();
		UrmusicView.init();
		UrmusicController.init();
		
		Project prj = UrmusicModel.getCurrentProject();
		
		prj.getMainComposition().setClearColor(new MutableRGBA32(0xffffffff));
		
		Track song = new Track();
		song.setName("Song");
		
		Track visuals = new Track();
		visuals.setName("Visuals");
		
		TrackEffectInstance vignette = VignetteVFX.FX.instance();
		FloatParam dist = (FloatParam) vignette.getControl("distance");
		dist.addKeyFrame(0, 0.0f);
		dist.addKeyFrame(120, 1.0f, Elastic::easeOut);
		
		visuals.addEffect(vignette);
		
		prj.getMainComposition().getTimeline().addTrack(song);
		prj.getMainComposition().getTimeline().addTrack(visuals);
	}
	
	public static void main(String[] args) {
		new Urmusic();
	}
}
