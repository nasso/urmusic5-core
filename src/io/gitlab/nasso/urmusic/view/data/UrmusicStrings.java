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
package io.gitlab.nasso.urmusic.view.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.gitlab.nasso.urmusic.Urmusic;
import io.gitlab.nasso.urmusic.common.DataUtils;
import io.gitlab.nasso.urmusic.plugin.UrmPluginPackage;

public class UrmusicStrings {
	private static final Gson gson = new Gson();
	
	private static final Map<String, String> langmap = new HashMap<>();
	private static String currentLangTag = null;
	private static String currentLangName = null;
	
	private static final void mapLocalTree(String prefix, JsonObject map) {
		for(String key : map.keySet()) {
			if(key.isEmpty()) continue;
			
			JsonElement v = map.get(key);
			
			if(v.isJsonObject()) {
				JsonObject sub = (JsonObject) v;
				
				if(sub.has("")) {
					JsonElement e = sub.get("");
					if(e.isJsonPrimitive()) langmap.putIfAbsent(prefix + key, e.getAsString());
				}
				
				mapLocalTree(prefix + key + ".", sub);
			} else if(v.isJsonPrimitive()) langmap.putIfAbsent(prefix + key, v.getAsString());
		}
	}
	
	public static final void init(Locale loc) {
		currentLangTag = loc.toLanguageTag();
		
		File f = DataUtils.localFile("lang/" + currentLangTag + ".json");
		
		if(!f.exists()) {
			currentLangTag = "en";
			f = DataUtils.localFile("lang/en.json");
		}
		
		try {
			JsonObject root = gson.fromJson(new BufferedReader(new FileReader(f)), JsonObject.class);
			currentLangName = root.get("locale_name").getAsString();
			
			JsonObject map = root.getAsJsonObject("map");
			mapLocalTree("", map);
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		}
		
		UrmPluginPackage[] plugins = Urmusic.getPlugins();
		for(int i = 0; i < plugins.length; i++) {
			UrmPluginPackage upp = plugins[i];
			
			Object folder = upp.getPluginLangFolder();
			if(folder != null) {
				String folderStr = folder.toString();
				String jsonData = null;
				
				try {
					// First choice is currentLangTag
					jsonData = DataUtils.readFile((folderStr.endsWith("/") ? folderStr : folderStr + "/") + currentLangTag + ".json", upp.getPlugin().getClass().getClassLoader());

					// If not found, default to english
					if(jsonData == null)
						jsonData = DataUtils.readFile((folderStr.endsWith("/") ? folderStr : folderStr + "/") + "en.json", upp.getPlugin().getClass().getClassLoader());
				} catch(IOException e) {
					// Do nothing
				}

				// If no english then... uh well fix your thing mate
				if(jsonData != null) {
					JsonObject pluginRoot = gson.fromJson(jsonData, JsonObject.class);
					mapLocalTree("plugin." + upp.getPluginID() + ".", pluginRoot);
				}
			}
		}
	}
	
	public static final String getCurrentLanguageTag() {
		return currentLangTag;
	}
	
	public static final String getCurrentLanguageName() {
		return currentLangName;
	}
	
	public static final String getString(String key) {
		return langmap.getOrDefault(key, "[" + key + "]");
	}
	
	public static final String getString(UrmPluginPackage upp, String key) {
		return getString("plugin." + upp.getPluginID() + "." + key);
	}
	
	private UrmusicStrings() { }
}
