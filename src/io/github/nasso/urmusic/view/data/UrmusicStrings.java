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
package io.github.nasso.urmusic.view.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.github.nasso.urmusic.common.DataUtils;

public class UrmusicStrings {
	private static final Gson gson = new Gson();
	
	private static final Map<String, String> langmap = new HashMap<String, String>();
	private static String currentLangTag = null;
	private static String currentLangName = null;
	
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
			for(String key : map.keySet()) {
				langmap.put(key, map.get(key).getAsString());
			}
		} catch(FileNotFoundException e) {
			e.printStackTrace();
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
	
	private UrmusicStrings() { }
}
