package io.gitlab.nasso.urmusic.plugin;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

public class UrmPluginPackage {
	public static final String ATTR_KEY_NAME = "UrmPlugin-Name";
	public static final String ATTR_KEY_ID = "UrmPlugin-ID";
	public static final String ATTR_KEY_MAIN = "UrmPlugin-MainClass";
	public static final String ATTR_KEY_LANGFOLDER = "UrmPlugin-LangFolder";
	
	private File jarFilePath;
	
	private UrmPlugin plugin;
	private Attributes attr;

	public UrmPluginPackage(File jarFilePath) throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException {
		this.jarFilePath = jarFilePath;
		this.load();
	}
	
	private void load() throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException {
		System.out.println("Loading plugin: " + this.jarFilePath.getName());
		
		try {
			JarFile jar = new JarFile(this.jarFilePath);
			Attributes mainAttributes = jar.getManifest().getMainAttributes();
			String mainClassName = mainAttributes.getValue(ATTR_KEY_MAIN);
			jar.close();
			
			if(mainClassName == null) throw new ClassNotFoundException("No " + ATTR_KEY_MAIN + " found in manifest.");
			
			URLClassLoader ld = URLClassLoader.newInstance(new URL[] { this.jarFilePath.toURI().toURL() });
			Class<?> mainClass = ld.loadClass(mainClassName);
			
			if(mainClass == null) throw new ClassNotFoundException(mainClassName + " not found.");
			
			this.plugin = (UrmPlugin) mainClass.newInstance();
			this.attr = mainAttributes;
		} catch(MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	public UrmPlugin getPlugin() {
		return this.plugin;
	}
	
	public String getPluginName() {
		return this.getManifestAttribute(ATTR_KEY_NAME);
	}
	
	public String getPluginID() {
		return this.getManifestAttribute(ATTR_KEY_ID);
	}
	
	public String getPluginMain() {
		return this.getManifestAttribute(ATTR_KEY_MAIN);
	}
	
	public String getPluginLangFolder() {
		return this.getManifestAttribute(ATTR_KEY_LANGFOLDER);
	}
	
	private String getManifestAttribute(String key) {
		return this.attr.getValue(key);
	}
}
