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

public class UrmusicStrings {
	private static final Gson gson = new Gson();
	
	private static final Map<String, String> langmap = new HashMap<String, String>();
	private static String currentLangTag = null;
	private static String currentLangName = null;
	
	public static final void init(Locale loc) {
		currentLangTag = loc.toLanguageTag();
		
		File f = new File("lang/" + currentLangTag + ".json");
		
		if(!f.exists()) {
			currentLangTag = "en";
			f = new File("lang/en.json");
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
		return langmap.getOrDefault(key, "MISSING_STRG");
	}
	
	private UrmusicStrings() { }
}
