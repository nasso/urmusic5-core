package io.github.nasso.urmusic;

import io.github.nasso.urmusic.controller.UrmusicController;
import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.effect.video.VignetteVFX;
import io.github.nasso.urmusic.model.project.Project;
import io.github.nasso.urmusic.model.project.audio.AudioTrack;
import io.github.nasso.urmusic.model.project.video.VideoTrack;
import io.github.nasso.urmusic.utils.MutableRGBA32;
import io.github.nasso.urmusic.view.UrmusicView;

public class Urmusic {
	public Urmusic() {
		UrmusicModel.init();
		UrmusicView.init();
		UrmusicController.init();
		
		Project prj = UrmusicModel.getCurrentProject();
		
		prj.getMainComposition().setClearColor(new MutableRGBA32(0x5e25afff));
		
		AudioTrack song = new AudioTrack();
		song.setName("Song");
		
		VideoTrack visuals = new VideoTrack();
		visuals.setName("Visuals");
		visuals.addEffect(VignetteVFX.FX.instance());
		
		prj.getMainComposition().getTimeline().addTrack(song);
		prj.getMainComposition().getTimeline().addTrack(visuals);
		prj.getMainComposition().getTimeline().addCompositeTrack("More shit");
	}
	
	public static void main(String[] args) {
		new Urmusic();
	}
}
