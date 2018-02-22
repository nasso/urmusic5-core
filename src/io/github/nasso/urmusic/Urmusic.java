package io.github.nasso.urmusic;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

import io.github.nasso.urmusic.common.DataUtils;
import io.github.nasso.urmusic.common.MutableRGBA32;
import io.github.nasso.urmusic.controller.UrmusicController;
import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.project.Composition;
import io.github.nasso.urmusic.model.project.Project;
import io.github.nasso.urmusic.model.project.Track;
import io.github.nasso.urmusic.view.UrmusicView;

public class Urmusic {
	public static final boolean URM_FORCE_RES_EXPORT = true;
	public static final File URM_HOME = new File(System.getProperty("user.home") + File.separatorChar + ".urmusic");
	
	private Urmusic() {
	}
	
	@SuppressWarnings("unused")
	private static final void setupFiles() {
		if(!URM_HOME.exists()) URM_HOME.mkdirs();
		
		try {
			String internalAppdataFolder = "res/appdata";
			List<Path> appdata = DataUtils.listFilesInResource(internalAppdataFolder);
			
			for(Path p : appdata) {
				String parentStr = URM_HOME.getAbsolutePath() + File.separator;
				
				Path parent = p.getParent();
				if(parent != null) parentStr += parent.toString().replace('\\', '/');
				
				File f = new File(parentStr, p.getFileName().toString());
				
				if(URM_FORCE_RES_EXPORT || !f.exists()) DataUtils.exportResource(internalAppdataFolder + File.separatorChar + p.toString(), f.getAbsolutePath());
			}
		} catch(IOException e) {
			e.printStackTrace();
		} catch(URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	public static final void init() {
		Urmusic.setupFiles();
		
		UrmusicModel.init();
		UrmusicController.init();
		UrmusicView.init();
		
		Project prj = UrmusicModel.getCurrentProject();
		
		Composition comp = prj.getMainComposition();
		comp.setClearColor(new MutableRGBA32(0x0077ffff));
		
		Track visuals = new Track(comp.getTimeline().getLength());
		visuals.setName("Visuals");
		
		visuals.addEffect(UrmusicModel.STOCK_EFFECTS[2].instance());
		prj.getMainComposition().getTimeline().addTrack(visuals);
	}
	
	public static void main(String[] args) {
		Urmusic.init();
	}
}
