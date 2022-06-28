package org.eclipse.epsilon.picto.web;

import java.util.HashMap;
import java.util.Map;

public class ViewContentCache {

	private final Map<String, String> viewContentCache = new HashMap<String, String>();

	public void putViewContentCache(String path, String viewContent) {
		viewContentCache.put(path, viewContent);
	}

	public String getViewContentCache(String elementUrl) {
		return viewContentCache.get(elementUrl);
	}
}
