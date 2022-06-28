package org.eclipse.epsilon.picto.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FileViewContentCache {

	public static final String PICTO_TREE = "/";
	private static final Map<String, ViewContentCache> fileViewContentCache = new HashMap<String, ViewContentCache>();
	private static final List<AccessRecord> accessRecords = new ArrayList<>();

	public static List<AccessRecord> getAccessRecords() {
		return accessRecords;
	}

	public static AccessRecord addAccessRecord(String fileName, String ruleName, String contextUri, String elementUri,
			String propertyName, String path) {
		AccessRecord accessRecord = new AccessRecord(fileName, ruleName, contextUri, elementUri, propertyName, path);
		System.out.println(accessRecord.toString());
		addAccessRecords(accessRecord);
		return accessRecord;
	}

	public static String getPathFromAccessRecords(String fileName, String ruleName, String contextUri, String elementUri,
			String propertyName) {
		AccessRecord ar = accessRecords.stream()
				.filter(r -> r.getFileName() == fileName && r.getRuleName() == ruleName
						&& r.getContextUri() == contextUri && r.getElementUri() == elementUri)
				.reduce((first, second) -> second).orElse(null);
		return ar.getPath();
	}

	public static void addAccessRecords(AccessRecord accessRecord) {
		accessRecords.add(accessRecord);
	}

	public static Map<String, ViewContentCache> getMap() {
		return fileViewContentCache;
	}

	public static ViewContentCache addPictoFile(String pictoFilename) {
		ViewContentCache map = getViewContentCache(pictoFilename);
		if (map == null) {
			map = new ViewContentCache();
			fileViewContentCache.put(pictoFilename, map);
		}
		return map;
	}

	public static ViewContentCache getViewContentCache(String pictoFilename) {
		return fileViewContentCache.get(pictoFilename);
	}
}
