package org.eclipse.epsilon.picto.web;

import java.util.HashMap;
import java.util.Map;

public class ElementViewTreeMap {

	private final Map<String, String> elementViewTreeMap = new HashMap<String, String>();

	public void putElementViewTree(String elementUri, String viewContent) {
		elementViewTreeMap.put(elementUri, viewContent);
	}

	public String getElementViewTree(String elementUrl) {
		return elementViewTreeMap.get(elementUrl);
	}
}
