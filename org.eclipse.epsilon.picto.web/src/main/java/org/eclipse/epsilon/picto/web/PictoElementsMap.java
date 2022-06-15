package org.eclipse.epsilon.picto.web;

import java.util.HashMap;
import java.util.Map;

public class PictoElementsMap {

	public static final String PICTO_TREE = "PictoTree";
	private static final Map<String, ElementViewTreeMap> pictoElementsMap = new HashMap<String, ElementViewTreeMap>();

	public static Map<String, ElementViewTreeMap> getMap() {
		return pictoElementsMap;
	}

	public static ElementViewTreeMap addPictoFile(String pictoFilename) {
		ElementViewTreeMap map = getElementViewContentMap(pictoFilename);
		if (map == null) {
			map = new ElementViewTreeMap();
			pictoElementsMap.put(pictoFilename, map);
		}
		return map;
	}

	public static ElementViewTreeMap getElementViewContentMap(String pictoFilename) {
		return pictoElementsMap.get(pictoFilename);
	}
}
