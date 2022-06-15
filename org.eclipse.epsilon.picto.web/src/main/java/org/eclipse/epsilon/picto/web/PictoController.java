package org.eclipse.epsilon.picto.web;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.eclipse.epsilon.picto.dom.PictoPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

/***
 * Receive Requests and send back Responses
 * 
 * @author Alfa Yohannis
 *
 */

@Controller
public class PictoController {

	public final FileWatcher FILE_WATCHER = new FileWatcher(this);

	@Autowired
	private ApplicationContext context;

	@Autowired
	public SimpMessagingTemplate template;

//	/* Instance variable(s): */
//	@Autowired
//	@Qualifier("messageTemplateEngine")
//	protected SpringTemplateEngine mMessageTemplateEngine;

	public PictoController() throws Exception {
		PictoPackage.eINSTANCE.eClass();
		// WORKSPACE = PictoApplication.PICTO_FILES.getParentFile().getAbsolutePath() +
		// File.separator;
		FILE_WATCHER.start();
	}

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
//	public String getPictoJson(String file, String uri, String name, Model model) throws Exception {
//		String result = PictoElementsMap.getElementViewContentMap(file).getElementViewTree(uri);
//		return result;
//	}
	
	@GetMapping(value = "/picto")
	public String getPicto(String file, String uri, String name, Model model) throws Exception {
		model.addAttribute("pictoName", file);
		if (uri == null) {
			WebEglPictoSource source = new WebEglPictoSource();
			File pictoFile = new File((new File(PictoApplication.WORKSPACE + file)).getAbsolutePath());
			source.transform(pictoFile);
			uri = PictoElementsMap.PICTO_TREE;
		}
		String result = PictoElementsMap.getElementViewContentMap(file).getElementViewTree(uri);

		model.addAttribute("pictoResponse", result);

		return "picto";
	}

	@MessageMapping("/treeview")
	@SendTo("/topic/picto")
	public PictoResponse getTreeView(PictoRequest message) throws Exception {
		WebEglPictoSource source = new WebEglPictoSource();
		File pictoFile = new File((new File(PictoApplication.WORKSPACE + message.getPictoFile())).getAbsolutePath());
		source.transform(pictoFile);
		String filename = pictoFile.getAbsolutePath()
				.replace(new File(PictoApplication.WORKSPACE).getAbsolutePath() + File.separator, "")
				.replace("\\", "/");
		Object x = PictoElementsMap.getMap();
		String result = PictoElementsMap.getElementViewContentMap(filename)
				.getElementViewTree(PictoElementsMap.PICTO_TREE);
		PictoResponse pictoResponse = new PictoResponse(result);
		pictoResponse.setType("TreeView");

		MessageChannel brokerChannel = context.getBean("brokerChannel", MessageChannel.class);
		SimpMessagingTemplate messaging = new SimpMessagingTemplate(brokerChannel);
		messaging.setMessageConverter(new MappingJackson2MessageConverter());
		messaging.convertAndSend("/topic/picto/" + message.getPictoFile(), pictoResponse);

		return pictoResponse;
	}

	@MessageMapping("/gs-guide-websocket")
	@SendTo("/topic/picto")
	public PictoResponse sendBackFileUpdate(File modifiedFile) throws Exception {
		WebEglPictoSource source = new WebEglPictoSource();
		source.transform(modifiedFile);
		String filename = modifiedFile.getAbsolutePath()
				.replace(new File(PictoApplication.WORKSPACE).getAbsolutePath() + File.separator, "")
				.replace("\\", "/");
		String result = PictoElementsMap.getElementViewContentMap(filename)
				.getElementViewTree(PictoElementsMap.PICTO_TREE);
		PictoResponse pictoResponse = new PictoResponse(result);
		pictoResponse.setType("TreeView");

		MessageChannel brokerChannel = context.getBean("brokerChannel", MessageChannel.class);
		SimpMessagingTemplate messaging = new SimpMessagingTemplate(brokerChannel);
		messaging.setMessageConverter(new MappingJackson2MessageConverter());
		messaging.convertAndSend("/topic/picto/" + modifiedFile.getName(), pictoResponse);

		return pictoResponse;
	}

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
