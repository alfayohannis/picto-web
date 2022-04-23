package org.eclipse.epsilon.picto.web;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.HtmlUtils;

@Controller
public class PictoController {

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
	
	@MessageMapping("/foldertree")
	@SendTo("/topic/picto")
	public PictoResponse getProjectTree(PictoRequest message) throws Exception {
		
		String temp = "";
		PictoResponse pictoResponse = new PictoResponse(temp);
		pictoResponse.setType("ProjectTree");
		return pictoResponse;
	}

}
