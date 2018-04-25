package io.github.nasso.urmusic;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import io.github.nasso.urmusic.common.DataUtils;
import io.github.nasso.urmusic.common.easing.EasingFunction;
import io.github.nasso.urmusic.controller.UrmusicController;
import io.github.nasso.urmusic.model.UrmusicModel;
import io.github.nasso.urmusic.model.effect.CircleMaskVFX.CircleMaskVFXInstance;
import io.github.nasso.urmusic.model.project.codec.ProjectCodec;
import io.github.nasso.urmusic.model.project.param.FloatParam;
import io.github.nasso.urmusic.view.UrmusicView;

public class Urmusic {
	public static final boolean URM_FORCE_RES_EXPORT = true;
	public static final File URM_STATIC_LIB_FOLDER = new File(System.getProperty("staticLibFolder"));
	public static final File URM_HOME = new File(System.getProperty("user.home") + File.separatorChar + ".urmusic");
	
	private Urmusic() {
	}
	
	@SuppressWarnings("unused")
	private static final void setupFiles() {
		if(!Urmusic.URM_HOME.exists()) Urmusic.URM_HOME.mkdirs();
		
		try {
			String internalAppdataFolder = "res/appdata";
			List<Path> appdata = DataUtils.listFilesInResource(internalAppdataFolder);
			
			for(Path p : appdata) {
				String parentStr = Urmusic.URM_HOME.getAbsolutePath() + File.separator;
				
				Path parent = p.getParent();
				if(parent != null) parentStr += parent.toString().replace('\\', '/');
				
				File f = new File(parentStr, p.getFileName().toString());
				
				if(Urmusic.URM_FORCE_RES_EXPORT || !f.exists()) DataUtils.exportResource(internalAppdataFolder + File.separatorChar + p.toString(), f.getAbsolutePath());
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

//		UrmusicController.openProject(Paths.get("./test." + ProjectCodec.FILE_EXT));
		
		UrmusicController.addTrack();
		UrmusicController.focusTrack(UrmusicModel.getCurrentProject().getMainComposition().getTimeline().getTracks().get(0));
		UrmusicController.addEffect(UrmusicModel.STOCK_EFFECTS[1]);
		
		CircleMaskVFXInstance fx = (CircleMaskVFXInstance) UrmusicController.getFocusedTrack().getEffect(0);
		((FloatParam) fx.getParamByID("outerRadius")).addKeyFrame(0f, 50f, EasingFunction.LINEAR);
		((FloatParam) fx.getParamByID("outerRadius")).addKeyFrame(2f, 100f, EasingFunction.EASE_IN_ELASTIC);
		((FloatParam) fx.getParamByID("outerRadius")).addKeyFrame(4f, 200f, EasingFunction.EASE_IN_OUT_ELASTIC);
		
		UrmusicController.saveCurrentProject(Paths.get("./test." + ProjectCodec.FILE_EXT));
		
//		UrmusicController.addEffect(new TestVFX());
	}
	
	public static void main(String[] args) {
		Urmusic.init();
	}
}
