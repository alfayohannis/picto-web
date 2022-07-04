package org.eclipse.epsilon.picto.web;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/***
 * Receive Requests and send back Responses
 * 
 * @author Alfa Yohannis
 *
 */

@Controller
public class PictoController {

//	/* Instance variable(s): */
//	@Autowired
//	@Qualifier("messageTemplateEngine")
//	protected SpringTemplateEngine mMessageTemplateEngine;

//	@GetMapping("/greeting")
//	public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
//		model.addAttribute("name", name);
//		return "greeting";
//	}

//	@RequestMapping(value = "/pictofiles", method = RequestMethod.GET)
////	@GetMapping("/pictofiles", method=RequestMethod.GET)
//	public String getPictoFiles(String information, Model model) throws IOException {
//		String temp = ProjectTreeToJson.convert(PictoApplication.WORKSPACE);
////		model.addAttribute("json", temp);
////		Context theContext = new Context();
////		theContext.setVariable("pictofiles", temp);
////		final String theJsonMessage = mMessageTemplateEngine.process("json/pictofiles", theContext);
//		model.addAttribute("pictofiles", temp);
//		return "json/pictofiles";
//
////		return temp;
//	}

	@GetMapping(value = "/")
	public String getPictoFiles(String information, Model model) throws IOException {
		List<String> pictoFiles = ProjectTreeToJson.getPictoFiles(PictoApplication.WORKSPACE);
		model.addAttribute("pictofiles", pictoFiles);
		return "pictofiles";
	}

//	@GetMapping(value = "/pictojson", produces = MediaType.APPLICATION_JSON_VALUE)
//	public String getPictoJson(String file, String target, String name, Model model) throws Exception {
//		String result = FileViewContentCache.getElementViewContentMap(file).getElementViewTree(target);
//		return result;
//	}

	@GetMapping(value = "/picto")
	public String getPicto(String file, String path, String name, Model model) throws Exception {
		model.addAttribute("pictoName", file);
		if (FileViewContentCache.getViewContentCache(file) == null) {
			File pictoFile = new File((new File(PictoApplication.WORKSPACE + file)).getAbsolutePath());
			WebEglPictoSource source = new WebEglPictoSource();
			source.transform(pictoFile);
		}
		if (path == null) {
			String treeResult = FileViewContentCache.getViewContentCache(file)
					.getViewContentCache(FileViewContentCache.PICTO_TREE);
			model.addAttribute("treeResponse", treeResult);
		} else {
			String treeResult = FileViewContentCache.getViewContentCache(file)
					.getViewContentCache(FileViewContentCache.PICTO_TREE);
			String viewResult = FileViewContentCache.getViewContentCache(file).getViewContentCache(path);
			model.addAttribute("treeResponse", treeResult);
			model.addAttribute("viewResponse", viewResult);
			model.addAttribute("selectedUri", path);
		}

		return "picto";
	}

//	@MessageMapping("/treeview")
//	@SendTo("/topic/picto")
//	public PictoResponse getTreeView(PictoRequest message) throws Exception {
//		WebEglPictoSource source = new WebEglPictoSource();
//		File pictoFile = new File((new File(PictoApplication.WORKSPACE + message.getPictoFile())).getAbsolutePath());
//		source.transform(pictoFile);
//		String filename = pictoFile.getAbsolutePath()
//				.replace(new File(PictoApplication.WORKSPACE).getAbsolutePath() + File.separator, "")
//				.replace("\\", "/");
//		Object x = FileViewContentCache.getMap();
//		String result = FileViewContentCache.getElementViewContentMap(filename)
//				.getElementViewTree(FileViewContentCache.PICTO_TREE);
//		PictoResponse pictoResponse = new PictoResponse(result);
//		pictoResponse.setType("TreeView");
//
//		MessageChannel brokerChannel = context.getBean("brokerChannel", MessageChannel.class);
//		SimpMessagingTemplate messaging = new SimpMessagingTemplate(brokerChannel);
//		messaging.setMessageConverter(new MappingJackson2MessageConverter());
//		messaging.convertAndSend("/topic/picto/" + message.getPictoFile(), pictoResponse);
//
//		return pictoResponse;
//	}

//	@MessageMapping("/picto-web")
//	@SendTo("/topic/picto")
//	public PictoResponse sendBackFileUpdate(File modifiedFile) throws Exception {
//		WebEglPictoSource source = new WebEglPictoSource();
//		source.transform(modifiedFile);
//		String filename = modifiedFile.getAbsolutePath()
//				.replace(new File(PictoApplication.WORKSPACE).getAbsolutePath() + File.separator, "")
//				.replace("\\", "/");
//		String result = FileViewContentCache.getElementViewContentMap(filename)
//				.getElementViewTree(FileViewContentCache.PICTO_TREE);
//		PictoResponse pictoResponse = new PictoResponse(result);
//		pictoResponse.setType("TreeView");
//
//		MessageChannel brokerChannel = context.getBean("brokerChannel", MessageChannel.class);
//		SimpMessagingTemplate messaging = new SimpMessagingTemplate(brokerChannel);
//		messaging.setMessageConverter(new MappingJackson2MessageConverter());
//		messaging.convertAndSend("/topic/picto/" + modifiedFile.getName(), pictoResponse);
//
//		return pictoResponse;
//	}

//	@MessageMapping("/projecttree")
//	@SendTo("/topic/picto")
//	public PictoResponse getProjectTree(PictoRequest message) throws Exception {
//
//		String temp = ProjectTreeToJson.convert(PictoApplication.WORKSPACE);
//		PictoResponse pictoResponse = new PictoResponse(temp);
//		pictoResponse.setType("ProjectTree");
//		return pictoResponse;
//	}
//
//	@MessageMapping("/openfile")
//	@SendTo("/topic/picto")
//	public PictoResponse openFile(PictoRequest message) throws Exception {
//
//		String temp = Files.readString(Paths.get(PictoApplication.WORKSPACE + message.getCode()));
//		PictoResponse pictoResponse = new PictoResponse(temp);
//		pictoResponse.setType("OpenFile");
//		return pictoResponse;
//	}
}
