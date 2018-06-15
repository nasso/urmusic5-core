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
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.security.Policy;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

import io.gitlab.nasso.urmusic.common.DataUtils;
import io.gitlab.nasso.urmusic.controller.UrmusicController;
import io.gitlab.nasso.urmusic.model.UrmusicModel;
import io.gitlab.nasso.urmusic.model.ffmpeg.FFmpeg;
import io.gitlab.nasso.urmusic.plugin.UrmPlugin;
import io.gitlab.nasso.urmusic.plugin.UrmPluginPolicy;
import io.gitlab.nasso.urmusic.view.UrmusicView;

public class Urmusic {
	private static UrmPlugin[] URM_PLUGINS;
	private static File URM_PLUGIN_FOLDER;
	private static File URM_STATIC_LIB_FOLDER;
	private static File URM_HOME;
	
	private Urmusic() {
	}
	
	public static final UrmPlugin[] getPlugins() {
		return URM_PLUGINS;
	}
	
	public static final File getPluginFolder() {
		return URM_PLUGIN_FOLDER;
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
		loadPlugins();
		
		UrmusicModel.init();
		UrmusicController.init();
		UrmusicView.init();
		
		// Test stuff below this line!
		
//		UrmusicController.addEffect(new TestVFX());
	}
	
	public static void loadPlugins() {
		Policy.setPolicy(new UrmPluginPolicy());
		System.setSecurityManager(new SecurityManager());
		
		List<UrmPlugin> pluginList = new ArrayList<>();
		for(File f : URM_PLUGIN_FOLDER.listFiles()) {
			try {
				JarFile jar = new JarFile(f);
				String mainClassName = jar.getManifest().getMainAttributes().getValue("Plugin-Main");
				jar.close();
				
				if(mainClassName == null) return;
				
				URLClassLoader ld = URLClassLoader.newInstance(new URL[] { f.toURI().toURL() });
				Class<?> mainClass = ld.loadClass(mainClassName);
				
				if(mainClass == null) return;
				
				UrmPlugin plugin = (UrmPlugin) mainClass.newInstance();
				pluginList.add(plugin);
				
				System.out.println("Found plugin: " + plugin.getName());
			} catch(MalformedURLException e) {
				e.printStackTrace();
			} catch(InstantiationException e) {
				e.printStackTrace();
			} catch(IllegalAccessException e) {
				e.printStackTrace();
			} catch(ClassNotFoundException e) {
				e.printStackTrace();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		
		URM_PLUGINS = pluginList.toArray(new UrmPlugin[pluginList.size()]);
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
		URM_PLUGIN_FOLDER = new File("./plugins").getAbsoluteFile();
		
		if(!URM_PLUGIN_FOLDER.exists() || !URM_PLUGIN_FOLDER.isDirectory())
			URM_PLUGIN_FOLDER.mkdir();
		
		try {
			URM_PLUGIN_FOLDER = URM_PLUGIN_FOLDER.getCanonicalFile();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		Urmusic.init();
	}
}
