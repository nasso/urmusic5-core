/*******************************************************************************
 * urmusic - The Free and Open Source Music Visualizer Tool
 * Copyright (C) 2018  nasso (https://github.com/nasso)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * Contact "nasso": nassomails -at- gmail dot com
 ******************************************************************************/
package io.gitlab.nasso.urmusic;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import io.gitlab.nasso.urmusic.common.DataUtils;
import io.gitlab.nasso.urmusic.common.easing.EasingFunction;
import io.gitlab.nasso.urmusic.controller.UrmusicController;
import io.gitlab.nasso.urmusic.model.UrmusicModel;
import io.gitlab.nasso.urmusic.model.effect.CircleMaskVFX.CircleMaskVFXInstance;
import io.gitlab.nasso.urmusic.model.ffmpeg.FFmpeg;
import io.gitlab.nasso.urmusic.model.project.param.FloatParam;
import io.gitlab.nasso.urmusic.view.UrmusicView;

public class Urmusic {
	private static File URM_STATIC_LIB_FOLDER;
	private static File URM_HOME;
	
	private Urmusic() {
	}
	
	public static final File getStaticLibFolder() {
		return URM_STATIC_LIB_FOLDER;
	}
	
	public static final File getHome() {
		return URM_HOME;
	}
	
	private static final void setupFiles() {
		if(!Urmusic.URM_HOME.exists()) Urmusic.URM_HOME.mkdirs();
		
		try {
			String internalAppdataFolder = "res/appdata";
			List<Path> appdata = DataUtils.listFilesInResource(internalAppdataFolder);
			
			boolean forceResExport = System.getProperty("forceResExport") != null;
			
			for(Path p : appdata) {
				String parentStr = Urmusic.URM_HOME.getAbsolutePath() + File.separator;
				
				Path parent = p.getParent();
				if(parent != null) parentStr += parent.toString().replace('\\', '/');
				
				File f = new File(parentStr, p.getFileName().toString());
				
				if(forceResExport || !f.exists()) DataUtils.exportResource(internalAppdataFolder + File.separatorChar + p.toString(), f.getAbsolutePath());
			}
		} catch(IOException e) {
			e.printStackTrace();
		} catch(URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	public static final void init() {
		Urmusic.setupFiles();
		
		FFmpeg.init();
		
		UrmusicModel.init();
		UrmusicController.init();
		UrmusicView.init();
		
		// Test stuff below this line!
		UrmusicController.addTrack();
		UrmusicController.focusTrack(UrmusicModel.getCurrentProject().getMainComposition().getTimeline().getTracks().get(0));
		
		UrmusicController.addEffect(UrmusicModel.STOCK_EFFECTS[1]);
		CircleMaskVFXInstance fx = (CircleMaskVFXInstance) UrmusicController.getFocusedTrack().getEffect(0);
		((FloatParam) fx.getParamByID("outerRadius")).addKeyFrame(0f, 50f, EasingFunction.LINEAR);
		((FloatParam) fx.getParamByID("outerRadius")).addKeyFrame(2f, 100f, EasingFunction.EASE_IN_ELASTIC);
		((FloatParam) fx.getParamByID("outerRadius")).addKeyFrame(4f, 200f, EasingFunction.EASE_IN_OUT_ELASTIC);
		
		// save there just so we don't get annoyed
		if(System.getProperty("os.name").equals("Linux")) {
			UrmusicController.saveCurrentProject(Paths.get("/dev/null"));
		}
		
//		UrmusicController.addEffect(new TestVFX());
	}
	
	public static void main(String[] args) {
		String os = System.getProperty("os.name").toLowerCase();
		
		if((os.indexOf("mac") >= 0) || (os.indexOf("darwin") >= 0)) {
			// TODO: Add Mac OS binaries
		} else if(os.indexOf("win") >= 0) {
			URM_STATIC_LIB_FOLDER = new File("libs/static/win32").getAbsoluteFile();
		} else if(os.indexOf("nux") >= 0) {
			URM_STATIC_LIB_FOLDER = new File("libs/static/linux").getAbsoluteFile();
		} else {
			String f = System.getProperty("staticLibFolder");
			if(f != null) URM_STATIC_LIB_FOLDER = new File(f).getAbsoluteFile();
		}
		
		String homeFolderName = System.getProperty("homeOverride");
		if(homeFolderName == null) homeFolderName = "urmusic";
		
		URM_HOME = new File(System.getProperty("user.home") + File.separatorChar + "." + homeFolderName).getAbsoluteFile();
		
		Urmusic.init();
	}
}
