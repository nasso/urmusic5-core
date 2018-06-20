package io.gitlab.nasso.urmusic.plugin;

import java.util.jar.Attributes;

public class UrmPluginPackage {
	public static final String ATTR_KEY_NAME = "UrmPlugin-Name";
	public static final String ATTR_KEY_ID = "UrmPlugin-ID";
	public static final String ATTR_KEY_MAIN = "UrmPlugin-MainClass";
	public static final String ATTR_KEY_LANGFOLDER = "UrmPlugin-LangFolder";
	
	private final UrmPlugin plugin;
	private final Attributes attr;
	
	public UrmPluginPackage(UrmPlugin plugin, Attributes attr) {
		this.plugin = plugin;
		this.attr = attr;
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
