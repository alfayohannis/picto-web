package org.eclipse.epsilon.picto.web;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.converter.ObjectToStringHttpMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/***
 * Receive Requests and send back Responses
 * 
 * @author Alfa Yohannis
 *
 */
@Controller
public class PictoController {

	public static final String WORKSPACE = ".."+ File.separator + "workspace" + File.separator;
	public final FileWatcher FILE_WATCHER = new FileWatcher(this);

	@Autowired
	private ApplicationContext context;
	
	@Autowired
	public SimpMessagingTemplate template;
	
	public PictoController() throws IOException {
		FILE_WATCHER.start();
	}

//	@GetMapping("/greeting")
//	public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
//		model.addAttribute("name", name);
//		return "greeting";
//	}

	@MessageMapping("/treeview")
	@SendTo("/topic/picto")
	public PictoResponse execute(PictoRequest message) throws Exception {
		WebEglPictoSourceImpl source = new WebEglPictoSourceImpl(null);
		String result = source.getViewTree(message.getCode());
		String temp = result;
		PictoResponse pictoResponse = new PictoResponse(temp);
		pictoResponse.setType("TreeView");
		return pictoResponse;
	}

	@MessageMapping("/gs-guide-websocket") 
	@SendTo("/topic/picto")
	public PictoResponse sendBackFileUpdate(File modelFile) throws Exception {
		WebEglPictoSourceImpl source = new WebEglPictoSourceImpl(null);
		String result = source.getViewTree(modelFile);
		String temp = result;
		PictoResponse pictoResponse = new PictoResponse(temp);
		pictoResponse.setType("TreeView");
	
		MessageChannel brokerChannel = context.getBean("brokerChannel", MessageChannel.class);
		SimpMessagingTemplate messaging = new SimpMessagingTemplate(brokerChannel);
		messaging.setMessageConverter(new MappingJackson2MessageConverter());
		messaging.convertAndSend("/topic/picto", pictoResponse);

		return pictoResponse;
	}

	@MessageMapping("/projecttree")
	@SendTo("/topic/picto")
	public PictoResponse getProjectTree(PictoRequest message) throws Exception {

		String temp = ProjectTreeToJson.convert(WORKSPACE);
		PictoResponse pictoResponse = new PictoResponse(temp);
		pictoResponse.setType("ProjectTree");
		return pictoResponse;
	}

	@MessageMapping("/openfile")
	@SendTo("/topic/picto")
	public PictoResponse openFile(PictoRequest message) throws Exception {

		String temp = Files.readString(Paths.get(WORKSPACE + message.getCode()));
		PictoResponse pictoResponse = new PictoResponse(temp);
		pictoResponse.setType("OpenFile");
		return pictoResponse;
	}
}
