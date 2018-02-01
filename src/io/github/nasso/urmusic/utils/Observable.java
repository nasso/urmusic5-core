package io.github.nasso.urmusic.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Observable {
	private Map<String, List<Consumer<Observable>>> listeners = new HashMap<>();
	
	public void addEventListener(String event, Consumer<Observable> consumer) {
		if(consumer == null) return;
		
		List<Consumer<Observable>> listlist = this.listeners.get(event);
		
		if(listlist == null) {
			listlist = new ArrayList<>();
			this.listeners.put(event, listlist);
		}
		
		listlist.add(consumer);
	}
	
	public void removeEventListener(String event, Consumer<Observable> consumer) {
		List<Consumer<Observable>> listlist = this.listeners.get(event);
		
		if(listlist == null) return;
		
		listlist.remove(consumer);
	}
	
	public void triggerEvent(String event) {
		List<Consumer<Observable>> listlist = this.listeners.get(event);
		if(listlist == null) return;
		
		for(int i = 0, l = listlist.size(); i < l; i++)
			listlist.get(i).accept(this);
	}
}
