package org.eclipse.epsilon.picto.web;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/***
 * Convert a filepath to a json tree structure that conforms to JsTree.com's
 * tree structure.
 * 
 * @author Alfa Yohannis
 *
 */
public class ProjectTreeToJson {

	public static void main(String[] args) throws IOException {

		String json = ProjectTreeToJson.convert("");
		System.out.println(json);

	}

	public static List<String> getPictoFiles(String path) throws IOException {
		List<String> paths = Files.walk(Paths.get(path)).filter(p -> !Files.isDirectory(p)).map(p -> p.toString())
				.filter(f -> f.toLowerCase().endsWith(".picto")
				).collect(Collectors.toList());
		paths = paths.stream().map(s -> s.replace(PictoApplication.WORKSPACE, "").replace("\\", "/")).collect(Collectors.toList());
		return paths;
	}

	/***
	 * Convert a filepath to a json tree structure that conforms to JsTree.com's
	 * tree structure.
	 * 
	 * @param target
	 * @return
	 * @throws IOException
	 */
	public static String convert(String path) throws IOException {
		List<String> paths = Files.walk(Paths.get(path)).filter(p -> !Files.isDirectory(p)).map(p -> p.toString())
				.filter(f -> f.toLowerCase().endsWith(".picto")
//						|| f.toLowerCase().endsWith(".ecore") 
//						|| f.toLowerCase().endsWith(".emf")
//						|| f.toLowerCase().endsWith(".flexmi")
//						|| f.toLowerCase().endsWith(".egx") 
//						|| f.toLowerCase().endsWith(".egl")
//						|| f.toLowerCase().endsWith(".model")
				).collect(Collectors.toList());

		ObjectMapper mapper = new ObjectMapper();
		ArrayNode root = mapper.createArrayNode();

		for (String p : paths) {
			System.out.println(p);
			String[] fragments = p.split("\\/|\\\\");

			ObjectNode parent = null;
			for (int i = 0; i < fragments.length; i++) {
				String fragment = fragments[i];
				if (fragment.equals("..") || fragment.equals(".") || fragment.equals("workspace"))
					continue;

				ObjectNode file = null;
				if (parent == null) {
					JsonNode node = contains(root, fragment);
					if (node != null) {
						parent = (ObjectNode) node;
						continue;
					}
				} else {
					ArrayNode children = (ArrayNode) parent.get("children");
					JsonNode node = contains(children, fragment);
					if (node != null) {
						parent = (ObjectNode) node;
						continue;
					}
				}

				file = mapper.createObjectNode();
				if (i < fragments.length - 1) {
					file.put("text", fragment);
					ObjectNode stateNode = file.putObject("state");
					stateNode.put("opened", true);
					file.putArray("children");
				} else {
					file.put("text", fragment);
					file.put("icon", "jstree-file");
				}
				if (parent != null) {
					ArrayNode children = (ArrayNode) parent.get("children");
					children.add(file);
				} else {
					root.add(file);
				}
				parent = file;
			}
		}

		String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
//		System.out.println(json);
		return json;
	}

	private static JsonNode contains(ArrayNode root, String fragment) {
		Iterator<JsonNode> iterator = root.iterator();
		while (iterator.hasNext()) {
			JsonNode temp = iterator.next();
			if (fragment.equals(temp.get("text").asText())) {
				return temp;
			}
		}
		return null;
	}
}
