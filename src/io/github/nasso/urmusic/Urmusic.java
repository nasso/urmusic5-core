package io.github.nasso.urmusic;

import io.github.nasso.urmusic.controller.UrmusicController;
import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.view.UrmusicView;

public class Urmusic {
	public Urmusic() {
		UrmusicModel.init();
		UrmusicView.init();
		UrmusicController.init();
		
		UrmusicModel.getTimeline().addAudioTrack("Song");
		UrmusicModel.getTimeline().addVideoTrack("Visuals");
		UrmusicModel.getTimeline().addVideoTrack("More shit");
	}
	
	public static void main(String[] args) {
		new Urmusic();
	}
}
